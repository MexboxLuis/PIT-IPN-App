package com.example.pitapp.ui.features.scheduling.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import com.example.pitapp.ui.features.scheduling.utils.monthToString
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PeriodInfo(startYear: String, startMonth: String, endYear: String, endMonth: String) {
    val startYearInt = startYear.toIntOrNull() ?: LocalDate.now().year
    val startMonthInt = startMonth.toIntOrNull() ?: LocalDate.now().monthValue
    val endYearInt = endYear.toIntOrNull() ?: LocalDate.now().year
    val endMonthInt = endMonth.toIntOrNull() ?: LocalDate.now().monthValue

    val startMonthName = monthToString(startMonthInt)
    val endMonthName = monthToString(endMonthInt)

    val periodDescription = if (startYearInt == endYearInt) {
        stringResource(
            R.string.period_same_year,
            startMonthName,
            endMonthName,
            startYearInt
        )
    } else {
        stringResource(
            R.string.period_different_year,
            startMonthName,
            startYearInt,
            endMonthName,
            endYearInt
        )
    }

    val now = LocalDate.now()

    val startDate = try {
        LocalDate.of(startYearInt, startMonthInt, 1)
    } catch (e: Exception) {
        now
    }
    val endDate = try {
        YearMonth.of(endYearInt, endMonthInt).atEndOfMonth()
    } catch (e: Exception) {
        now
    }

    val relativeMessage = when {
        now.isBefore(startDate) -> {
            val daysUntilStart = ChronoUnit.DAYS.between(now, startDate)
            if (daysUntilStart < 30) {
                stringResource(R.string.starts_in_days, daysUntilStart)
            } else {
                val monthsUntilStart = ChronoUnit.MONTHS.between(
                    now.withDayOfMonth(1),
                    startDate.withDayOfMonth(1)
                )
                stringResource(R.string.starts_in_months, monthsUntilStart)
            }
        }

        now.isAfter(endDate) -> stringResource(R.string.period_finished)
        else -> {
            val daysUntilEnd = ChronoUnit.DAYS.between(now, endDate)
            if (daysUntilEnd < 30) {
                stringResource(R.string.ends_in_days, daysUntilEnd)
            } else {
                val monthsUntilEnd = ChronoUnit.MONTHS.between(
                    now.withDayOfMonth(1),
                    endDate.withDayOfMonth(1)
                )
                stringResource(R.string.ends_in_months, monthsUntilEnd)
            }
        }
    }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = periodDescription,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 28.dp)
        ) {
            Text(
                text = relativeMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}