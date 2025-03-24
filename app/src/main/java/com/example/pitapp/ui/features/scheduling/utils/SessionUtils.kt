package com.example.pitapp.ui.features.scheduling.utils

import com.example.pitapp.ui.features.scheduling.model.Session

fun createSessions(
    selectedDays: Map<Int, Boolean>,
    sessionsState: Map<Int, String>
): List<Session> {
    return selectedDays.mapNotNull { (day, isSelected) ->
        if (isSelected) {
            val hour = sessionsState[day]?.toIntOrNull()
            if (hour != null) Session(dayOfWeek = day, startTime = hour) else null
        } else {
            null
        }
    }
}