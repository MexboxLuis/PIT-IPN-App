package com.example.pitapp.ui.model

import com.example.pitapp.data.ClassData
import com.google.firebase.Timestamp

enum class ClassState { IN_PROGRESS, UPCOMING, FINISHED }

fun determineClassState(classData: ClassData): ClassState {
    val now = Timestamp.now().seconds + 1
    return when {
        classData.realDuration != null -> ClassState.FINISHED
        classData.startTime.seconds <= now -> ClassState.IN_PROGRESS
        else -> ClassState.UPCOMING
    }
}