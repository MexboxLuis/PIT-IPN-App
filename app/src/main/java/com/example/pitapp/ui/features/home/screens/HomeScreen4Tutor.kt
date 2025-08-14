package com.example.pitapp.ui.features.home.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.NoBackpack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.datasource.FireStoreManager
import com.example.pitapp.model.SavedClass
import com.example.pitapp.model.SavedStudent
import com.example.pitapp.ui.features.classes.components.AttendanceStatsPieChart
import com.example.pitapp.ui.features.classes.components.InstantClassCard
import com.example.pitapp.ui.features.classes.components.PeriodSelector
import com.example.pitapp.ui.features.classes.components.SortOrder
import com.example.pitapp.ui.features.classes.helpers.generateStudentsCsv
import com.example.pitapp.ui.features.classes.helpers.saveFileToDownloads
import com.example.pitapp.ui.features.classes.helpers.shareFile
import com.example.pitapp.ui.features.home.components.CreateClassSheet
import com.example.pitapp.ui.features.home.components.TutorScaffold
import com.example.pitapp.ui.shared.components.EmptyState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Locale

enum class TimeFilter { NONE, WEEK, MONTH, YEAR }

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen4Tutor(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    val email = authManager.getUserEmail() ?: ""
    val instantClasses = remember { mutableStateOf<List<Pair<String, SavedClass>>>(emptyList()) }
    val savedInstantClasses =
        remember { mutableStateOf<List<Pair<String, SavedClass>>>(emptyList()) }
    val filteredSavedInstantClasses =
        remember { mutableStateOf<List<Pair<String, SavedClass>>>(emptyList()) }
    val studentsCountMap = remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var searchText by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.NEWEST) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val infiniteTransition = rememberInfiniteTransition(label = "blinkingTransition")
    val currentTimeMillis = remember { mutableLongStateOf(System.currentTimeMillis()) }
    val pastStudentsByClassId =
        remember { mutableStateOf<Map<String, List<SavedStudent>>>(emptyMap()) }

    val context = LocalContext.current
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkingAnimation"
    )

    LaunchedEffect(Unit) {
        fireStoreManager.getInstantClasses(email = email) { result ->
            instantClasses.value = result.getOrDefault(emptyList())
        }

        fireStoreManager.getInstantClasses(email = email) { result ->
            result.onSuccess { list ->
                list.forEach { (classId, savedClass) ->
                    fireStoreManager.getStudentsNow(classId) { studentResult ->
                        studentResult.onSuccess { students ->
                            if (students.isNotEmpty() &&
                                savedInstantClasses.value.none { it.first == classId }
                            ) {
                                savedInstantClasses.value += Pair(classId, savedClass)
                                studentsCountMap.value += (classId to students.size)
                                pastStudentsByClassId.value += (classId to students)
                            }
                        }
                    }
                }
            }
        }
        while (true) {
            currentTimeMillis.longValue = System.currentTimeMillis()
            delay(1000L)
        }
    }

    LaunchedEffect(
        searchText,
        sortOrder,
        savedInstantClasses.value,
        instantClasses.value,
        currentTimeMillis.longValue
    ) {
        val visibleIds = instantClasses.value.filter { (_, savedClass) ->
            val classCalendar = Calendar.getInstance().apply { time = savedClass.date.toDate() }
            val currentCalendar =
                Calendar.getInstance().apply { timeInMillis = currentTimeMillis.longValue }
            val isSameDay =
                classCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                        classCalendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR)
            val classHourStartMillis = classCalendar.apply {
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val currentHourStartMillis = currentCalendar.apply {
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            isSameDay && (classHourStartMillis == currentHourStartMillis)
        }.map { it.first }

        filteredSavedInstantClasses.value = savedInstantClasses.value
            .filter { (classId, savedClass) ->
                classId !in visibleIds && (
                        savedClass.subject.contains(searchText, ignoreCase = true) ||
                                savedClass.topic.contains(searchText, ignoreCase = true) ||
                                savedClass.classroom.contains(searchText, ignoreCase = true)
                        )
            }
            .let { filtered ->
                when (sortOrder) {
                    SortOrder.NEWEST -> filtered.sortedByDescending { it.second.date }
                    SortOrder.OLDEST -> filtered.sortedBy { it.second.date }
                }
            }
    }

    val visibleInstantClasses = instantClasses.value.filter { (_, savedClass) ->
        val classCalendar = Calendar.getInstance().apply { time = savedClass.date.toDate() }
        val currentCalendar =
            Calendar.getInstance().apply { timeInMillis = currentTimeMillis.longValue }
        val isSameDay = classCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                classCalendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR)
        val classHourStartMillis = classCalendar.apply {
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val currentHourStartMillis = currentCalendar.apply {
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        isSameDay && (classHourStartMillis == currentHourStartMillis)
    }

    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedTimeFilter by remember { mutableStateOf(TimeFilter.NONE) }
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val consolidatedStudentsForPeriod: List<SavedStudent> by remember(
        selectedTimeFilter,
        selectedDate,
        savedInstantClasses.value,
        pastStudentsByClassId.value
    ) {
        derivedStateOf {

            val studentsInPeriod = mutableListOf<SavedStudent>()
            val locale = Locale.getDefault()

            filteredSavedInstantClasses.value.forEach { (classId, savedClass) ->
                val classDate = try {
                    savedClass.date.toDate()
                } catch (_: Exception) {
                    null
                }
                val classLocalDate = classDate?.toInstant()?.atZone(ZoneId.systemDefault())
                    ?.toLocalDate()

                val isInPeriod = if (classLocalDate == null) {
                    false
                } else {
                    when (selectedTimeFilter) {
                        TimeFilter.NONE -> true
                        TimeFilter.WEEK -> {
                            val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
                            val startOfWeekSelected = selectedDate.with(firstDayOfWeek)
                            val endOfWeekSelected = startOfWeekSelected.plusDays(6)
                            !classLocalDate.isBefore(startOfWeekSelected) && !classLocalDate.isAfter(
                                endOfWeekSelected
                            )
                        }

                        TimeFilter.MONTH -> {
                            YearMonth.from(classLocalDate) == YearMonth.from(selectedDate)
                        }

                        TimeFilter.YEAR -> {
                            classLocalDate.year == selectedDate.year
                        }
                    }
                }


                if (isInPeriod) {
                    pastStudentsByClassId.value[classId]?.let { classStudents ->
                        studentsInPeriod.addAll(classStudents)
                    }
                }
            }
            studentsInPeriod
        }
    }

    TutorScaffold(
        navController = navController,
        fireStoreManager = fireStoreManager,
        onFabClick = filteredSavedInstantClasses.value
            .takeIf { it.isNotEmpty() }
            ?.let {
                { scope.launch { filterSheetState.show() } }
            }
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (instantClasses.value.isNotEmpty() || savedInstantClasses.value.isNotEmpty()) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            label = { Text(text = stringResource(id = R.string.search_my_classes)) },
                            trailingIcon = {
                                if (searchText.isNotEmpty()) {
                                    IconButton(onClick = { searchText = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                sortOrder =
                                    if (sortOrder == SortOrder.NEWEST) SortOrder.OLDEST else SortOrder.NEWEST
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (sortOrder == SortOrder.NEWEST) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    IconButton(onClick = { scope.launch { sheetState.show() } }) {
                        Icon(
                            imageVector = Icons.Default.AddBox,
                            contentDescription = null,
                            modifier = Modifier.size(if (sheetState.isVisible) 64.dp else 32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (visibleInstantClasses.isEmpty() && filteredSavedInstantClasses.value.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon = Icons.Default.NoBackpack,
                            message = stringResource(R.string.no_classes_found)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .align(Alignment.TopEnd)
                                .graphicsLayer(alpha = alpha)
                        )
                    }
                }
            } else {
                if (visibleInstantClasses.isNotEmpty()) {
                    items(visibleInstantClasses) { (classId, savedClass) ->
                        InstantClassCard(
                            savedClass = savedClass,
                            studentsCount = studentsCountMap.value[classId] ?: 0,
                            onClick = { navController.navigate("instantClassDetailsScreen/$classId") }
                        )
                    }
                    if (filteredSavedInstantClasses.value.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable { dropdownExpanded = !dropdownExpanded }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.past_instant_classes),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Icon(
                                        imageVector = if (dropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                }
                            }
                            if (dropdownExpanded) {
                                filteredSavedInstantClasses.value.forEach { (classId, savedClass) ->
                                    InstantClassCard(
                                        savedClass = savedClass,
                                        studentsCount = studentsCountMap.value[classId] ?: 0,
                                        onClick = { navController.navigate("instantClassSummaryScreen/$classId") }
                                    )
                                }
                            }
                        }
                    } else if (searchText.isNotEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.NoBackpack,
                                message = stringResource(R.string.no_filtered_classes)
                            )
                        }
                    }
                } else {
                    if (filteredSavedInstantClasses.value.isNotEmpty()) {
                        items(filteredSavedInstantClasses.value) { (classId, savedClass) ->
                            InstantClassCard(
                                savedClass = savedClass,
                                studentsCount = studentsCountMap.value[classId] ?: 0,
                                onClick = { navController.navigate("instantClassSummaryScreen/$classId") }
                            )
                        }
                    } else {
                        item {
                            EmptyState(
                                icon = Icons.Default.NoBackpack,
                                message = stringResource(R.string.no_classes_found)
                            )
                        }
                    }
                }
            }
        }
        if (filterSheetState.isVisible) {
            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch {
                        filterSheetState.hide()
                    }
                },
                sheetState = filterSheetState,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = selectedDate.format(
                                DateTimeFormatter.ofPattern("dd/MMM/yyyy")
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val filtersAreActive by remember(selectedTimeFilter, selectedDate) {
                            derivedStateOf {
                                selectedTimeFilter != TimeFilter.NONE ||
                                        (selectedTimeFilter != TimeFilter.NONE && selectedDate != LocalDate.now())
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        if (filtersAreActive) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        selectedTimeFilter = TimeFilter.NONE
                                        selectedDate = LocalDate.now()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterListOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val timeFilterOptions = listOf(
                            TimeFilter.WEEK to Icons.Default.DateRange,
                            TimeFilter.MONTH to Icons.Default.CalendarToday,
                            TimeFilter.YEAR to Icons.Default.Event,
                            TimeFilter.NONE to Icons.Default.AllInbox
                        )

                        timeFilterOptions.forEachIndexed { index, (filterType, icon) ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = timeFilterOptions.size
                                ),
                                onClick = {
                                    selectedTimeFilter = filterType
                                },
                                selected = (filterType == selectedTimeFilter),
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null
                                    )
                                },
                                label = {
                                    Text(
                                        text = when (filterType) {
                                            TimeFilter.WEEK -> stringResource(id = R.string.filter_week)
                                            TimeFilter.MONTH -> stringResource(id = R.string.filter_month)
                                            TimeFilter.YEAR -> stringResource(id = R.string.filter_year)
                                            TimeFilter.NONE -> stringResource(id = R.string.filter_all)
                                        }
                                    )
                                }
                            )
                        }
                    }


                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedVisibility(visible = selectedTimeFilter != TimeFilter.NONE) {
                        PeriodSelector(
                            selectedFilterType = selectedTimeFilter,
                            currentSelectedDate = selectedDate,
                            onDateChange = { newDate -> selectedDate = newDate }
                        )
                    }

                    AttendanceStatsPieChart(consolidatedStudentsForPeriod)

                    if (consolidatedStudentsForPeriod.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                val classesWithStudents =
                                    filteredSavedInstantClasses.value.mapNotNull { (classId, savedClass) ->
                                        val studs = pastStudentsByClassId.value[classId].orEmpty()
                                        if (studs.isNotEmpty()) savedClass to studs else null
                                    }

                                val csv = generateStudentsCsv(classesWithStudents, context)
                                shareFile(context, csv)
                                saveFileToDownloads(context, csv, "text/csv")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(id = R.string.download_csv_summary))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Icon(Icons.Default.Downloading, null)
                        }

                        Spacer(Modifier.height(16.dp))
                    }

                }
            }
        }
    }

    CreateClassSheet(
        sheetState = sheetState,
        scope = scope,
        onStartNewClassClick = { navController.navigate("startInstantClassScreen") },
        onGoToExistingClassClick = { classId ->
            navController.navigate("instantClassDetailsScreen/$classId")
        },
        onScheduleClick = { navController.navigate("generateScheduleScreen") },
        instantClasses = instantClasses.value
    )
}