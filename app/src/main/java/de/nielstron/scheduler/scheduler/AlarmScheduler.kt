package de.nielstron.scheduler.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import de.nielstron.scheduler.model.Schedule
import de.nielstron.scheduler.receiver.GrayscaleToggleReceiver
import java.time.LocalTime
import java.time.ZonedDateTime

object AlarmScheduler {
    const val ACTION_TOGGLE = "de.nielstron.scheduler.action.TOGGLE"
    const val EXTRA_ENABLE = "extra_enable"
    const val EXTRA_TYPE = "extra_type"

    const val TYPE_START = "start"
    const val TYPE_END = "end"

    private const val REQUEST_CODE_START = 1001
    private const val REQUEST_CODE_END = 1002

    fun scheduleAll(context: Context, schedule: Schedule) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (alarmManager == null) return

        cancelAll(context, alarmManager)
        if (!schedule.enabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }

        val now = ZonedDateTime.now()
        val startTime = LocalTime.of(schedule.startHour, schedule.startMinute)
        val endTime = LocalTime.of(schedule.endHour, schedule.endMinute)

        scheduleExact(
            alarmManager,
            context,
            startTime,
            now,
            true,
            TYPE_START,
            REQUEST_CODE_START,
        )
        scheduleExact(
            alarmManager,
            context,
            endTime,
            now,
            false,
            TYPE_END,
            REQUEST_CODE_END,
        )
    }

    fun scheduleNext(context: Context, schedule: Schedule, type: String) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        if (!schedule.enabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }

        val now = ZonedDateTime.now()
        when (type) {
            TYPE_START -> {
                val time = LocalTime.of(schedule.startHour, schedule.startMinute)
                scheduleExact(
                    alarmManager,
                    context,
                    time,
                    now,
                    true,
                    TYPE_START,
                    REQUEST_CODE_START,
                )
            }
            TYPE_END -> {
                val time = LocalTime.of(schedule.endHour, schedule.endMinute)
                scheduleExact(
                    alarmManager,
                    context,
                    time,
                    now,
                    false,
                    TYPE_END,
                    REQUEST_CODE_END,
                )
            }
        }
    }

    private fun scheduleExact(
        alarmManager: AlarmManager,
        context: Context,
        time: LocalTime,
        now: ZonedDateTime,
        enable: Boolean,
        type: String,
        requestCode: Int,
    ) {
        val triggerAt = nextTriggerMillis(now, time)
        val intent = Intent(context, GrayscaleToggleReceiver::class.java).apply {
            action = ACTION_TOGGLE
            putExtra(EXTRA_ENABLE, enable)
            putExtra(EXTRA_TYPE, type)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }

    private fun nextTriggerMillis(now: ZonedDateTime, time: LocalTime): Long {
        var trigger = now
            .withHour(time.hour)
            .withMinute(time.minute)
            .withSecond(0)
            .withNano(0)
        if (!trigger.isAfter(now)) {
            trigger = trigger.plusDays(1)
        }
        return trigger.toInstant().toEpochMilli()
    }

    private fun cancelAll(context: Context, alarmManager: AlarmManager) {
        alarmManager.cancel(buildPendingIntent(context, REQUEST_CODE_START))
        alarmManager.cancel(buildPendingIntent(context, REQUEST_CODE_END))
    }

    private fun buildPendingIntent(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, GrayscaleToggleReceiver::class.java).apply {
            action = ACTION_TOGGLE
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
