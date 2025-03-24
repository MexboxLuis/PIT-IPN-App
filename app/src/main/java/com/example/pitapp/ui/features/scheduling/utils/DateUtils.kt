package com.example.pitapp.ui.features.scheduling.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.pitapp.R

@Composable
fun dayOfWeekToString(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        1 -> stringResource(R.string.day_mon)
        2 -> stringResource(R.string.day_tue)
        3 -> stringResource(R.string.day_wed)
        4 -> stringResource(R.string.day_thu)
        5 -> stringResource(R.string.day_fri)
        6 -> stringResource(R.string.day_sat)
        7 -> stringResource(R.string.day_sun)
        else -> ""
    }
}

@Composable
fun monthToString(month: Int): String {
    return when (month) {
        1 -> stringResource(R.string.january)
        2 -> stringResource(R.string.february)
        3 -> stringResource(R.string.march)
        4 -> stringResource(R.string.april)
        5 -> stringResource(R.string.may)
        6 -> stringResource(R.string.june)
        7 -> stringResource(R.string.july)
        8 -> stringResource(R.string.august)
        9 -> stringResource(R.string.september)
        10 -> stringResource(R.string.october)
        11 -> stringResource(R.string.november)
        12 -> stringResource(R.string.december)
        else -> ""
    }
}