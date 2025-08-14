package com.example.pitapp.ui.features.classes.helpers

import android.content.Context
import com.example.pitapp.R
import com.example.pitapp.model.SavedClass
import com.example.pitapp.model.SavedStudent
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun generateStudentsCsv(
    classesWithStudents: List<Pair<SavedClass, List<SavedStudent>>>,
    context: Context
): File {

    require(classesWithStudents.isNotEmpty()) { context.getString(R.string.error_no_classes_to_export) }

    val firstTutor = classesWithStudents.first().first.tutorEmail
    require(classesWithStudents.all { it.first.tutorEmail == firstTutor }) {
        context.getString(R.string.error_different_tutors)
    }

    val timeStamp = SimpleDateFormat("yyyy_MM_dd_HH-mm", Locale.getDefault())
        .format(System.currentTimeMillis())

    @Suppress("SpellCheckingInspection")
    val fileName = "asistencia_${firstTutor.substringBefore("@")}_$timeStamp.csv"
    val file = File(context.filesDir, fileName)

    csvWriter { charset = StandardCharsets.UTF_8.name() }
        .open(file, append = false) {
            @Suppress("SpellCheckingInspection")
            writeRow(
                listOf(
                    "Fecha",
                    "Nombre del alumno asesorado",
                    "Boleta",
                    "Horario",
                    "Tema Visto",
                    "Programa Educativo",
                    "Correo Electrónico",
                    "Regular o irregular"
                )
            )

            val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val hourFmt = SimpleDateFormat("HH:00", Locale.getDefault())

            for ((savedClass, students) in classesWithStudents) {
                val classDate = savedClass.date.toDate()
                val dateStr = dateFmt.format(classDate)

                val startHour = hourFmt.format(classDate)
                val endHour = hourFmt.format(
                    Calendar.getInstance().apply {
                        time = classDate; add(Calendar.HOUR_OF_DAY, 1)
                    }.time
                )
                val schedule = "$startHour - $endHour"

                for (student in students) {
                    writeRow(
                        listOf(
                            dateStr,
                            student.name,
                            student.studentId,
                            schedule,
                            savedClass.topic,
                            student.academicProgram,
                            student.email,
                            if (student.regular) context.getString(R.string.student_regular)
                            else context.getString(R.string.student_irregular)
                        )
                    )
                }
            }
        }
    return file
}