package com.example.pitapp.ui.features.calendar.components

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import com.example.pitapp.model.NonWorkingDay
import com.example.pitapp.model.getLocalDate
import com.example.pitapp.model.toTimestampStartOfDay
import com.example.pitapp.datasource.FireStoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("NewApi")
@Composable
fun EditNonWorkingDayTab(
    fireStoreManager: FireStoreManager,
    onFinish: () -> Unit,
    displayedMonth: LocalDate,
    onMonthChange: (LocalDate) -> Unit
) {
    val selectedDate = remember { mutableStateOf<LocalDate?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.getDefault())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var allNonWorkingDays by remember { mutableStateOf<List<NonWorkingDay>>(emptyList()) }

    LaunchedEffect(displayedMonth.year) {
        allNonWorkingDays = fireStoreManager.getNonWorkingDays(displayedMonth.year.toString())
        selectedDate.value = null
    }

    val existingEvent = selectedDate.value?.let { date ->
        allNonWorkingDays.find { it.getLocalDate() == date }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (selectedDate.value == null)
            HeaderCalendar(title = stringResource(id = R.string.select_non_working_day))
        else
            HeaderCalendar(title = stringResource(id = R.string.save_to_confirm))
        Spacer(modifier = Modifier.height(32.dp))

        Calendar(
            displayedMonth = displayedMonth,
            onMonthChange = onMonthChange,
            filter = CalendarFilter.NonWorking,
            nonWorkingDays = allNonWorkingDays.filter { it.getLocalDate().month == displayedMonth.month },
            periods = emptyList(),
            onDateSelected = { date ->
                selectedDate.value = date
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        selectedDate.value?.let { date ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = date.format(dateFormatter),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (existingEvent != null) {
            DetailDateItem(
                label = stringResource(R.string.non_working_day),
                dateText =  selectedDate.value?.format(dateFormatter) ?: "",
                onDelete = {
                    scope.launch {
                        try {
                            fireStoreManager.deleteNonWorkingDay(existingEvent.date)
                            allNonWorkingDays =
                                fireStoreManager.getNonWorkingDays(displayedMonth.year.toString())
                            selectedDate.value = null
                        } catch (_: Exception) {
                        }
                    }
                }
            )
        } else {
            if (selectedDate.value != null) {
                LaunchedEffect(selectedDate.value) {
                    showSnackBar = true
                    progress = 0f
                    for (i in 1..100) {
                        delay(50)
                        progress = i / 100f
                    }
                    selectedDate.value = null
                    showSnackBar = false
                    progress = 0f
                }
            }

            if (showSnackBar) {
                Snackbar(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(
                                R.string.add_non_working_day,
                                selectedDate.value?.format(dateFormatter) ?: ""
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showSnackBar = false
                                    selectedDate.value = null
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(id = R.string.cancel))
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            fireStoreManager.addNonWorkingDay(selectedDate.value!!.toTimestampStartOfDay())
                                            allNonWorkingDays = fireStoreManager.getNonWorkingDays(displayedMonth.year.toString())
                                            showSnackBar = false
                                            selectedDate.value = null
                                            onFinish()
                                        } catch (_: Exception) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.error_saving),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(id = R.string.save_changes))
                            }
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            trackColor = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}