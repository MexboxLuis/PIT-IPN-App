package com.example.pitapp.data

import com.google.firebase.Timestamp


data class ClassData(
    val email: String = "",
    val tutoring: String = "",
    val topic: String = "",
    val classroom: String = "",
    val startTime: Timestamp = Timestamp.now(),
    val expectedDuration: Long? = null,
    val realDuration: Long? = null
)


data class Student(
    val name: String = "",
    val studentId: String = "",
    val academicProgram: String = "",
    val email: String = "",
    val status: String = "",
    val signature: String = ""
)



