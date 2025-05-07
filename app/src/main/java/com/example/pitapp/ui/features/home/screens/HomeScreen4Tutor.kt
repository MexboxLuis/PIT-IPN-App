package com.example.pitapp.ui.features.home.screens

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import androidx.compose.ui.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NoBackpack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.ui.shared.components.BackScaffold
import com.example.pitapp.ui.features.home.components.CreateClassSheet
import com.example.pitapp.ui.shared.components.EmptyState
import com.example.pitapp.ui.features.home.components.TutorScaffold
import com.example.pitapp.ui.features.classes.components.SortOrder
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.datasource.FireStoreManager
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import com.example.pitapp.model.SavedClass
import com.example.pitapp.model.SavedStudent
import com.example.pitapp.ui.features.classes.components.InstantClassCard
import com.example.pitapp.ui.features.classes.components.StudentRow
import kotlinx.coroutines.isActive
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import kotlin.math.cos
import kotlin.math.sin

enum class TimeFilter { NONE, WEEK, MONTH, YEAR }

@RequiresApi(Build.VERSION_CODES.O)
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

            savedInstantClasses.value.forEach { (classId, savedClass) ->
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
        onFabClick = { scope.launch { filterSheetState.show() } }
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
                    Text(
                        "Filtrar Asistencias Por ${selectedTimeFilter.name} - ${
                            selectedDate.format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            )
                        }",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val timeFilterOptions = TimeFilter.entries.toList()

                        timeFilterOptions.forEachIndexed { index, filterType ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = timeFilterOptions.size
                                ),
                                onClick = {
                                    selectedTimeFilter = filterType
                                },
                                selected = (filterType == selectedTimeFilter)
                            ) {
                                Text(
                                    when (filterType) {
                                        TimeFilter.WEEK -> "Semana"
                                        TimeFilter.MONTH -> "Mes"
                                        TimeFilter.YEAR -> "Año"
                                        TimeFilter.NONE -> "Todos"
                                    }
                                )
                            }
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

                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                selectedTimeFilter = TimeFilter.NONE
                                selectedDate = LocalDate.now()
                            }
                        }
                    ) {
                        Text(text = "Eliminar Filtros")
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PeriodSelector(
    selectedFilterType: TimeFilter,
    currentSelectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = Locale.getDefault()

    val weekFormatter = remember { DateTimeFormatter.ofPattern("'Semana del' dd MMM", locale) }
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }
    val yearFormatter = remember { DateTimeFormatter.ofPattern("yyyy", locale) }

    val currentPeriodText = remember(selectedFilterType, currentSelectedDate) {
        when (selectedFilterType) {
            TimeFilter.WEEK -> {
                val firstDayOfWeek =
                    WeekFields.of(locale).firstDayOfWeek
                val startOfWeek = currentSelectedDate.with(firstDayOfWeek)
                startOfWeek.format(weekFormatter)
            }

            TimeFilter.MONTH -> YearMonth.from(currentSelectedDate).format(monthFormatter)
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
            }) {
            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun InstantClassSummaryScreen(
    classId: String,
    authManager: AuthManager,
    navController: NavHostController,
    fireStoreManager: FireStoreManager
) {
    val savedClass = remember { mutableStateOf<SavedClass?>(null) }
    val students = remember { mutableStateOf<List<SavedStudent>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(classId) {
        isLoading.value = true
        var classLoaded = false
        var studentsLoaded = false

        val checkCompletion = {
            if (classLoaded && studentsLoaded) {
                isLoading.value = false
            }
        }

        fireStoreManager.getInstantClassDetails(classId) { result ->
            result.onSuccess { retrievedClass ->
                savedClass.value = retrievedClass
            }.onFailure {
                savedClass.value = null
            }
            classLoaded = true
            checkCompletion()
        }

        fireStoreManager.getStudentsNow(classId) { studentResult ->
            studentResult.onSuccess { studentList ->
                students.value = studentList.sortedBy { it.name }
            }.onFailure {
                students.value = emptyList()
            }
            studentsLoaded = true
            checkCompletion()
        }
    }


    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = "Detalles de la Clase"
    ) {

        when {
            isLoading.value -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            savedClass.value != null -> {
                val currentClass = savedClass.value!!
                val currentStudents = students.value

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ClassSummaryCard(savedClass = currentClass, enabled = true, onClick = {
                            val csvFile =
                                generateStudentsCsv(currentClass, currentStudents, context)
                            shareFile(context, csvFile)
                            saveFileToDownloads(context, csvFile, "text/csv")
                        })
                    }

                    if (currentStudents.isNotEmpty()) {
                        item {
                            AttendanceStatsCard(students = currentStudents)
                        }
                    }
                    item {
                        Text(
                            text = "Alumnos Inscritos (${currentStudents.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (currentStudents.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay alumnos inscritos en esta clase.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(currentStudents) { student ->
                            StudentRow(student = student)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontró información para esta clase.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ClassSummaryCard(
    savedClass: SavedClass,
    enabled: Boolean,
    onClick: () -> Unit
) {

    val formattedDateTime = remember(savedClass.date) {
        try {
            val sdf = SimpleDateFormat("HH:mm dd-MMM-yyyy", Locale.getDefault())
            sdf.format(savedClass.date.toDate())
        } catch (_: Exception) {
            savedClass.date.toDate().toString()
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = savedClass.subject,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))

            DetailSummaryCardRow(
                icon = Icons.Default.Email,
                label = "Tutor",
                value = savedClass.tutorEmail
            )
            DetailSummaryCardRow(
                icon = Icons.Default.LocationOn,
                label = "Salón",
                value = savedClass.classroom
            )
            DetailSummaryCardRow(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                label = "Tópico",
                value = savedClass.topic
            )
            DetailSummaryCardRow(
                icon = Icons.Default.CalendarToday,
                label = "Fecha",
                value = formattedDateTime
            )
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Descargar lista de asistencia", textAlign = TextAlign.Center)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Icon(imageVector = Icons.Default.Downloading, contentDescription = null)
            }
        }
    }
}


@Composable
fun DetailSummaryCardRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AttendanceStatsCard(students: List<SavedStudent>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        AttendanceStatsPieChart(students = students)
    }
}

@Composable
fun AttendanceStatsPieChart(students: List<SavedStudent>) {
    if (students.isEmpty()) {
        return
    }

    val (regularCount, irregularCount, programMap) = remember(students) {
        val total = students.size
        val regCount = students.count { it.regular }
        val noRegCount = total - regCount
        val progMap = students
            .groupBy { it.academicProgram.takeIf { it.isNotBlank() } ?: "Desconocido" }
            .mapValues { it.value.size }
        Triple(regCount, noRegCount, progMap)
    }

    var selectedView by remember { mutableStateOf(StatsViewType.REGULARITY) }



    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Estadísticas de Asistencia",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth()
        )

        SegmentedButtonRow(selectedView = selectedView) {
            selectedView = it
        }
        Spacer(Modifier.height(16.dp))

        val entries = remember(selectedView, regularCount, irregularCount, programMap) {
            when (selectedView) {
                StatsViewType.REGULARITY -> {
                    listOfNotNull(
                        if (regularCount > 0) PieEntry(
                            "Regulares",
                            regularCount.toFloat()
                        ) else null,
                        if (irregularCount > 0) PieEntry(
                            "Irregulares",
                            irregularCount.toFloat()
                        ) else null
                    )
                }

                StatsViewType.PROGRAM -> {
                    programMap.map { PieEntry(it.key, it.value.toFloat()) }
                        .filter { it.value > 0 }
                }
            }
        }
        var highlightedIndex by remember { mutableStateOf<Int?>(null) }
        val cycleIntervalMillis = 3000L
        val highlightDurationMillis = 500L

        LaunchedEffect(key1 = entries) {
            if (entries.isEmpty()) return@LaunchedEffect
            delay(1000L)
            while (isActive) {
                for (index in entries.indices) {
                    if (!isActive) break
                    highlightedIndex = index
                    delay(highlightDurationMillis)

                    if (!isActive) break
                    highlightedIndex = null
                    delay(cycleIntervalMillis - highlightDurationMillis)
                }
                if (isActive) delay(500L)
            }
        }

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay datos para mostrar en esta vista.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PieChart(
                        entries = entries,
                        colors = generateDistinctComposeColors(entries.size),
                        highlightedIndex = highlightedIndex
                    )
                }
                Spacer(Modifier.width(16.dp))
                Legend(
                    entries = entries,
                    colors = generateDistinctComposeColors(entries.size),
                    modifier = Modifier.weight(1f),
                    highlightedIndex = highlightedIndex
                )
            }
        }
    }
}

