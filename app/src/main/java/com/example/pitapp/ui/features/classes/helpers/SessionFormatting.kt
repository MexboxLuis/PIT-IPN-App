package com.example.pitapp.ui.features.classes.helpers

import android.content.Context
import com.example.pitapp.R
import com.example.pitapp.model.Session
import com.example.pitapp.ui.features.classes.components.FormattedTimeInfo
import java.util.Calendar
import java.util.concurrent.TimeUnit


fun formatSessionTime(context: Context, session: Session, sessionTime: Calendar): FormattedTimeInfo {
    val now = Calendar.getInstance()
    val adaptedSessionDayOfWeek = if (session.dayOfWeek == 7) 1 else session.dayOfWeek + 1
    val dayOfWeekString = when (adaptedSessionDayOfWeek) {
        Calendar.MONDAY -> context.getString(R.string.day_mon)
        Calendar.TUESDAY -> context.getString(R.string.day_tue)
        Calendar.WEDNESDAY -> context.getString(R.string.day_wed)
        Calendar.THURSDAY -> context.getString(R.string.day_thu)
        Calendar.FRIDAY -> context.getString(R.string.day_fri)
        Calendar.SATURDAY -> context.getString(R.string.day_sat)
        Calendar.SUNDAY -> context.getString(R.string.day_sun)
        else -> ""
    }
    val timeString = "${session.startTime}:00"
    val absoluteTimeString = context.getString(R.string.time_format_absolute, dayOfWeekString, timeString)

    val diffInMillis = sessionTime.timeInMillis - now.timeInMillis
    val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
    val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

    val relativeTimeString = when {
        diffInDays > 7 -> context.getString(R.string.coming_soon)
        diffInDays > 1 -> context.getString(R.string.relative_days, diffInDays.toInt())
        diffInDays == 1L -> {
            val remainderHours = diffInHours - 24
            if (remainderHours > 0)
                context.getString(
                    R.string.relative_day_hours,
                    remainderHours,
                    context.getString(if (remainderHours == 1L) R.string.hour else R.string.hours)
                )
            else
                context.getString(R.string.relative_day)
        }
        diffInHours > 0 -> context.getString(R.string.relative_hours, diffInHours)
        else -> context.getString(R.string.coming_soon)
    }

    return FormattedTimeInfo(
        relative = relativeTimeString,
        absolute = absoluteTimeString
    )
}

