package de.nielstron.scheduler.ui

import android.app.AlarmManager
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.nielstron.scheduler.data.SchedulePreferences
import de.nielstron.scheduler.model.Schedule
import de.nielstron.scheduler.scheduler.AlarmScheduler
import de.nielstron.scheduler.util.GrayscaleController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = SchedulePreferences(application)

    private val _schedule = MutableStateFlow(Schedule.default())
    val schedule: StateFlow<Schedule> = _schedule

    private val _permissions = MutableStateFlow(refreshPermissionState())
    val permissions: StateFlow<PermissionState> = _permissions

    private val _grayscaleEnabled = MutableStateFlow(GrayscaleController.isGrayscaleEnabled(application))
    val grayscaleEnabled: StateFlow<Boolean?> = _grayscaleEnabled

    init {
        viewModelScope.launch {
            prefs.scheduleFlow.collect { schedule ->
                _schedule.value = schedule
            }
        }
    }

    fun saveSchedule(schedule: Schedule) {
        viewModelScope.launch {
            prefs.saveSchedule(schedule)
            AlarmScheduler.scheduleAll(getApplication(), schedule)
        }
    }

    fun refreshPermissions() {
        _permissions.value = refreshPermissionState()
    }

    fun refreshGrayscaleState() {
        _grayscaleEnabled.value = GrayscaleController.isGrayscaleEnabled(getApplication())
    }

    fun setGrayscale(enabled: Boolean): Boolean {
        val success = GrayscaleController.setGrayscale(getApplication(), enabled)
        if (success) {
            _grayscaleEnabled.value = enabled
        }
        return success
    }

    private fun refreshPermissionState(): PermissionState {
        val context = getApplication<Application>()
        val secureSettingsGranted = context.checkSelfPermission(
            android.Manifest.permission.WRITE_SECURE_SETTINGS
        ) == PackageManager.PERMISSION_GRANTED
        val exactAlarmsAllowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            alarmManager?.canScheduleExactAlarms() == true
        } else {
            true
        }
        return PermissionState(
            secureSettingsGranted = secureSettingsGranted,
            exactAlarmsAllowed = exactAlarmsAllowed,
        )
    }
}

data class PermissionState(
    val secureSettingsGranted: Boolean,
    val exactAlarmsAllowed: Boolean,
)
