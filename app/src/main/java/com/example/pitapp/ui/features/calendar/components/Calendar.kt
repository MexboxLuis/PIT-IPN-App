package com.example.pitapp.ui.features.calendar.components

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pitapp.model.NonWorkingDay
import com.example.pitapp.model.Period
import com.example.pitapp.ui.features.calendar.helpers.getPeriodForDate
import com.example.pitapp.ui.features.calendar.helpers.isDateNonWorking
import com.example.pitapp.ui.shared.formatting.dayOfWeekToString
import com.example.pitapp.ui.shared.formatting.monthToString
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Calendar(
    displayedMonth: LocalDate,
    onMonthChange: (LocalDate) -> Unit,
    filter: CalendarFilter,
    nonWorkingDays: List<NonWorkingDay>,
    periods: List<Period>,
    onDateSelected: (LocalDate) -> Unit,
    selectedStartDate: LocalDate? = null,
    selectedEndDate: LocalDate? = null
) {

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { onMonthChange(displayedMonth.minusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            AnimatedContent(
                targetState = displayedMonth,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "Calendar Transition"
            ) { month ->
                Text(
                    text = "${monthToString(month.month.value)} ${month.year}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
            IconButton(onClick = { onMonthChange(displayedMonth.plusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            (1..7).forEach { day ->
                val dayName = dayOfWeekToString(day)
                Text(
                    text = dayName.take(3),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        AnimatedContent(
            targetState = displayedMonth,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "Grid Transition"
        ) { month ->

            val yearMonth = YearMonth.of(month.year, month.month)
            val daysInMonth = yearMonth.lengthOfMonth()
            val firstDayOffset = month.withDayOfMonth(1).dayOfWeek.let {
                if (it == DayOfWeek.SUNDAY) 6 else it.value - 1
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.padding(8.dp)
            ) {
                items(firstDayOffset) { Box(modifier = Modifier.size(48.dp)) {} }
                items(daysInMonth) { index ->
                    val date = month.withDayOfMonth(index + 1)
                    val isNonWorking = isDateNonWorking(date, nonWorkingDays)
                    val periodForDate = getPeriodForDate(date, periods)
                    val isPeriod = periodForDate != null
                    val shouldHighlight = when (filter) {
                        CalendarFilter.Combined -> isNonWorking || isPeriod
                        CalendarFilter.NonWorking -> isNonWorking
                        CalendarFilter.Period -> isPeriod
                    }

                    val isWithinSelectedRange = selectedStartDate != null && selectedEndDate != null &&
                            !date.isBefore(selectedStartDate) && !date.isAfter(selectedEndDate)

                    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
                    val isSelected = date == selectedDate

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(4.dp)
                            .background(
                                color = when {
                                    isWithinSelectedRange -> MaterialTheme.colorScheme.secondary
                                    isPeriod -> MaterialTheme.colorScheme.inversePrimary
                                    isSelected -> MaterialTheme.colorScheme.secondary
                                    shouldHighlight -> when (filter) {
                                        CalendarFilter.NonWorking -> MaterialTheme.colorScheme.surfaceVariant
                                        CalendarFilter.Combined -> MaterialTheme.colorScheme.surfaceContainerHigh
                                        else -> Color.Transparent
                                    }
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .clickable {
                                if (isWeekend && CalendarFilter.NonWorking == filter) return@clickable
                                else {
                                    selectedDate = date
                                    onDateSelected(date)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = when {
                                isWithinSelectedRange -> MaterialTheme.colorScheme.onSecondary
                                isSelected -> MaterialTheme.colorScheme.onSecondary
                                isPeriod -> MaterialTheme.colorScheme.secondary
                                shouldHighlight -> when (filter) {
                                    CalendarFilter.NonWorking -> MaterialTheme.colorScheme.onSurfaceVariant
                                    CalendarFilter.Combined -> MaterialTheme.colorScheme.onSurface
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}