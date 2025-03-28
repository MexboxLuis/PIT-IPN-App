package com.example.pitapp.ui.features.calendar.helpers

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.pitapp.model.NonWorkingDay
import com.example.pitapp.model.Period
import java.time.LocalDate


fun isDateNonWorking(date: LocalDate, nonWorkingDays: List<NonWorkingDay>): Boolean =
    nonWorkingDays.any { it.getLocalDate() == date }

@RequiresApi(Build.VERSION_CODES.O)
fun getPeriodForDate(date: LocalDate, periods: List<Period>): Period? =
    periods.find { period ->
        val start = period.getStartLocalDate()
        val end = period.getEndLocalDate()
        !date.isBefore(start) && !date.isAfter(end)
    }