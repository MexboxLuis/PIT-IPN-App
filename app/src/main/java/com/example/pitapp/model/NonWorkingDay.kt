package com.example.pitapp.model

import com.google.firebase.Timestamp

data class NonWorkingDay(
    val date: Timestamp = Timestamp.now()
)

