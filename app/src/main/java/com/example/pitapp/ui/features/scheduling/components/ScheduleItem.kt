package com.example.pitapp.ui.features.scheduling.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.example.pitapp.model.Schedule

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleItem(
    schedule: Schedule,
    onEdit: () -> Unit,
    onApprove: () -> Unit,
    onDisapprove: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f)
    val elevation by animateDpAsState(targetValue = if (expanded) 12.dp else 4.dp)
    val backgroundColor by animateColorAsState(
        if (schedule.approved) MaterialTheme.colorScheme.onSecondary
        else MaterialTheme.colorScheme.inverseOnSurface
    )
    val contentColor = MaterialTheme.colorScheme.secondary

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDisapproveDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .alpha(if (expanded) 1f else 0.8f)
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(20.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TutorInfo(
                    icon = Icons.Default.Person,
                    tutorName = schedule.tutorEmail.substringBefore("@")
                        .replaceFirstChar { it.uppercase() },
                    subject = schedule.subject
                )
                StatusBadge(isApproved = schedule.approved)
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top,
                ) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    ClassroomInfo(schedule.salonId)
                    PeriodInfo(
                        schedule.startYear.toString(),
                        schedule.startMonth.toString(),
                        schedule.endYear.toString(),
                        schedule.endMonth.toString()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SessionsMiniCalendar(sessions = schedule.sessions)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (schedule.approved) {
                            IconButton(onClick = { showDisapproveDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.DoNotDisturbOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = onApprove) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .rotate(rotationState)
            )
        }
    }

    if (showDeleteDialog) {
        DeleteScheduleDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            }
        )
    }

    if (showDisapproveDialog) {
        DisapproveScheduleDialog(
            onDismiss = { showDisapproveDialog = false },
            onConfirm = {
                onDisapprove()
                showDisapproveDialog = false
            }
        )
    }
}