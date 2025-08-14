package com.example.pitapp.ui.features.classes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.ui.features.classes.screens.TutorWithClasses
import kotlinx.coroutines.launch

@Composable
fun TutorClassesRow(
    tutorData: TutorWithClasses,
    navController: NavHostController,
    onSummarizeClick: () -> Unit
) {
    val lazyRowState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(tutorData.filteredPastClasses) {
        if (tutorData.filteredPastClasses.isNotEmpty()) {
            scope.launch {
                lazyRowState.scrollToItem(0)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tutorData.tutorInfo.name + " " + tutorData.tutorInfo.surname
                        .split(" ")
                        .firstOrNull()
                        .orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (tutorData.tutorInfo.name.isNotBlank()) {
                    Text(
                        text = tutorData.tutorInfo.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (tutorData.allPastClassesWithStudents.isNotEmpty()) {
                OutlinedButton(onClick = onSummarizeClick) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.summarize_button))
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        if (tutorData.filteredPastClasses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_classes_match_filter_for_tutor),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyRow(
                state = lazyRowState,
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tutorData.filteredPastClasses, key = { it.first }) { (classId, savedClass) ->
                    Box(modifier = Modifier.width(385.dp)) {
                        InstantClassCard(
                            savedClass = savedClass,
                            studentsCount = tutorData.studentsCountMap[classId] ?: 0,
                            onClick = { navController.navigate("instantClassSummaryScreen/$classId") }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}
