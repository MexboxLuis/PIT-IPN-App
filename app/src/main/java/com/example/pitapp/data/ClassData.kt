package com.example.pitapp.data

import com.google.firebase.Timestamp


data class ClassData(
    val email: String = "",
    val tutoring: String = "",
    val topic: String = "",
    val classroom: String = "",
    val startTime: Timestamp = Timestamp.now(),
    val students: List<String>? = null,
    val expectedDuration: Long? = null,
    val realDuration: Long? = null
)

data class Student(
    val name: String = "",
    val studentId: String = "", // Student ID
    val academicProgram: String = "", // Career program
    val email: String = "",
    val status: String = "", // Regular or Irregular
    val signature: String = "" // Signature
)



