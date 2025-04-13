package com.example.pitapp.ui.features.classes.components

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.pitapp.model.Schedule
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
data class UpcomingClassDisplayInfo(
    val schedule: Schedule,
    val sessionTime: Calendar,
    val formattedTime: FormattedTimeInfo,
    val invalidityType: InvalidityType,
    val isTheVeryLastSession: Boolean
)

