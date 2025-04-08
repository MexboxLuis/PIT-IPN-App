package com.example.pitapp.ui.shared.formatting

import java.util.Locale

fun formatTitleCase(stringText: String): String {

    val exceptions = setOf("en", "de", "la", "los", "las", "y", "o", "a")

    if (stringText.isBlank()) {
        return stringText
    }

    val words = stringText.trim().split(" ").filter { it.isNotEmpty() }
    val result = mutableListOf<String>()

    for ((index, word) in words.withIndex()) {

        val lowerWord = word.lowercase(Locale.getDefault())

        if (index == 0) {
            result.add(lowerWord.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            })
        } else {
            if (exceptions.contains(lowerWord)) {
                result.add(lowerWord)
            } else {
                result.add(
                    lowerWord.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }
                )
            }
        }
    }

    return result.joinToString(" ")
}