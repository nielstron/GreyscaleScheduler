package de.nielstron.scheduler.ui

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import de.nielstron.scheduler.model.Schedule
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier,
) {
    val schedule by viewModel.schedule.collectAsState()
    val permissions by viewModel.permissions.collectAsState()
    val grayscaleEnabled by viewModel.grayscaleEnabled.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var enabled by remember { mutableStateOf(schedule.enabled) }
    var startTime by remember { mutableStateOf(LocalTime.of(schedule.startHour, schedule.startMinute)) }
    var endTime by remember { mutableStateOf(LocalTime.of(schedule.endHour, schedule.endMinute)) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showPermissionHelp by remember { mutableStateOf(false) }

    LaunchedEffect(schedule) {
        enabled = schedule.enabled
        startTime = LocalTime.of(schedule.startHour, schedule.startMinute)
        endTime = LocalTime.of(schedule.endHour, schedule.endMinute)
    }

    LaunchedEffect(enabled, startTime, endTime, schedule) {
        val updatedSchedule = Schedule(
            enabled = enabled,
            startHour = startTime.hour,
            startMinute = startTime.minute,
            endHour = endTime.hour,
            endMinute = endTime.minute,
        )
        if (updatedSchedule != schedule) {
            viewModel.saveSchedule(updatedSchedule)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
                viewModel.refreshGrayscaleState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showPermissionHelp) {
        PermissionHelpScreen(
            secureSettingsGranted = permissions.secureSettingsGranted,
            onBack = { showPermissionHelp = false },
        )
        return
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = "Greyscale Scheduler",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Schedule daily grayscale mode and let the app toggle it for you.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(text = "Enable schedule", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = if (enabled) "Active" else "Paused",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TimeRow(
                        label = "Start grayscale",
                        time = startTime,
                        onPick = { showStartPicker = true },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TimeRow(
                        label = "End grayscale",
                        time = endTime,
                        onPick = { showEndPicker = true },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    val enableGrayscale = grayscaleEnabled != true
                    val success = viewModel.setGrayscale(enableGrayscale)
                    if (!success) {
                        Toast.makeText(
                            context,
                            "Missing WRITE_SECURE_SETTINGS permission.",
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (grayscaleEnabled == true) "Stop grayscale" else "Start grayscale")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Permissions",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (permissions.secureSettingsGranted) {
                    "Secure settings permission granted."
                } else {
                    "Secure settings permission missing. Grayscale toggling won't work without it."
                },
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { showPermissionHelp = true }) {
                Text("How to enable it")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !permissions.exactAlarmsAllowed) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Exact alarm permission required for fixed times.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        context.startActivity(intent)
                    },
                ) {
                    Text("Allow exact alarms")
                }
            }
        }
    }

    if (showStartPicker) {
        TimePickerDialog(
            title = "Start time",
            initial = startTime,
            onDismiss = { showStartPicker = false },
            onConfirm = {
                startTime = it
                showStartPicker = false
            },
        )
    }

    if (showEndPicker) {
        TimePickerDialog(
            title = "End time",
            initial = endTime,
            onDismiss = { showEndPicker = false },
            onConfirm = {
                endTime = it
                showEndPicker = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionHelpScreen(
    secureSettingsGranted: Boolean,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val adbCommand =
        "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
    val adbDevicesCommand = "adb devices"
    val platformToolsUrl = "https://developer.android.com/studio/releases/platform-tools"
    val platformToolsText = buildAnnotatedString {
        append("Install Android SDK Platform-Tools from: ")
        val start = length
        append(platformToolsUrl)
        val end = length
        addStringAnnotation(tag = "URL", annotation = platformToolsUrl, start = start, end = end)
        addStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            ),
            start = start,
            end = end,
        )
    }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grant permissions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        SelectionContainer {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(20.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Top,
            ) {
                Text(
                    text = "This app toggles system-wide grayscale by writing secure settings. Android requires the privileged permission WRITE_SECURE_SETTINGS, which cannot be granted by the app itself.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Step 1: Install ADB",
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(6.dp))
                ClickableText(
                    text = platformToolsText,
                    style = MaterialTheme.typography.bodySmall,
                    onClick = { offset ->
                        platformToolsText
                            .getStringAnnotations(tag = "URL", start = offset, end = offset)
                            .firstOrNull()
                            ?.let { uriHandler.openUri(it.item) }
                    },
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Step 2: Enable Developer Options",
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Settings -> About phone -> tap Build number 7 times. Then enable USB debugging in Developer options.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Step 3: Connect the device",
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Connect via USB and accept the USB debugging prompt. Check that the device is listed as 'device' with:",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = adbDevicesCommand,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { clipboardManager.setText(AnnotatedString(adbDevicesCommand)) }) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy adb devices",
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Step 4: Grant the permission",
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = adbCommand,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { clipboardManager.setText(AnnotatedString(adbCommand)) }) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy ADB command",
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Step 5: Verify",
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Open the app and tap Start grayscale. If it works, the permission is active.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                    ) {
                        Text(
                            if (secureSettingsGranted) {
                                "Permission was granted"
                            } else {
                                "Permission was not granted"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeRow(
    label: String,
    time: LocalTime,
    onPick: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.titleSmall)
            Text(text = formatTime(context, time), style = MaterialTheme.typography.bodySmall)
        }
        OutlinedButton(onClick = onPick) {
            Text("Pick time")
        }
    }
}

private fun formatTime(context: android.content.Context, time: LocalTime): String {
    val is24 = DateFormat.is24HourFormat(context)
    val pattern = if (is24) "HH:mm" else "h:mm a"
    return time.format(DateTimeFormatter.ofPattern(pattern))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String,
    initial: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val is24 = DateFormat.is24HourFormat(LocalContext.current)
    val pickerState = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = is24,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            Button(onClick = {
                onConfirm(LocalTime.of(pickerState.hour, pickerState.minute))
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
