package com.example.pitapp.data

data class UserData(
    val email: String = "",
    val name: String = "",
    val surname: String = "",
    val profilePictureUrl: String? = null,
    val permission: Int = 0
)
