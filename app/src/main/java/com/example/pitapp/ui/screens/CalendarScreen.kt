package com.example.pitapp.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.ui.components.HeaderCalendar
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(id = R.string.calendar_title)
    ) {
        var selectedTab by rememberSaveable { mutableIntStateOf(0) }
        val tabTitles = listOf(
            stringResource(id = R.string.tab_title_days),
            stringResource(id = R.string.tab_title_periods)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(text = title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> NonWorkingDaysTab(fireStoreManager = fireStoreManager)
                1 -> PeriodsTab(fireStoreManager = fireStoreManager)
            }
        }
    }
}


@SuppressLint("NewApi")
@Composable
fun NonWorkingDaysTab(fireStoreManager: FireStoreManager) {
    val selectedDate = remember { mutableStateOf<LocalDate?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.getDefault())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(showSnackBar) {
        if (showSnackBar) {
            for (i in 1..100) {
                delay(50)
                progress = i / 100f
            }
            showSnackBar = false
            progress = 0f
            selectedDate.value = null
        }
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
            onDateSelected = { date ->
                if (!showSnackBar) {
                    selectedDate.value = date
                    showSnackBar = true
                }
            },
            startDate = null, endDate = null,
            fireStoreManager = fireStoreManager
        )
        Spacer(modifier = Modifier.height(16.dp))

        selectedDate.value?.let { date ->
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = date.format(dateFormatter),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showSnackBar) {
            Snackbar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.message_non_working_day),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer

                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                showSnackBar = false
                                selectedDate.value = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.cancel),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val selectedDateValue = selectedDate.value
                                        if (selectedDateValue == null) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.error_saving),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@launch
                                        }

                                        val firebaseTimestamp = Timestamp(
                                            selectedDateValue.atStartOfDay(ZoneId.systemDefault())
                                                .toInstant().epochSecond, 0
                                        )

                                        fireStoreManager.addNonWorkingDay(firebaseTimestamp)

                                        selectedDate.value = null
                                        showSnackBar = false
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.error_saving),
                                            Toast.LENGTH_SHORT
                                        ).show()
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
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun PeriodsTab(fireStoreManager: FireStoreManager) {
    val startDate = remember { mutableStateOf<LocalDate?>(null) }
    val endDate = remember { mutableStateOf<LocalDate?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val snackBarMessage = remember { mutableStateOf("") }
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.getDefault())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(showSnackBar) {
        if (showSnackBar) {
            for (i in 1..100) {
                delay(50)
                progress = i / 100f
            }
            showSnackBar = false
            startDate.value = null
            endDate.value = null
            snackBarMessage.value = ""
            progress = 0f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (startDate.value == null)
            HeaderCalendar(title = stringResource(id = R.string.select_date_range))
        else if (endDate.value == null)
            HeaderCalendar(title = stringResource(id = R.string.select_end_range))
        else
            HeaderCalendar(title = stringResource(id = R.string.save_to_confirm))

        Spacer(modifier = Modifier.height(32.dp))

        Calendar(
            onDateSelected = { date ->
                if (startDate.value == null) {
                    startDate.value = date
                } else if (endDate.value == null) {
                    if (date.isAfter(startDate.value)) {
                        endDate.value = date
                        snackBarMessage.value =
                            context.getString(
                                R.string.confirm_range_message,
                                startDate.value,
                                endDate.value
                            )
                        showSnackBar = true
                    } else {
                        snackBarMessage.value = context.getString(R.string.end_date_error)
                        showSnackBar = true
                    }
                } else {
                    startDate.value = date
                    endDate.value = null
                }
            },
            startDate = startDate.value,
            endDate = endDate.value,
            fireStoreManager = fireStoreManager
        )


        startDate.value?.let {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it.format(dateFormatter),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

        }

        endDate.value?.let {
            Text(
                text = stringResource(id = R.string.to),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = it.format(dateFormatter),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showSnackBar) {
            if (endDate.value != null)
                Snackbar(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ) {

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = snackBarMessage.value,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer

                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
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
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.cancel),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Button(
                                onClick = {
                                    if (startDate.value != null && endDate.value != null) {
                                        val startTimestamp = Timestamp(
                                            Date.from(
                                                startDate.value!!.atStartOfDay(ZoneId.systemDefault())
                                                    .toInstant()
                                            )
                                        )
                                        val endTimestamp = Timestamp(
                                            Date.from(
                                                endDate.value!!.plusDays(1)
                                                    .atStartOfDay(ZoneId.systemDefault())
                                                    .toInstant()
                                            )
                                        )

                                        scope.launch {
                                            try {
                                                fireStoreManager.addPeriod(
                                                    year = startDate.value!!.year.toString(),
                                                    startTimestamp,
                                                    endTimestamp
                                                )

                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.error_saving),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } finally {
                                                startDate.value = null
                                                endDate.value = null
                                                showSnackBar = false
                                            }
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
            else {
                Toast.makeText(
                    context,
                    context.getString(R.string.end_date_error),
                    Toast.LENGTH_SHORT
                ).show()
                showSnackBar = false
                startDate.value = null
                endDate.value = null
                snackBarMessage.value = ""
            }
        }


    }
}

@SuppressLint("NewApi")
@Composable
fun Calendar(
    onDateSelected: (LocalDate) -> Unit,
    startDate: LocalDate?,
    endDate: LocalDate?,
    fireStoreManager: FireStoreManager
) {
    val currentDate = remember { LocalDate.now() }
    val displayedMonth = remember { mutableStateOf(currentDate) }
    val selectedDate = remember { mutableStateOf<LocalDate?>(null) }
    val context = LocalContext.current
    val nonWorkingDays = remember { mutableStateOf<List<LocalDate>>(emptyList()) }
    val periods = remember { mutableStateOf<List<LocalDate>>(emptyList()) }
    var listenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }


    LaunchedEffect(displayedMonth.value.year) {
        // Obtener NonWorkingDays
        try {
            val days = fireStoreManager.getNonWorkingDays(displayedMonth.value.year.toString())
            nonWorkingDays.value = days.map {
                it.date.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            }
        } catch (exception: Exception) {
            Toast.makeText(
                context,
                "Error fetching non-working days: ${exception.message}",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Obtener Periods y convertirlos a List<LocalDate>
        try {
            val fetchedPeriods = fireStoreManager.getPeriods(displayedMonth.value.year.toString())
            periods.value = fetchedPeriods.flatMap { it.toLocalDateList() }
        } catch (exception: Exception) {
            Toast.makeText(
                context,
                "Error fetching periods: ${exception.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            listenerRegistration?.remove()
        }
    }


    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { displayedMonth.value = displayedMonth.value.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }

            AnimatedContent(
                targetState = displayedMonth.value,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                }, label = "Calendar Transition"
            ) { month ->
                Text(
                    text = "${month.month.name} ${month.year}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }

            IconButton(onClick = { displayedMonth.value = displayedMonth.value.plusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf(
                context.getString(R.string.day_mon),
                context.getString(R.string.day_tue),
                context.getString(R.string.day_wed),
                context.getString(R.string.day_thu),
                context.getString(R.string.day_fri),
                context.getString(R.string.day_sat),
                context.getString(R.string.day_sun)
            ).forEach { day ->
                Text(
                    text = day.take(3),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(4.dp),
                )
            }

        }

        val yearMonth = YearMonth.of(displayedMonth.value.year, displayedMonth.value.month)
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDayOfMonth = displayedMonth.value.withDayOfMonth(1).dayOfWeek.let {
            if (it == DayOfWeek.SUNDAY) 6 else it.value - 1
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.padding(8.dp)
        ) {

            items(firstDayOfMonth) { Box(modifier = Modifier.size(48.dp)) {} }

            items(daysInMonth) { day ->
                val date = displayedMonth.value.withDayOfMonth(day + 1)
                val isSelected = selectedDate.value == date
                val isSelectedRange = startDate != null && endDate != null &&
                        (date.isEqual(startDate) || date.isEqual(endDate) || (date.isAfter(startDate) && date.isBefore(
                            endDate
                        )))
                val isNonWorking = isNonWorkingDay(date, nonWorkingDays.value)
                val isWithinPeriod = isWithinPeriod(date, periods.value)

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(4.dp)
                        .background(
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isSelectedRange || isWithinPeriod -> MaterialTheme.colorScheme.inverseSurface
                                isNonWorking -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.surface
                            },
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .alpha(
                            if (isNonWorking || isWithinPeriod) 1f else 0.5f
                        )
                        .clickable {
                            selectedDate.value = date
                            onDateSelected(date)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${day + 1}",
                        color = if (isSelected || isSelectedRange || isWithinPeriod) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}



fun isNonWorkingDay(date: LocalDate, nonWorkingDays: List<LocalDate>): Boolean {
    return nonWorkingDays.contains(date)
}

fun isWithinPeriod(date: LocalDate, periods: List<LocalDate>): Boolean {
    return periods.contains(date)
}



@SuppressLint("NewApi")
fun Period.toLocalDateList(): List<LocalDate> {
    val start = this.startDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val end = this.endDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val dates = mutableListOf<LocalDate>()
    var currentDate = start
    while (!currentDate.isAfter(end)) {
        dates.add(currentDate)
        currentDate = currentDate.plusDays(1)
    }
    return dates
}


data class NonWorkingDay(
    val date: Timestamp = Timestamp.now(),
)

data class Period(
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
)