@Composable
fun SegmentedButtonRow(
    selectedView: StatsViewType,
    onSelectionChange: (StatsViewType) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        StatsViewType.entries.forEachIndexed { index, viewType ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = StatsViewType.entries.size
                ),
                onClick = { onSelectionChange(viewType) },
                selected = selectedView == viewType,
                label = { Text(if (viewType == StatsViewType.REGULARITY) "Status" else "Programa") }
            )
        }
    }
}

@Composable
fun Legend(
    entries: List<PieEntry>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    highlightedIndex: Int? = null
) {
    fun Color.highlighted(): Color {
        return lerp(this, Color.White, 0.3f)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val totalValue = entries.sumOf { it.value.toDouble() }.toFloat()
        entries.forEachIndexed { index, entry ->
            val percentage = if (totalValue > 0) (entry.value / totalValue * 100) else 0f
            val isHighlighted = index == highlightedIndex

            val textColor = if (isHighlighted) {
                MaterialTheme.colorScheme.primary
            } else {
                LocalContentColor.current
            }
            val fontWeight = if (isHighlighted) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
            val boxColor = colors.getOrElse(index) { Color.Gray }
            val finalBoxColor = if (isHighlighted) {
                boxColor.highlighted()
            } else {
                boxColor
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = finalBoxColor,
                            shape = MaterialTheme.shapes.small
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${entry.label}: ${entry.value.toInt()} (${"%.1f".format(percentage)}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                    fontWeight = fontWeight
                )
            }
        }
    }
}


