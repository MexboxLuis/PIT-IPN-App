package com.example.pitapp.ui.features.classes.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import com.example.pitapp.model.SavedStudent

@Composable
fun StudentRow(student: SavedStudent, modifier: Modifier = Modifier) {
    var isExpanded = remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = { isExpanded.value = !isExpanded.value }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                StudentStatusBadge(isRegular = student.regular)
            }

            Spacer(Modifier.height(4.dp))

            DetailStatusItem(
                icon = Icons.Filled.Badge,
                label = stringResource(R.string.student_id),
                value = student.studentId
            )

            AnimatedVisibility(visible = isExpanded.value) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 12.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    DetailStatusItem(
                        icon = Icons.Filled.School,
                        label = stringResource(R.string.academic_program),
                        value = student.academicProgram
                    )
                    Spacer(Modifier.height(8.dp))
                    DetailStatusItem(
                        icon = Icons.Filled.Email,
                        label = stringResource(R.string.email),
                        value = student.email
                    )
                }
            }
        }
    }
}



