package com.example.pitapp.ui.features.classes.helpers

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.pitapp.R
import com.example.pitapp.ui.features.classes.screens.TutorWithClasses
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.forEach

@RequiresApi(Build.VERSION_CODES.O)
fun generateGlobalPeerTutoringSummaryCsv(
    peerTutorsData: List<TutorWithClasses>,
    context: Context
): File {

    val hasClassesToExport = peerTutorsData.any { it.allPastClassesWithStudents.isNotEmpty() }
    require(hasClassesToExport) {
        context.getString(R.string.error_no_peer_tutoring_classes_to_export)
    }

    val timeStamp = SimpleDateFormat("yyyy_MM_dd_HH-mm", Locale.getDefault())
        .format(System.currentTimeMillis())
    @Suppress("SpellCheckingInspection")
    val fileName = "resumen_global_tutorias_pares_$timeStamp.csv"
    val file = File(context.filesDir, fileName)

    csvWriter { charset = StandardCharsets.UTF_8.name() }
        .open(file, append = false) {
            writeRow(
                @Suppress("SpellCheckingInspection")
                listOf(
                    "Fecha",
                    "Nombre",
                    "Tutoría entre Pares",
                    "Horario",
                    "Tema Visto",
                    "N° Alumnos Atendidos",
                    "Lugar de Atención"
                )
            )

            val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val hourFmt = SimpleDateFormat("HH:00", Locale.getDefault())

            peerTutorsData.forEach { tutorData ->
                val tutorInfo = tutorData.tutorInfo
                tutorData.allPastClassesWithStudents.forEach { (classId, savedClass) ->
                    val studentCount = tutorData.studentsCountMap[classId] ?: 0

                    val classDate = savedClass.date.toDate()
                    val dateStr = dateFmt.format(classDate)
                    val tutorFullName = "${tutorInfo.name} ${tutorInfo.surname}".trim()

                    val startHour = hourFmt.format(classDate)
                    val endHourCalendar = Calendar.getInstance().apply {
                        time = classDate
                        add(Calendar.HOUR_OF_DAY, 1)
                    }
                    val endHour = hourFmt.format(endHourCalendar.time)
                    val schedule = "$startHour - $endHour"

                    writeRow(
                        listOf(
                            dateStr,
                            tutorFullName,
                            savedClass.subject,
                            schedule,
                            savedClass.topic,
                            studentCount.toString(),
                            savedClass.classroom
                        )
                    )
                }
            }
        }
    return file
}