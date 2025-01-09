package com.example.pitapp.utils

import com.example.pitapp.data.ClassData
import com.example.pitapp.ui.model.ClassState
import com.google.firebase.Timestamp

fun determineClassState(classData: ClassData): ClassState {
    val now = Timestamp.now().seconds + 1
    return when {
        classData.realDuration != null -> ClassState.FINISHED
        classData.startTime.seconds <= now -> ClassState.IN_PROGRESS
        else -> ClassState.UPCOMING
    }
}