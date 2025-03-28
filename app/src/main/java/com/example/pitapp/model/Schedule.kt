package com.example.pitapp.model

data class Schedule(
    val salonId: String = "",
    val tutorEmail: String = "",
    val subject: String = "",
    val approved: Boolean = false,
    val startYear: Int = 0,
    val startMonth: Int = 0,
    val endYear: Int = 0,
    val endMonth: Int = 0,
    val sessions: List<Session> = emptyList()
)