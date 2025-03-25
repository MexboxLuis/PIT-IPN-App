package com.example.pitapp.ui.features.classes.model
import com.google.firebase.Timestamp

data class SavedClass(
    val tutorEmail: String = "",
    val subject: String = "",
    val classroom: String = "",
    val topic: String = "",
    val date: Timestamp = Timestamp.now(),
)