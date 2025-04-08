package com.example.pitapp.ui.features.home.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.pitapp.model.SavedClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClassSheet(
    sheetState: SheetState,
    scope: CoroutineScope,
    onStartNewClassClick: () -> Unit,
    onGoToExistingClassClick: (classId: String) -> Unit,
    onScheduleClick: () -> Unit,
    instantClasses: List<Pair<String, SavedClass>>,
) {

    val context = LocalContext.current

    fun hasClassInProgress(classes: List<Pair<String, SavedClass>>): Pair<Boolean, String?> {
        val currentCalendar = Calendar.getInstance()
        val currentYear = currentCalendar.get(Calendar.YEAR)
        val currentDayOfYear = currentCalendar.get(Calendar.DAY_OF_YEAR)
        val currentHourStartMillis = currentCalendar.apply {
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        for ((id, savedClass) in classes) {
            val classCalendar = Calendar.getInstance().apply { time = savedClass.date.toDate() }
            val classYear = classCalendar.get(Calendar.YEAR)
            val classDayOfYear = classCalendar.get(Calendar.DAY_OF_YEAR)
            val classHourStartMillis = classCalendar.apply {
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val isSameDay = classYear == currentYear && classDayOfYear == currentDayOfYear
            val isSameHour = classHourStartMillis == currentHourStartMillis

            if (isSameDay && isSameHour) {
                return Pair(true, id)
            }
        }
        return Pair(false, null)
    }


    if (sheetState.isVisible) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                }
            }
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.headline_sheet),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ClassInstantIcon(
                            iconTitle = stringResource(id = R.string.create_class),
                            icon = Icons.Default.Schedule,
                            onClick = {
                                scope.launch {
                                    val (inProgress, classId) = hasClassInProgress(instantClasses)
                                    sheetState.hide()
                                    if (inProgress && classId != null) {
                                        onGoToExistingClassClick(classId)
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.class_in_progress),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        onStartNewClassClick()
                                    }
                                }
                            }
                        )

                        ClassInstantIcon(
                            iconTitle = stringResource(id = R.string.schedule_class),
                            icon = Icons.Default.CalendarMonth,
                            onClick = {
                                scope.launch {
                                    sheetState.hide()
                                    onScheduleClick()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}