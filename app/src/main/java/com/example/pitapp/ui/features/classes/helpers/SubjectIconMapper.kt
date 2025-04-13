package com.example.pitapp.ui.features.classes.helpers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BorderColor
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Pool
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.Normalizer

@Composable
fun getSubjectIcon(subject: String): ImageVector {
    val normalized = Normalizer.normalize(subject, Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .lowercase()
    @Suppress("SpellCheckingInspection")
    return when {
        normalized.contains("calculo") ||
                normalized.contains("matematicas") ||
                normalized.contains("algebra") ||
                normalized.contains("geometria") ||
                normalized.contains("estadistica") -> Icons.Filled.Calculate

        normalized.contains("fisica") -> Icons.Filled.Science

        normalized.contains("quimica") -> Icons.Filled.Science

        normalized.contains("historia") -> Icons.Filled.HistoryEdu

        normalized.contains("programacion") ||
                normalized.contains("informatica") ||
                normalized.contains("desarrollo") ||
                normalized.contains("coding") -> Icons.Filled.Code

        normalized.contains("geografia") -> Icons.Outlined.LocationOn

        normalized.contains("arte") ||
                normalized.contains("dibujo") ||
                normalized.contains("pintura") ||
                normalized.contains("artes") -> Icons.Filled.Brush

        normalized.contains("biologia") ||
                normalized.contains("ciencias de la vida") -> Icons.Filled.Biotech

        normalized.contains("literatura") ||
                normalized.contains("lengua") ||
                normalized.contains("espanol") ||
                normalized.contains("ingles") ||
                normalized.contains("idiomas") ||
                normalized.contains("lenguas") -> Icons.AutoMirrored.Filled.MenuBook

        normalized.contains("musica") ||
                normalized.contains("arte musical") -> Icons.Filled.MusicNote

        normalized.contains("traduccion") -> Icons.Filled.Translate

        normalized.contains("economia") ||
                normalized.contains("finanzas") -> Icons.Filled.AttachMoney

        normalized.contains("derecho") ||
                normalized.contains("leyes") ||
                normalized.contains("juridico") -> Icons.Filled.Gavel

        normalized.contains("psicologia") -> Icons.Filled.Face

        normalized.contains("sistemas") ||
                normalized.contains("tecnologias de la informacion") ||
                normalized.contains("tics") -> Icons.Filled.Computer

        normalized.contains("tecnologia") -> Icons.Filled.Devices

        normalized.contains("educacion fisica") ||
                normalized.contains("deporte") -> Icons.Filled.FitnessCenter

        normalized.contains("econometria") -> Icons.Outlined.InsertChart

        normalized.contains("filosofia") -> Icons.Filled.Lightbulb

        normalized.contains("sociologia") -> Icons.Filled.Groups

        normalized.contains("arquitectura") ||
                normalized.contains("diseno arquitectonico") -> Icons.Filled.HomeWork

        normalized.contains("quijotesco") ||
                normalized.contains("literatura clasica") -> Icons.Outlined.AutoStories

        normalized.contains("robotica") -> Icons.Filled.SmartToy

        normalized.contains("astronomia") -> Icons.Outlined.StarOutline

        normalized.contains("dibujo tecnico") -> Icons.Outlined.BorderColor

        normalized.contains("ciencias sociales") ||
                normalized.contains("ciencias politicas") -> Icons.Filled.People

        normalized.contains("mapas") ||
                normalized.contains("cartografia") -> Icons.Outlined.Map

        normalized.contains("ingenieria") -> Icons.Rounded.Build

        normalized.contains("biologia marina") -> Icons.Rounded.Pool

        else -> Icons.Filled.School
    }
}
