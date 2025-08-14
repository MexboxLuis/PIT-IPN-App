package com.example.pitapp.ui.features.classes.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import com.example.pitapp.model.SavedStudent
import com.example.pitapp.ui.features.classes.helpers.generateDistinctComposeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun AttendanceStatsPieChart(students: List<SavedStudent>) {

    val context = LocalContext.current

    if (students.isEmpty()) {
        return
    }

    val (regularCount, irregularCount, programMap) = remember(students) {
        val total = students.size
        val regCount = students.count { it.regular }
        val noRegCount = total - regCount
        val progMap = students
            .groupBy { it.academicProgram.takeIf { it.isNotBlank() } ?: "" }
            .mapValues { it.value.size }
        Triple(regCount, noRegCount, progMap)
    }

    var selectedView by remember { mutableStateOf(StatsViewType.REGULARITY) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.attendance_statistics),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth()
        )

        SegmentedButtonRow(selectedView = selectedView) {
            selectedView = it
        }
        Spacer(Modifier.height(16.dp))

        val entries = remember(selectedView, regularCount, irregularCount, programMap) {
            when (selectedView) {
                StatsViewType.REGULARITY -> {
                    listOfNotNull(
                        if (regularCount > 0) PieEntry(
                            context.getString(R.string.regular_students),
                            regularCount.toFloat()
                        ) else null,
                        if (irregularCount > 0) PieEntry(
                            context.getString(R.string.irregular_students),
                            irregularCount.toFloat()
                        ) else null
                    )
                }

                StatsViewType.PROGRAM -> {
                    programMap.map { PieEntry(it.key, it.value.toFloat()) }
                        .filter { it.value > 0 }
                }
            }
        }
        var highlightedIndex by remember { mutableStateOf<Int?>(null) }
        val cycleIntervalMillis = 3000L
        val highlightDurationMillis = 500L

        LaunchedEffect(key1 = entries) {
            if (entries.isEmpty()) return@LaunchedEffect
            delay(1000L)
            while (isActive) {
                for (index in entries.indices) {
                    if (!isActive) break
                    highlightedIndex = index
                    delay(highlightDurationMillis)

                    if (!isActive) break
                    highlightedIndex = null
                    delay(cycleIntervalMillis - highlightDurationMillis)
                }
                if (isActive) delay(500L)
            }
        }

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.no_data_to_display),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PieChart(
                        entries = entries,
                        colors = generateDistinctComposeColors(entries.size),
                        highlightedIndex = highlightedIndex
                    )
                }
                Spacer(Modifier.width(16.dp))
                Legend(
                    entries = entries,
                    colors = generateDistinctComposeColors(entries.size),
                    modifier = Modifier.weight(1f),
                    highlightedIndex = highlightedIndex
                )
            }
        }
    }
}