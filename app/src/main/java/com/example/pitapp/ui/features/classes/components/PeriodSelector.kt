package com.example.pitapp.ui.features.classes.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.pitapp.R
import com.example.pitapp.ui.features.home.screens.TimeFilter
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PeriodSelector(
    selectedFilterType: TimeFilter,
    currentSelectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = Locale.getDefault()

    val context = LocalContext.current
    val weekFormatter = remember { DateTimeFormatter.ofPattern("dd MMM", locale) }
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }
    val yearFormatter = remember { DateTimeFormatter.ofPattern("yyyy", locale) }

    val currentPeriodText = remember(selectedFilterType, currentSelectedDate) {
        when (selectedFilterType) {
            TimeFilter.WEEK -> {
                val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
                val startOfWeek = currentSelectedDate.with(firstDayOfWeek)
                val formattedDate = startOfWeek.format(weekFormatter)
                val prefix = context.getString(R.string.week_prefix)
                "$prefix $formattedDate"
            }

            TimeFilter.MONTH -> YearMonth.from(currentSelectedDate)
                .format(monthFormatter)
                .replaceFirstChar { it.titlecase(locale) }

            TimeFilter.YEAR -> currentSelectedDate.format(yearFormatter)
            TimeFilter.NONE -> ""
        }
    }


    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        IconButton(
            onClick = {
                val newDate = when (selectedFilterType) {
                    TimeFilter.WEEK -> currentSelectedDate.minusWeeks(1)
                    TimeFilter.MONTH -> currentSelectedDate.minusMonths(1)
                    TimeFilter.YEAR -> currentSelectedDate.minusYears(1)
                    TimeFilter.NONE -> currentSelectedDate
                }
                onDateChange(newDate)
            }
        ) {
            Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = null)
        }

        Text(
            text = currentPeriodText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = {
                val newDate = when (selectedFilterType) {
                    TimeFilter.WEEK -> currentSelectedDate.plusWeeks(1)
                    TimeFilter.MONTH -> currentSelectedDate.plusMonths(1)
                    TimeFilter.YEAR -> currentSelectedDate.plusYears(1)
                    TimeFilter.NONE -> currentSelectedDate
                }
                onDateChange(newDate)
            }
        ) {
            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}
