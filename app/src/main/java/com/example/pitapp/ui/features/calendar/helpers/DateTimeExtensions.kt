package com.example.pitapp.ui.features.calendar.helpers

import android.annotation.SuppressLint
import com.example.pitapp.model.NonWorkingDay
import com.example.pitapp.model.Period
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId

@SuppressLint("NewApi")
fun LocalDate.toTimestampStartOfDay(): Timestamp {
    val instant = this.atStartOfDay(ZoneId.systemDefault()).toInstant()
    return Timestamp(instant.epochSecond, 0)
}

@SuppressLint("NewApi")
fun NonWorkingDay.getLocalDate(): LocalDate =
    this.date.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

@SuppressLint("NewApi")
fun Period.getStartLocalDate(): LocalDate =
    this.startDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

@SuppressLint("NewApi")
fun Period.getEndLocalDate(): LocalDate =
    this.endDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()