package com.example.pitapp.model

data class SavedStudent(
    val name: String = "",
    val studentId: String = "",
    val academicProgram: String = "",
    val email: String = "",
    val isRegular: Boolean = true,
    val signature: String = ""
)