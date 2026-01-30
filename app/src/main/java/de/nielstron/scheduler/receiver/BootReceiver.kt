package de.nielstron.scheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.nielstron.scheduler.data.SchedulePreferences
import de.nielstron.scheduler.scheduler.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val schedule = SchedulePreferences(context.applicationContext).readSchedule()
                AlarmScheduler.scheduleAll(context.applicationContext, schedule)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
