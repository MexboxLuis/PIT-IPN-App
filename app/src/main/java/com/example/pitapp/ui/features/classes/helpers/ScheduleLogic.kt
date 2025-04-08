package com.example.pitapp.ui.features.classes.helpers

import com.example.pitapp.model.Schedule
import com.example.pitapp.model.Session
import java.util.Calendar

fun nextSessionTime(
    schedule: Schedule,
    now: Calendar
): Pair<Session, Calendar>? {
    val currentYear = now.get(Calendar.YEAR)
    val currentMonth = now.get(Calendar.MONTH) + 1
    val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
    val adaptedCurrentDayOfWeek =
        if (currentDayOfWeek == Calendar.SUNDAY) 7 else currentDayOfWeek - 1
    val currentHour = now.get(Calendar.HOUR_OF_DAY)

    if (currentYear !in schedule.startYear..schedule.endYear ||
        !schedule.isWithinDateRange(currentYear, currentMonth)
    ) return null


    var nextSession: Session? = null
    var nextSessionTime: Calendar? = null

    for (session in schedule.sessions) {
        val adaptedSessionDayOfWeek = if (session.dayOfWeek == 7) 1 else session.dayOfWeek + 1
        val sessionTime = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth - 1)
            set(Calendar.DAY_OF_WEEK, adaptedSessionDayOfWeek)
            set(Calendar.HOUR_OF_DAY, session.startTime)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (adaptedSessionDayOfWeek == adaptedCurrentDayOfWeek && currentHour <= session.startTime) {
            if (nextSessionTime == null || sessionTime.before(nextSessionTime)) {
                nextSession = session
                nextSessionTime = sessionTime
            }
        } else if (sessionTime.after(now)) {
            if (nextSessionTime == null || sessionTime.before(nextSessionTime)) {
                nextSession = session
                nextSessionTime = sessionTime
            }
        }
    }

    if (nextSession == null) {
        for (session in schedule.sessions) {
            val adaptedSessionDayOfWeek = if (session.dayOfWeek == 7) 1 else session.dayOfWeek + 1
            val sessionTime = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth - 1)
                set(Calendar.DAY_OF_WEEK, adaptedSessionDayOfWeek)
                set(Calendar.HOUR_OF_DAY, session.startTime)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.WEEK_OF_YEAR, 1)
            }

            if (nextSessionTime == null || sessionTime.before(nextSessionTime)) {
                nextSession = session
                nextSessionTime = sessionTime
            }
        }
    }

    return if (nextSession != null && nextSessionTime != null) {
        Pair(nextSession, nextSessionTime)
    } else {
        null
    }
}