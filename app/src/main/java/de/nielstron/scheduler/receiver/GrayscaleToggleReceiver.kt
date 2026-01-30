package de.nielstron.scheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.nielstron.scheduler.data.SchedulePreferences
import de.nielstron.scheduler.scheduler.AlarmScheduler
import de.nielstron.scheduler.util.GrayscaleController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GrayscaleToggleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val enable = intent.getBooleanExtra(AlarmScheduler.EXTRA_ENABLE, false)
        val type = intent.getStringExtra(AlarmScheduler.EXTRA_TYPE) ?: AlarmScheduler.TYPE_START

        GrayscaleController.setGrayscale(context, enable)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val schedule = SchedulePreferences(context.applicationContext).readSchedule()
                AlarmScheduler.scheduleNext(context.applicationContext, schedule, type)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