data class PieEntry(val label: String, val value: Float)

enum class StatsViewType { REGULARITY, PROGRAM }

@Composable
fun generateDistinctComposeColors(count: Int): List<Color> {
    val baseColors = listOf(
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.error,
        Color(0xFFF44336),
        Color(0xFFE91E63),
        Color(0xFF9C27B0),
        Color(0xFF673AB7),
        Color(0xFF3F51B5),
        Color(0xFF2196F3),
        Color(0xFF03A9F4),
        Color(0xFF00BCD4),
        Color(0xFF009688),
        Color(0xFF4CAF50),
        Color(0xFF8BC34A),
    )
    if (count <= 0) return emptyList()
    return List(count) { baseColors[it % baseColors.size] }
}

@Composable
fun PieChart(entries: List<PieEntry>, colors: List<Color>, highlightedIndex: Int? = null) {
    if (entries.isEmpty() || colors.isEmpty()) return
    val total = entries.sumOf { it.value.toDouble() }.toFloat()
    if (total <= 0f) return

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(key1 = entries) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
            )
        )
    }

    val bounceDistancePx = with(LocalDensity.current) { 4.dp.toPx() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        var startAngle = -90f

        entries.forEachIndexed { index, entry ->
            val targetSweepAngle = (entry.value / total) * 360f

            val animatedSweepAngle = targetSweepAngle * animationProgress.value

            if (animatedSweepAngle > 0f) {
                val baseColor = colors.getOrElse(index) { Color.LightGray }
                val currentColor = baseColor

                val currentTopLeft: Offset
                if (index == highlightedIndex) {
                    val middleAngleDegrees = startAngle + animatedSweepAngle / 2f
                    val middleAngleRadians = Math.toRadians(middleAngleDegrees.toDouble()).toFloat()

                    val offsetX = cos(middleAngleRadians) * bounceDistancePx
                    val offsetY = sin(middleAngleRadians) * bounceDistancePx

                    currentTopLeft = Offset(offsetX, offsetY)
                } else {
                    currentTopLeft = Offset.Zero
                }

                drawArc(
                    color = currentColor,
                    startAngle = startAngle,
                    sweepAngle = animatedSweepAngle,
                    useCenter = true,
                    topLeft = currentTopLeft,
                    size = size
                )
            }
            startAngle += animatedSweepAngle
        }
    }
}


