package com.example.pitapp.model

import android.os.Build
import androidx.annotation.RequiresApi
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

@RequiresApi(Build.VERSION_CODES.O)
fun isDateInPeriod(date: LocalDate, periods: List<Period>): Boolean =
    periods.any { period ->
        val start = period.getStartLocalDate()
        val end = period.getEndLocalDate()
        !date.isBefore(start) && !date.isAfter(end)
    }