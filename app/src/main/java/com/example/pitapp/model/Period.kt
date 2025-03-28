package com.example.pitapp.model

import com.google.firebase.Timestamp

data class Period(
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now()
)