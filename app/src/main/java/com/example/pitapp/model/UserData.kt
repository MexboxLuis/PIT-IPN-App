package com.example.pitapp.model

data class UserData(
    val email: String = "",
    val name: String = "",
    val surname: String = "",
    val profilePictureUrl: String? = null,
    val permission: Int = 0,
    val academicProgram: String? = null,
    val studentId: String? = null,
    val phoneNumber: String? = null
)