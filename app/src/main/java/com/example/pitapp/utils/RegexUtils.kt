package com.example.pitapp.utils

fun isValidEmail(email: String): Boolean {
    val ipnEmailPattern = Regex("^[a-zA-Z0-9._%+-]+@([a-zA-Z0-9-]+\\.)?ipn\\.mx\$")
    return ipnEmailPattern.matches(email)
}

fun isValidPassword(password: String): Boolean {
    return password.length >= 6
}

fun registerData(currentRoute: String): Boolean{
    val routePattern = Regex("^registerAllDataScreen/.+$")
    return routePattern.matches(currentRoute)
}