fun generateStudentsCsv(
    savedClass: SavedClass,
    students: List<SavedStudent>,
    context: Context
): File {

    val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH-mm", Locale.getDefault())
    val formattedDate = dateFormat.format(savedClass.date.toDate())

    val fileName = "asistenca_${savedClass.tutorEmail.substringBefore("@")}_${
        savedClass.subject.substringBefore(" ")
    }_${savedClass.topic.substringBefore(" ")}_$formattedDate.csv"

    val file = File(context.filesDir, fileName)

    csvWriter {
        charset = StandardCharsets.UTF_8.name()

    }.open(file, append = false) {

        writeRow(
            listOf(
                "Fecha",
                "Nombre del alumno asesorado",
                "Boleta",
                "Horario",
                "Tema Visto",
                "Programa Educativo",
                "Correo Electrónico",
                "Regular o irregular"
            )
        )

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:00", Locale.getDefault())
        val date = dateFormat.format(savedClass.date.toDate())


        for (student in students) {
            val startTime = try {
                timeFormat.format(savedClass.date.toDate())
            } catch (_: Exception) {
                context.getString(R.string.unknown_error)
            }

            val endTime = try {
                val calendar = Calendar.getInstance().apply {
                    time = savedClass.date.toDate()
                    add(Calendar.HOUR_OF_DAY, 1)
                }
                timeFormat.format(calendar.time)
            } catch (_: Exception) {
                context.getString(R.string.unknown_error)
            }


            val schedule = "$startTime - $endTime"

            writeRow(
                listOf(
                    date,
                    student.name,
                    student.studentId,
                    schedule,
                    savedClass.topic,
                    student.academicProgram,
                    student.email,
                    if (student.regular) "Regular" else "Irregular"
                )
            )
        }
    }
    return file
}


fun shareFile(context: Context, file: File) {
    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir CSV"))
}


@RequiresApi(Build.VERSION_CODES.Q)
fun saveFileToDownloads(
    context: Context,
    sourceFile: File,
    mimeType: String = "text/csv"
): Uri? {
    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, sourceFile.name)
        put(MediaStore.Downloads.MIME_TYPE, mimeType)
        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        put(MediaStore.Downloads.IS_PENDING, 1)
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
    uri?.let { contentUri ->
        resolver.openOutputStream(contentUri)?.use { out ->
            sourceFile.inputStream().use { input -> input.copyTo(out) }
        }
        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(contentUri, values, null, null)
    }
    notifyDownloadComplete(context, uri!!)
    return uri
}


private fun notifyDownloadComplete(context: Context, uri: Uri) {
    val channelId = "downloads_channel"
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Descargas de la app",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notificaciones de archivos descargados"
        }
        manager.createNotificationChannel(channel)
    }

    val fileName = getFileNameFromUri(context, uri)
        ?: "archivo descargado"
    val userFriendlyPath =
        "${Environment.DIRECTORY_DOWNLOADS}/$fileName"

    val openIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, context.contentResolver.getType(uri))
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    }
    val pi = PendingIntent.getActivity(
        context, 0, openIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.pit_logo)
        .setContentTitle("🎉 Descarga completada")
        .setContentText("Archivo ${fileName}")
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText("Ruta: ${userFriendlyPath}\n\nToca para abrir o comparte desde aquí.")
        )
        .setColor(ContextCompat.getColor(context, R.color.white))
        .setColorized(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pi)
        .addAction(
            R.drawable.pit_logo,
            "Compartir",
            PendingIntent.getActivity(
                context, 1,
                Intent.createChooser(
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uri)
                        type = context.contentResolver.getType(uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    "Compartir archivo"
                ),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
    manager.notify(1001, notification.build())
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var fileName: String? = null
    if (uri.scheme == "content") {
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        } catch (_: Exception) {

        } finally {
            cursor?.close()
        }
    }

    if (fileName == null) {
        val path = uri.path
        if (path != null) {
            fileName = path
            val cut = fileName.lastIndexOf('/')
            if (cut != -1) {
                fileName = fileName.substring(cut + 1)
            }
        }
    }
    return fileName
}