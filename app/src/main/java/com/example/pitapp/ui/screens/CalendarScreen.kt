package com.example.pitapp.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.ui.components.EditNonWorkingDayTab
import com.example.pitapp.ui.components.HeaderCalendar
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var editingPeriod by remember { mutableStateOf<Period?>(null) }
    var currentMonth by remember { mutableStateOf(LocalDate.now()) }
    var nonWorkingDays by remember { mutableStateOf<List<NonWorkingDay>>(emptyList()) }
    var periods by remember { mutableStateOf<List<Period>>(emptyList()) }


    LaunchedEffect(currentMonth.year, isEditing) {
        if (!isEditing) {
            nonWorkingDays = fireStoreManager.getNonWorkingDays(currentMonth.year.toString())
            periods = fireStoreManager.getPeriods(currentMonth.year.toString())
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(id = R.string.calendar_title)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = isEditing,
                transitionSpec = {
                    slideInVertically(
                        initialOffsetY = { fullHeight -> -fullHeight },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) togetherWith slideOutVertically(
                        targetOffsetY = { fullHeight -> -fullHeight },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }, label = ""
            ) { editing ->
                if (editing) {
                    EditCalendarEntriesScreen(
                        fireStoreManager = fireStoreManager,
                        editingPeriod = editingPeriod,
                        displayedMonth = currentMonth,
                        onMonthChange = { currentMonth = it },
                        onFinish = {
                            isEditing = false
                            editingPeriod = null
                        }
                    )
                } else {
                    GeneralCalendarView(
                        displayedMonth = currentMonth,
                        onMonthChange = { currentMonth = it },
                        nonWorkingDays = nonWorkingDays,
                        periods = periods
                    )
                }
            }

            OutlinedButton(
                onClick = { isEditing = !isEditing },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                val iconSize by animateDpAsState(
                    targetValue = if (isEditing) 32.dp else 24.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                Icon(
                    imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
            }

        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GeneralCalendarView(
    displayedMonth: LocalDate,
    onMonthChange: (LocalDate) -> Unit,
    nonWorkingDays: List<NonWorkingDay>,
    periods: List<Period>
) {

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.getDefault())


    Column(modifier = Modifier.fillMaxSize()) {
        Calendar(
            displayedMonth = displayedMonth,
            onMonthChange = onMonthChange,
            filter = CalendarFilter.Combined,
            nonWorkingDays = nonWorkingDays,
            periods = periods,
            onDateSelected = { date ->
                selectedDate = date
            }
        )

        selectedDate?.let { date ->
            val isNonWorkingDay = nonWorkingDays.any { it.getLocalDate() == date }
            val period = periods.find {
                val startDate = it.getStartLocalDate()
                val endDate = it.getEndLocalDate()
                !date.isBefore(startDate) && !date.isAfter(endDate)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.selected_day),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (isNonWorkingDay) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(id = R.string.non_working_day),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Text(
                        text = date.format(dateFormatter),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Weekend,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.weekend),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else if (!isNonWorkingDay && period == null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.no_events),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    period?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.period_prefix) + " " +
                                        it.getStartLocalDate().format(dateFormatter) + " - " +
                                        it.getEndLocalDate().format(dateFormatter),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditCalendarEntriesScreen(
    fireStoreManager: FireStoreManager,
    editingPeriod: Period? = null,
    displayedMonth: LocalDate,
    onMonthChange: (LocalDate) -> Unit,
    onFinish: () -> Unit
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
            0 -> EditNonWorkingDayTab(
                fireStoreManager = fireStoreManager,
                onFinish = onFinish,
                displayedMonth = displayedMonth,
                onMonthChange = onMonthChange
            )

            1 -> EditPeriodTab(
                fireStoreManager = fireStoreManager,
                onFinish = onFinish,
                initialStartDate = editingPeriod?.getStartLocalDate(),
                initialEndDate = editingPeriod?.getEndLocalDate(),
                displayedMonth = displayedMonth,
                onMonthChange = onMonthChange
            )
        }
    }
}


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
            DetailItem(
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


@Composable
fun DetailItem(
    label: String,
    dateText: String,
    onDelete: () -> Unit
) {
    var isDeleting by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Label,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    OutlinedButton(
                        onClick = {
                            isDeleting = true
                            onDelete()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(id = R.string.delete))
                    }
                }
            }
        }
    }
}

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
                    text = "${month.month.name} ${month.year}",
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
            listOf(
                R.string.day_mon,
                R.string.day_tue,
                R.string.day_wed,
                R.string.day_thu,
                R.string.day_fri,
                R.string.day_sat,
                R.string.day_sun
            ).forEach { dayRes ->
                val dayName = stringResource(id = dayRes)
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

data class NonWorkingDay(
    val date: Timestamp = Timestamp.now()
)

data class Period(
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now()
)

@SuppressLint("NewApi")
fun LocalDate.toTimestampStartOfDay(): Timestamp {
    val instant = this.atStartOfDay(ZoneId.systemDefault()).toInstant()
    return Timestamp(instant.epochSecond, 0)
}

@SuppressLint("NewApi")
fun NonWorkingDay.getLocalDate(): LocalDate =
    this.date.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

@SuppressLint("NewApi")
fun Period.getStartLocalDate(): LocalDate =
    this.startDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

@SuppressLint("NewApi")
fun Period.getEndLocalDate(): LocalDate =
    this.endDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

fun isDateNonWorking(date: LocalDate, nonWorkingDays: List<NonWorkingDay>): Boolean =
    nonWorkingDays.any { it.getLocalDate() == date }

@RequiresApi(Build.VERSION_CODES.O)
fun getPeriodForDate(date: LocalDate, periods: List<Period>): Period? =
    periods.find { period ->
        val start = period.getStartLocalDate()
        val end = period.getEndLocalDate()
        !date.isBefore(start) && !date.isAfter(end)
    }

enum class CalendarFilter { Combined, NonWorking, Period }