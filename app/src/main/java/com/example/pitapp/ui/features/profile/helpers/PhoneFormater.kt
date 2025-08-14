package com.example.pitapp.ui.features.profile.helpers

fun formatPhoneNumber(phone: String): String {
    return if (phone.length == 10) {
        return if (phone.startsWith("55")) {
            val prefix = phone.substring(2, 6)
            val lineNumber = phone.substring(6)
            "+52 (55) $prefix-$lineNumber"
        } else {
            val areaCode = phone.substring(0, 3)
            val prefix = phone.substring(3, 6)
            val lineNumber = phone.substring(6)
            "+52 ($areaCode) $prefix-$lineNumber"
        }
    } else {
        "+52 $phone"
    }
}