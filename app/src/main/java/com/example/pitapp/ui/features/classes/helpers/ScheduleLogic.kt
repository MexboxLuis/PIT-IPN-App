package com.example.pitapp.ui.features.classes.helpers

import com.example.pitapp.model.Schedule
import com.example.pitapp.model.Session
import java.util.Calendar

fun nextSessionTime(
    schedule: Schedule,
    now: Calendar
): Pair<Session, Calendar>? {

    val searchStartTime = now.clone() as Calendar

    val sortedSessions = schedule.sessions.sortedWith(compareBy(
        { session -> if (session.dayOfWeek == 7) Calendar.SUNDAY else session.dayOfWeek + 1 },
        { session -> session.startTime }
    ))

    var earliestNextSessionPair: Pair<Session, Calendar>? = null

    for (session in sortedSessions) {
        val calendarDayOfWeek = if (session.dayOfWeek == 7) Calendar.SUNDAY else session.dayOfWeek + 1

        val potentialSessionTime = (searchStartTime.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            val currentDayOfWeek = get(Calendar.DAY_OF_WEEK)
            var daysToAdd = calendarDayOfWeek - currentDayOfWeek
            if (daysToAdd < 0) {
                daysToAdd += 7
            }
            add(Calendar.DAY_OF_YEAR, daysToAdd)

            set(Calendar.HOUR_OF_DAY, session.startTime)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (daysToAdd == 0) {
                if (before(searchStartTime)) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }
        }

        if (!isDateBeyondScheduleEnd(potentialSessionTime, schedule)) {
            if (earliestNextSessionPair == null || potentialSessionTime.before(earliestNextSessionPair.second)) {
                earliestNextSessionPair = Pair(session, potentialSessionTime)
            }
        }
    }

    return earliestNextSessionPair
}

fun findLastTheoreticalSession(schedule: Schedule): Pair<Session, Calendar>? {
    if (schedule.sessions.isEmpty() || schedule.endYear == 0 || schedule.endMonth == 0) {
        return null
    }

    val endCal = Calendar.getInstance().apply {
        clear()
        set(Calendar.YEAR, schedule.endYear)
        set(Calendar.MONTH, schedule.endMonth - 1)
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }

    var latestSessionPair: Pair<Session, Calendar>? = null

    for (session in schedule.sessions) {
        val calendarDayOfWeek =
            if (session.dayOfWeek == 7) Calendar.SUNDAY else session.dayOfWeek + 1

        val potentialLastOccurrence = (endCal.clone() as Calendar).apply {

            val lastDayOfMonthDayOfWeek = get(Calendar.DAY_OF_WEEK)

            var daysToSubtract = lastDayOfMonthDayOfWeek - calendarDayOfWeek
            if (daysToSubtract < 0) {
                daysToSubtract += 7
            }
            add(Calendar.DAY_OF_YEAR, -daysToSubtract)

            set(Calendar.HOUR_OF_DAY, session.startTime)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            val startCal = Calendar.getInstance().apply {
                clear()
                set(Calendar.YEAR, schedule.startYear)
                set(Calendar.MONTH, schedule.startMonth - 1)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
            }
            if (before(startCal)) {
                return@apply
            }
        }

        val startCheckCal = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, schedule.startYear)
            set(Calendar.MONTH, schedule.startMonth - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }
        if (potentialLastOccurrence.before(startCheckCal)) {
            continue
        }

        if (latestSessionPair == null || potentialLastOccurrence.after(latestSessionPair.second)) {
            latestSessionPair = Pair(session, potentialLastOccurrence)
        }
    }

    return latestSessionPair
}

private fun isDateBeyondScheduleEnd(dateToCheck: Calendar, schedule: Schedule): Boolean {
    val checkYear = dateToCheck.get(Calendar.YEAR)
    val checkMonth = dateToCheck.get(Calendar.MONTH) + 1

    return when {
        checkYear > schedule.endYear -> true
        checkYear == schedule.endYear && checkMonth > schedule.endMonth -> true
        else -> false
    }
}