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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import com.example.pitapp.ui.features.classes.helpers.generateDistinctComposeColors
import com.example.pitapp.ui.features.classes.screens.TutorWithClasses
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun PeerTutoringSummaryChart(
    peerTutorsWithClasses: List<TutorWithClasses>
) {
    val context = LocalContext.current

    val validPeerTutorsData = remember(peerTutorsWithClasses) {
        peerTutorsWithClasses.filter {
            !it.tutorInfo.academicProgram.isNullOrBlank() && !it.tutorInfo.studentId.isNullOrBlank()
        }
    }

    if (validPeerTutorsData.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.no_peer_tutoring_classes_to_summarize),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    var selectedView by remember { mutableStateOf(PeerTutoringSummaryViewType.BY_TUTOR) }

    val totalClassesByTutor = remember(validPeerTutorsData) {
        validPeerTutorsData.associate { tutorData ->
            val tutorName = tutorData.tutorInfo.name.ifBlank { tutorData.tutorInfo.email }
            tutorName to tutorData.allPastClassesWithStudents.size.toFloat()
        }.filter { it.value > 0 }
    }

    val totalClassesByProgram = remember(validPeerTutorsData) {
        val programClassCount = mutableMapOf<String, Float>()
        validPeerTutorsData.forEach { tutorData ->

            val program = tutorData.tutorInfo.academicProgram
                ?: context.getString(R.string.unknown_program) // NUEVO STRING
            val classCount = tutorData.allPastClassesWithStudents.size.toFloat()
            programClassCount[program] = (programClassCount[program] ?: 0f) + classCount
        }
        programClassCount.filter { it.value > 0 }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            PeerTutoringSummaryViewType.entries.forEachIndexed { index, viewType ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = PeerTutoringSummaryViewType.entries.size
                    ),
                    onClick = { selectedView = viewType },
                    selected = selectedView == viewType,
                    label = {
                        Text(
                            text = when (viewType) {
                                PeerTutoringSummaryViewType.BY_TUTOR -> stringResource(R.string.view_by_tutor)
                                PeerTutoringSummaryViewType.BY_ACADEMIC_PROGRAM -> stringResource(R.string.view_by_program)
                            }
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = when (viewType) {
                                PeerTutoringSummaryViewType.BY_TUTOR -> Icons.Default.Person
                                PeerTutoringSummaryViewType.BY_ACADEMIC_PROGRAM -> Icons.Default.School
                            },
                            contentDescription = null
                        )
                    }
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        val entries = remember(selectedView, totalClassesByTutor, totalClassesByProgram) {
            when (selectedView) {
                PeerTutoringSummaryViewType.BY_TUTOR -> {
                    totalClassesByTutor.map { PieEntry(it.key, it.value) }
                }

                PeerTutoringSummaryViewType.BY_ACADEMIC_PROGRAM -> {
                    totalClassesByProgram.map { PieEntry(it.key, it.value) }
                }
            }
        }


        var highlightedIndex by remember { mutableStateOf<Int?>(null) }
        val cycleIntervalMillis = 3000L
        val highlightDurationMillis = 500L

        LaunchedEffect(key1 = entries) {
            if (entries.isEmpty()) {
                highlightedIndex = null
                return@LaunchedEffect
            }
            highlightedIndex = null
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
                if (entries.size <= 1 && isActive) {
                    delay(cycleIntervalMillis)
                } else if (isActive) {
                    delay(500L)
                }
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
                    text = stringResource(id = R.string.no_data_for_selected_view),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
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
