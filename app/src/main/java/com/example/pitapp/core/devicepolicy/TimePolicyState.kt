package com.example.pitapp.core.devicepolicy

import android.content.ContentResolver
import android.os.Build
import android.provider.Settings
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.TimeZone

data class TimePolicyState(
    val autoTimeEnabled: Boolean,
    val isMexicoCityZone: Boolean,
    val isUtcMinus6Now: Boolean
) {
    val isCompliant: Boolean
        get() = autoTimeEnabled && isMexicoCityZone && isUtcMinus6Now
}

private fun readAutoTime(cr: ContentResolver): Boolean =
    Settings.Global.getInt(cr, Settings.Global.AUTO_TIME, 0) == 1

fun currentTimePolicy(cr: ContentResolver): TimePolicyState {
    val autoTime = readAutoTime(cr)
    val tz = TimeZone.getDefault()
    val isMxCity = tz.id == "America/Mexico_City"

    val isUtcMinus6 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ZonedDateTime.now(ZoneId.systemDefault()).offset.totalSeconds == -6 * 3600
    } else {
        tz.getOffset(System.currentTimeMillis()) == -6 * 3600 * 1000
    }

    return TimePolicyState(
        autoTimeEnabled = autoTime,
        isMexicoCityZone = isMxCity,
        isUtcMinus6Now = isUtcMinus6
    )
}
