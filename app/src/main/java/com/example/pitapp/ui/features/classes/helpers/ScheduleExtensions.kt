package com.example.pitapp.ui.features.classes.helpers

import com.example.pitapp.model.Schedule

fun Schedule.isWithinDateRange(currentYear: Int, currentMonth: Int): Boolean {
    return when {
        startYear == endYear -> currentMonth in startMonth..endMonth
        currentYear == startYear -> currentMonth >= startMonth
        currentYear == endYear -> currentMonth <= endMonth
        else -> currentYear in (startYear + 1) until endYear
    }
}
