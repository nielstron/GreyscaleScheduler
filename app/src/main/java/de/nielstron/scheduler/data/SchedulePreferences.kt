package de.nielstron.scheduler.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.nielstron.scheduler.model.Schedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "schedule_prefs")

class SchedulePreferences(private val context: Context) {
    private val enabledKey = booleanPreferencesKey("enabled")
    private val startHourKey = intPreferencesKey("start_hour")
    private val startMinuteKey = intPreferencesKey("start_minute")
    private val endHourKey = intPreferencesKey("end_hour")
    private val endMinuteKey = intPreferencesKey("end_minute")

    val scheduleFlow: Flow<Schedule> = context.dataStore.data.map { prefs ->
        Schedule(
            enabled = prefs[enabledKey] ?: Schedule.default().enabled,
            startHour = prefs[startHourKey] ?: Schedule.default().startHour,
            startMinute = prefs[startMinuteKey] ?: Schedule.default().startMinute,
            endHour = prefs[endHourKey] ?: Schedule.default().endHour,
            endMinute = prefs[endMinuteKey] ?: Schedule.default().endMinute,
        )
    }

    suspend fun readSchedule(): Schedule = scheduleFlow.first()

    suspend fun saveSchedule(schedule: Schedule) {
        context.dataStore.edit { prefs ->
            prefs[enabledKey] = schedule.enabled
            prefs[startHourKey] = schedule.startHour
            prefs[startMinuteKey] = schedule.startMinute
            prefs[endHourKey] = schedule.endHour
            prefs[endMinuteKey] = schedule.endMinute
        }
    }
}
