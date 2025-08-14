package com.example.pitapp.ui.features.classes.helpers

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.pitapp.ui.features.home.screens.TimeFilter
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
internal fun isDateInPeriod(
    classLocalDate: LocalDate?,
    selectedDate: LocalDate,
    selectedTimeFilter: TimeFilter,
    locale: Locale
): Boolean {
    if (classLocalDate == null) return false
    return when (selectedTimeFilter) {
        TimeFilter.NONE -> true
        TimeFilter.WEEK -> {
            val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
            val startOfWeekSelected = selectedDate.with(firstDayOfWeek)
            val endOfWeekSelected = startOfWeekSelected.plusDays(6)
            !classLocalDate.isBefore(startOfWeekSelected) && !classLocalDate.isAfter(
                endOfWeekSelected
            )
        }

        TimeFilter.MONTH -> YearMonth.from(classLocalDate) == YearMonth.from(
            selectedDate
        )

        TimeFilter.YEAR -> classLocalDate.year == selectedDate.year
    }
}