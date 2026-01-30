package de.nielstron.scheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import de.nielstron.scheduler.ui.ScheduleScreen
import de.nielstron.scheduler.ui.ScheduleViewModel
import de.nielstron.scheduler.ui.theme.GreyscaleSchedulerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val scheduleViewModel = ViewModelProvider(this)[ScheduleViewModel::class.java]
        setContent {
            GreyscaleSchedulerTheme {
                ScheduleScreen(viewModel = scheduleViewModel)
            }
        }
    }
}
