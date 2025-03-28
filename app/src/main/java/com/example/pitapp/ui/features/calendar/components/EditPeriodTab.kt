package com.example.pitapp.ui.features.calendar.components

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.pitapp.model.Period
import com.example.pitapp.ui.features.calendar.helpers.getEndLocalDate
import com.example.pitapp.ui.features.calendar.helpers.getStartLocalDate
import com.example.pitapp.ui.features.calendar.helpers.toTimestampStartOfDay
import com.example.pitapp.datasource.FireStoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditPeriodTab(
    fireStoreManager: FireStoreManager,
    onFinish: () -> Unit,
    initialStartDate: LocalDate? = null,
    initialEndDate: LocalDate? = null,
    displayedMonth: LocalDate,
    onMonthChange: (LocalDate) -> Unit
) {
    val startDate = remember { mutableStateOf(initialStartDate) }
    val endDate = remember { mutableStateOf(initialEndDate) }
    var showSnackBar by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.getDefault())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var allPeriods by remember { mutableStateOf<List<Period>>(emptyList()) }

    LaunchedEffect(displayedMonth.year) {
        allPeriods = fireStoreManager.getPeriods(displayedMonth.year.toString())
    }

    fun isDateWithinAnyPeriod(
        date: LocalDate,
        periods: List<Period>,
        excluding: Period? = null
    ): Boolean {
        return periods.any { period ->
            period != excluding && !date.isBefore(period.getStartLocalDate()) && !date.isAfter(
                period.getEndLocalDate()
            )
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            startDate.value == null ->
                HeaderCalendar(title = stringResource(id = R.string.select_date_range))

            endDate.value == null ->
                HeaderCalendar(title = stringResource(id = R.string.select_end_range))

            else ->
                HeaderCalendar(title = stringResource(id = R.string.save_to_confirm))
        }
        Spacer(modifier = Modifier.height(32.dp))

        Calendar(
            displayedMonth = displayedMonth,
            onMonthChange = onMonthChange,
            filter = CalendarFilter.Period,
            nonWorkingDays = emptyList(),
            periods = allPeriods,
            onDateSelected = { date ->
                // 1. Check if the selected date is part of an existing period
                val currentEditingPeriod =
                    allPeriods.find { isDateWithinAnyPeriod(date, listOf(it)) }

                if (startDate.value == null) {
                    // 2. If no start date is selected:
                    if (currentEditingPeriod != null) {
                        // 2.1. If the date is within a period, select the whole period
                        startDate.value = currentEditingPeriod.getStartLocalDate()
                        endDate.value = currentEditingPeriod.getEndLocalDate()
                    } else {
                        // 2.2. Otherwise, set the selected date as the start date
                        startDate.value = date
                    }
                } else if (endDate.value == null) {
                    // 3. If a start date is selected, but no end date:
                    if (date == startDate.value) {
                        //  If the selected date is the *same* as the start date, do nothing.
                        return@Calendar // Exit early. Important!
                    } else if (currentEditingPeriod != null) {
                        //3.1 If date is within a period. Select the period
                        startDate.value = currentEditingPeriod.getStartLocalDate()
                        endDate.value = currentEditingPeriod.getEndLocalDate()
                    } else if (date.isBefore(startDate.value!!)) {
                        //  If the selected date is *before* the start date, set it as the new start Date
                        startDate.value = date
                    } else if (isDateWithinAnyPeriod(date, allPeriods)) {
                        //3.4  If the selected date is within any other existing period, show toast

                    } else if (allPeriods.any {
                            !date.isBefore(it.getStartLocalDate()) && !startDate.value!!.isAfter(
                                it.getEndLocalDate()
                            )
                        }) {
                        // 3.5 The user selects a date range that overlaps an existing period
                        Toast.makeText(
                            context,
                            context.getString(R.string.date_range_overlaps),
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {
                        // 3.2. Otherwise, set the selected date as the end date
                        endDate.value = date
                        showSnackBar = true
                    }

                } else {
                    // 4. If both start and end dates are selected, reset:
                    if (currentEditingPeriod != null) {
                        //4.1 If the date is within a period, select the whole period
                        startDate.value = currentEditingPeriod.getStartLocalDate()
                        endDate.value = currentEditingPeriod.getEndLocalDate()
                    } else {
                        //4.2 Reset, and select the date.
                        startDate.value = date
                        endDate.value = null
                        showSnackBar = false
                        progress = 0f
                    }

                }
            },
            selectedStartDate = startDate.value,
            selectedEndDate = endDate.value
        )

        startDate.value?.let { start ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = start.format(dateFormatter),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                endDate.value?.let { end ->
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowRightAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = end.format(dateFormatter),
                        style = MaterialTheme.typography.titleSmall,
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
        }


        val selectedPeriod = if (startDate.value != null && endDate.value != null) {
            // Find a period in *allPeriods* that matches *exactly* the selected start and end dates.
            allPeriods.find {
                it.getStartLocalDate() == startDate.value && it.getEndLocalDate() == endDate.value
            }
        } else null

        selectedPeriod?.let { period ->
            showSnackBar = false
            DetailDateItem(
                label = stringResource(id = R.string.period_details),
                dateText = "${
                    period.getStartLocalDate().format(dateFormatter)
                } - ${period.getEndLocalDate().format(dateFormatter)}",
                onDelete = {
                    scope.launch {
                        try {
                            fireStoreManager.deletePeriod(period)
                            allPeriods = fireStoreManager.getPeriods(displayedMonth.year.toString())
                            startDate.value = null
                            endDate.value = null
                        } catch (_: Exception) {
                        }
                    }
                }
            )
        }


        if (showSnackBar && startDate.value != null && endDate.value != null) {
            Snackbar(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${
                            stringResource(
                                id = R.string.add_period,
                                startDate.value!!.format(dateFormatter)
                            )
                        } " +
                                "${stringResource(id = R.string.to)} " +
                                endDate.value!!.format(dateFormatter),
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
                                startDate.value = null
                                endDate.value = null
                                showSnackBar = false
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
                                        fireStoreManager.addPeriod(
                                            year = startDate.value!!.year.toString(),
                                            startDate = startDate.value!!.toTimestampStartOfDay(),
                                            endDate = endDate.value!!.toTimestampStartOfDay()
                                        )
                                        allPeriods =
                                            fireStoreManager.getPeriods(displayedMonth.year.toString())
                                    } catch (_: Exception) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.error_saving),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } finally {
                                        startDate.value = null
                                        endDate.value = null
                                        showSnackBar = false
                                        onFinish()
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
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

            LaunchedEffect(showSnackBar, startDate.value, endDate.value) {
                if (showSnackBar && startDate.value != null && endDate.value != null) {
                    progress = 0f
                    for (i in 1..100) {
                        delay(50)
                        progress = i / 100f
                    }
                    startDate.value = null
                    endDate.value = null
                    showSnackBar = false
                    progress = 0f
                }
            }
        }
    }
}

