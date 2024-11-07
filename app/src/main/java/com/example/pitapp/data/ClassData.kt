package com.example.pitapp.data

import com.google.firebase.Timestamp

data class ClassData(
    val id: String,
    val email: String,
    val tutoring: String,
    val topic: String,
    val classroom: String,
    val startTime: Timestamp,
    val students: List<String> = listOf(),
    val expectedDuration: Long? = null,
    val realDuration: Long? = null
)
