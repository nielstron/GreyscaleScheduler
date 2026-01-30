package de.nielstron.scheduler.model

data class Schedule(
    val enabled: Boolean,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
) {
    companion object {
        fun default(): Schedule = Schedule(
            enabled = false,
            startHour = 22,
            startMinute = 0,
            endHour = 7,
            endMinute = 0,
        )
    }
}
