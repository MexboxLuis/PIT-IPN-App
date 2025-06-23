package com.example.pitapp.ui.features.classes.screens

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.datasource.FireStoreManager
import com.example.pitapp.model.SavedClass
import com.example.pitapp.model.SavedStudent
import com.example.pitapp.model.UserData
import com.example.pitapp.ui.features.classes.components.InstantClassCard
import com.example.pitapp.ui.features.classes.components.SortOrder
import com.example.pitapp.ui.features.home.screens.*
import com.example.pitapp.ui.shared.components.BackScaffold
import com.example.pitapp.ui.shared.components.EmptyState
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Locale

data class TutorWithClasses(
    val tutorInfo: UserData,
    var filteredPastClasses: List<Pair<String, SavedClass>>,
    val allPastClassesWithStudents: List<Pair<String, SavedClass>>,
    val studentsByClassId: Map<String, List<SavedStudent>>,
    val studentsCountMap: Map<String, Int>
)

enum class TutorTab { PEER_TUTORING, ADVISORIES }

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorClassesScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    var isLoading by remember { mutableStateOf(true) }
    val allTutorsWithClasses = remember { mutableStateListOf<TutorWithClasses>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var searchText by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.NEWEST) }

    var selectedTab by remember { mutableStateOf(TutorTab.PEER_TUTORING) }

    val summarySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSummarySheetForTutorEmail by remember { mutableStateOf<String?>(null) }
    var showGlobalSummarySheet by remember { mutableStateOf(false) }
    val globalSummarySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTimeFilterForSummary by remember { mutableStateOf(TimeFilter.NONE) }
    var selectedDateForSummary by remember { mutableStateOf(LocalDate.now()) }

    val tutorForSummary: TutorWithClasses? by remember {
        derivedStateOf {
            allTutorsWithClasses.find { it.tutorInfo.email == showSummarySheetForTutorEmail }
        }
    }

    val consolidatedStudentsForSummarySheet: List<SavedStudent> by remember(
        tutorForSummary,
        selectedTimeFilterForSummary,
        selectedDateForSummary
    ) {
        derivedStateOf {
            val tutor = tutorForSummary ?: return@derivedStateOf emptyList()
            val studentsInPeriod = mutableListOf<SavedStudent>()
            tutor.allPastClassesWithStudents.forEach { (classId, savedClass) ->
                val classLocalDate = savedClass.date.toDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                val isInPeriod = isDateInPeriod(
                    classLocalDate,
                    selectedDateForSummary,
                    selectedTimeFilterForSummary,
                    Locale.getDefault()
                )

                if (isInPeriod) {
                    tutor.studentsByClassId[classId]?.let { classStudents ->
                        studentsInPeriod.addAll(classStudents)
                    }
                }
            }
            studentsInPeriod
        }
    }


    LaunchedEffect(Unit) {
        isLoading = true
        fireStoreManager.getAllTutorsWithPermissionOne { tutorsResult ->
            tutorsResult.onSuccess { tutorsList ->
                if (tutorsList.isEmpty()) {
                    isLoading = false
                    return@onSuccess
                }
                val tempTutorData = mutableListOf<TutorWithClasses>()
                var tutorsProcessed = 0

                tutorsList.forEach { tutor ->
                    fireStoreManager.getInstantClasses(email = tutor.email) { classesResult ->
                        classesResult.onSuccess { classesPairs ->
                            val classesWithStudents = mutableListOf<Pair<String, SavedClass>>()
                            val studentsMap = mutableMapOf<String, List<SavedStudent>>()
                            val studentCounts = mutableMapOf<String, Int>()
                            var classesToProcess = classesPairs.size

                            if (classesPairs.isEmpty()) {
                                tutorsProcessed++
                                if (tutorsProcessed == tutorsList.size) {
                                    allTutorsWithClasses.addAll(tempTutorData.filter { it.allPastClassesWithStudents.isNotEmpty() })
                                    isLoading = false
                                }
                                return@onSuccess
                            }

                            classesPairs.forEach { (classId, savedClass) ->
                                fireStoreManager.getStudentsNow(classId) { studentResult ->
                                    studentResult.onSuccess { students ->
                                        if (students.isNotEmpty()) {
                                            classesWithStudents.add(Pair(classId, savedClass))
                                            studentsMap[classId] = students
                                            studentCounts[classId] = students.size
                                        }
                                    }
                                    classesToProcess--
                                    if (classesToProcess == 0) {
                                        if (classesWithStudents.isNotEmpty()) {
                                            tempTutorData.add(
                                                TutorWithClasses(
                                                    tutorInfo = tutor,
                                                    filteredPastClasses = classesWithStudents.sortedByDescending { it.second.date },
                                                    allPastClassesWithStudents = classesWithStudents.toList(),
                                                    studentsByClassId = studentsMap.toMap(),
                                                    studentsCountMap = studentCounts.toMap()
                                                )
                                            )
                                        }
                                        tutorsProcessed++
                                        if (tutorsProcessed == tutorsList.size) {
                                            allTutorsWithClasses.addAll(tempTutorData.filter { it.allPastClassesWithStudents.isNotEmpty() })
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        }.onFailure {
                            tutorsProcessed++
                            if (tutorsProcessed == tutorsList.size) {
                                allTutorsWithClasses.addAll(tempTutorData.filter { it.allPastClassesWithStudents.isNotEmpty() })
                                isLoading = false
                            }
                        }
                    }
                }
            }.onFailure {
                isLoading = false
            }
        }
    }

    LaunchedEffect(searchText, sortOrder, allTutorsWithClasses.toList()) {
        if (allTutorsWithClasses.isEmpty()) return@LaunchedEffect

        allTutorsWithClasses.forEachIndexed { index, tutorData ->
            val newlyFiltered = tutorData.allPastClassesWithStudents
                .filter { (_, savedClass) ->
                    savedClass.subject.contains(searchText, ignoreCase = true) ||
                            savedClass.topic.contains(searchText, ignoreCase = true) ||
                            savedClass.classroom.contains(searchText, ignoreCase = true)
                }
                .let { filtered ->
                    when (sortOrder) {
                        SortOrder.NEWEST -> filtered.sortedByDescending { it.second.date }
                        SortOrder.OLDEST -> filtered.sortedBy { it.second.date }
                    }
                }
            allTutorsWithClasses[index] = tutorData.copy(filteredPastClasses = newlyFiltered)
        }
    }

    val peerTutors = remember(allTutorsWithClasses.toList()) {
        allTutorsWithClasses.filter {
            !it.tutorInfo.studentId.isNullOrBlank() && !it.tutorInfo.academicProgram.isNullOrBlank()
        }
    }

    val advisors = remember(allTutorsWithClasses.toList()) {
        allTutorsWithClasses.filter {
            it.tutorInfo.studentId.isNullOrBlank() && it.tutorInfo.academicProgram.isNullOrBlank()
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(id = R.string.all_tutor_past_classes)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {


                    TabRow(selectedTabIndex = selectedTab.ordinal) {
                        Tab(
                            selected = selectedTab == TutorTab.PEER_TUTORING,
                            onClick = { selectedTab = TutorTab.PEER_TUTORING },
                            text = { Text(stringResource(R.string.peer_tutoring_tab)) },
                            icon = {
                                Icon(
                                    Icons.Default.Group,
                                    null,
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == TutorTab.ADVISORIES,
                            onClick = { selectedTab = TutorTab.ADVISORIES },
                            text = { Text(stringResource(R.string.advisories_tab)) },
                            icon = {
                                Icon(
                                    Icons.Default.CoPresent,
                                    null,
                                )
                            }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            label = { Text(text = stringResource(id = R.string.search_classes)) },
                            trailingIcon = {
                                if (searchText.isNotEmpty()) {
                                    IconButton(onClick = { searchText = "" }) {
                                        Icon(
                                            Icons.Default.Close,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else {
                                    Icon(
                                        Icons.Default.Search,
                                        null,
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
                    }

                    val currentListToDisplay = when (selectedTab) {
                        TutorTab.PEER_TUTORING -> peerTutors
                        TutorTab.ADVISORIES -> advisors
                    }

                    if (currentListToDisplay.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.GroupOff,
                            message = stringResource(
                                if (selectedTab == TutorTab.PEER_TUTORING) R.string.no_peer_tutors_with_past_classes
                                else R.string.no_advisors_with_past_classes
                            )
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(currentListToDisplay, key = { it.tutorInfo.email }) { tutorData ->
                                TutorClassesRow(
                                    tutorData = tutorData,
                                    navController = navController,
                                    onSummarizeClick = {
                                        selectedTimeFilterForSummary = TimeFilter.NONE
                                        selectedDateForSummary = LocalDate.now()
                                        showSummarySheetForTutorEmail = tutorData.tutorInfo.email
                                        scope.launch { summarySheetState.show() }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (showSummarySheetForTutorEmail != null && tutorForSummary != null) {
                ModalBottomSheet(
                    onDismissRequest = {
                        scope.launch {
                            summarySheetState.hide()
                            showSummarySheetForTutorEmail = null
                        }
                    },
                    sheetState = summarySheetState,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(
                                R.string.summary_for_tutor,
                                tutorForSummary!!.tutorInfo.name.ifBlank { tutorForSummary!!.tutorInfo.email }),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = selectedDateForSummary.format(
                                    DateTimeFormatter.ofPattern(
                                        "dd/MMM/yyyy"
                                    )
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val filtersAreActive by remember(
                                selectedTimeFilterForSummary,
                                selectedDateForSummary
                            ) {
                                derivedStateOf {
                                    selectedTimeFilterForSummary != TimeFilter.NONE ||
                                            (selectedTimeFilterForSummary != TimeFilter.NONE && selectedDateForSummary != LocalDate.now())
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            if (filtersAreActive) {
                                IconButton(
                                    onClick = {
                                        selectedTimeFilterForSummary = TimeFilter.NONE
                                        selectedDateForSummary = LocalDate.now()
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.FilterListOff,
                                        null,
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
                                    onClick = { selectedTimeFilterForSummary = filterType },
                                    selected = (filterType == selectedTimeFilterForSummary),
                                    icon = { Icon(icon, null) },
                                    label = {
                                        Text(
                                            when (filterType) {
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
                        Spacer(modifier = Modifier.height(16.dp))

                        AnimatedVisibility(visible = selectedTimeFilterForSummary != TimeFilter.NONE) {
                            PeriodSelector(
                                selectedFilterType = selectedTimeFilterForSummary,
                                currentSelectedDate = selectedDateForSummary,
                                onDateChange = { newDate -> selectedDateForSummary = newDate }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        AttendanceStatsPieChart(consolidatedStudentsForSummarySheet)

                        if (consolidatedStudentsForSummarySheet.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    val classesToExport =
                                        tutorForSummary!!.allPastClassesWithStudents
                                            .filter { (_, savedClass) ->
                                                val classLocalDate =
                                                    savedClass.date.toDate().toInstant()
                                                        .atZone(ZoneId.systemDefault())
                                                        .toLocalDate()
                                                isDateInPeriod(
                                                    classLocalDate,
                                                    selectedDateForSummary,
                                                    selectedTimeFilterForSummary,
                                                    Locale.getDefault()
                                                )
                                            }
                                            .mapNotNull { (classId, savedClass) ->
                                                val studs =
                                                    tutorForSummary!!.studentsByClassId[classId].orEmpty()
                                                if (studs.isNotEmpty()) savedClass to studs else null
                                            }

                                    if (classesToExport.isNotEmpty()) {
                                        val csv = generateStudentsCsv(classesToExport, context)
                                        shareFile(context, csv)
                                        saveFileToDownloads(context, csv, "text/csv")
                                    }
                                    scope.launch {
                                        summarySheetState.hide()
                                        showSummarySheetForTutorEmail = null
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = stringResource(id = R.string.download_csv_summary))
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Icon(Icons.Default.Downloading, null)
                            }
                        }
                        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
                }
            }
            val peerTutorsForGlobalSummary = remember(allTutorsWithClasses.toList()) {
                allTutorsWithClasses.filter { tutorWithClasses ->

                    val tutorInfo = tutorWithClasses.tutorInfo
                    !tutorInfo.studentId.isNullOrBlank() && !tutorInfo.academicProgram.isNullOrBlank()
                }
            }

            if (peerTutorsForGlobalSummary.any { it.allPastClassesWithStudents.isNotEmpty() } && !isLoading && selectedTab == TutorTab.PEER_TUTORING) {
                SummarizePeerTutoringButton(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    onClick = {
                        showGlobalSummarySheet = true
                    }
                )
            }

            if (showGlobalSummarySheet) {
                ModalBottomSheet(
                    onDismissRequest = { showGlobalSummarySheet = false },
                    sheetState = globalSummarySheetState,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.global_summary_peer_tutoring_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        PeerTutoringSummaryChart(peerTutorsWithClasses = peerTutorsForGlobalSummary)
                        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))

                        OutlinedButton(
                            onClick = {
                                val csvFile = generateGlobalPeerTutoringSummaryCsv(
                                    peerTutorsForGlobalSummary,
                                    context
                                )
                                shareFile(context, csvFile)
                                saveFileToDownloads(context, csvFile, "text/csv")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(R.string.download_global_peer_tutoring_summary))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Icon(Icons.Default.Downloading, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun generateGlobalPeerTutoringSummaryCsv(
    peerTutorsData: List<TutorWithClasses>,
    context: Context
): File {

    val hasClassesToExport = peerTutorsData.any { it.allPastClassesWithStudents.isNotEmpty() }
    require(hasClassesToExport) {
        context.getString(R.string.error_no_peer_tutoring_classes_to_export)
    }

    val timeStamp = SimpleDateFormat("yyyy_MM_dd_HH-mm", Locale.getDefault())
        .format(System.currentTimeMillis())
    @Suppress("SpellCheckingInspection")
    val fileName = "resumen_global_tutorias_pares_$timeStamp.csv"
    val file = File(context.filesDir, fileName)

    csvWriter { charset = StandardCharsets.UTF_8.name() }
        .open(file, append = false) {
            writeRow(
                @Suppress("SpellCheckingInspection")
                listOf(
                    "Fecha",
                    "Nombre",
                    "Tutoría entre Pares",
                    "Horario",
                    "Tema Visto",
                    "N° Alumnos Atendidos",
                    "Lugar de Atención"
                )
            )

            val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val hourFmt = SimpleDateFormat("HH:00", Locale.getDefault())

            peerTutorsData.forEach { tutorData ->
                val tutorInfo = tutorData.tutorInfo
                tutorData.allPastClassesWithStudents.forEach { (classId, savedClass) ->
                    val studentCount = tutorData.studentsCountMap[classId] ?: 0

                    val classDate = savedClass.date.toDate()
                    val dateStr = dateFmt.format(classDate)
                    val tutorFullName = "${tutorInfo.name} ${tutorInfo.surname}".trim()

                    val startHour = hourFmt.format(classDate)
                    val endHourCalendar = Calendar.getInstance().apply {
                        time = classDate
                        add(Calendar.HOUR_OF_DAY, 1)
                    }
                    val endHour = hourFmt.format(endHourCalendar.time)
                    val schedule = "$startHour - $endHour"

                    writeRow(
                        listOf(
                            dateStr,
                            tutorFullName,
                            savedClass.subject,
                            schedule,
                            savedClass.topic,
                            studentCount.toString(),
                            savedClass.classroom
                        )
                    )
                }
            }
        }
    return file
}

@Composable
fun SummarizePeerTutoringButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Text(stringResource(R.string.peer_tutoring_summary))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Icon(
            Icons.Filled.PieChart,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
    }
}

enum class PeerTutoringSummaryViewType {
    BY_TUTOR,
    BY_ACADEMIC_PROGRAM
}

@Composable
fun PeerTutoringSummaryChart(
    peerTutorsWithClasses: List<TutorWithClasses>
) {
    val context = LocalContext.current

    val validPeerTutorsData = remember(peerTutorsWithClasses) {
        peerTutorsWithClasses.filter {
            !it.tutorInfo.academicProgram.isNullOrBlank() && !it.tutorInfo.studentId.isNullOrBlank()
        }
    }

    if (validPeerTutorsData.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.no_peer_tutoring_classes_to_summarize),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    var selectedView by remember { mutableStateOf(PeerTutoringSummaryViewType.BY_TUTOR) }

    val totalClassesByTutor = remember(validPeerTutorsData) {
        validPeerTutorsData.associate { tutorData ->
            val tutorName = tutorData.tutorInfo.name.ifBlank { tutorData.tutorInfo.email }
            tutorName to tutorData.allPastClassesWithStudents.size.toFloat()
        }.filter { it.value > 0 }
    }

    val totalClassesByProgram = remember(validPeerTutorsData) {
        val programClassCount = mutableMapOf<String, Float>()
        validPeerTutorsData.forEach { tutorData ->

            val program = tutorData.tutorInfo.academicProgram
                ?: context.getString(R.string.unknown_program) // NUEVO STRING
            val classCount = tutorData.allPastClassesWithStudents.size.toFloat()
            programClassCount[program] = (programClassCount[program] ?: 0f) + classCount
        }
        programClassCount.filter { it.value > 0 }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            PeerTutoringSummaryViewType.entries.forEachIndexed { index, viewType ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = PeerTutoringSummaryViewType.entries.size
                    ),
                    onClick = { selectedView = viewType },
                    selected = selectedView == viewType,
                    label = {
                        Text(
                            text = when (viewType) {
                                PeerTutoringSummaryViewType.BY_TUTOR -> stringResource(R.string.view_by_tutor)
                                PeerTutoringSummaryViewType.BY_ACADEMIC_PROGRAM -> stringResource(R.string.view_by_program)
                            }
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = when (viewType) {
                                PeerTutoringSummaryViewType.BY_TUTOR -> Icons.Default.Person
                                PeerTutoringSummaryViewType.BY_ACADEMIC_PROGRAM -> Icons.Default.School
                            },
                            contentDescription = null
                        )
                    }
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        val entries = remember(selectedView, totalClassesByTutor, totalClassesByProgram) {
            when (selectedView) {
                PeerTutoringSummaryViewType.BY_TUTOR -> {
                    totalClassesByTutor.map { PieEntry(it.key, it.value) }
                }

                PeerTutoringSummaryViewType.BY_ACADEMIC_PROGRAM -> {
                    totalClassesByProgram.map { PieEntry(it.key, it.value) }
                }
            }
        }


        var highlightedIndex by remember { mutableStateOf<Int?>(null) }
        val cycleIntervalMillis = 3000L
        val highlightDurationMillis = 500L

        LaunchedEffect(key1 = entries) {
            if (entries.isEmpty()) {
                highlightedIndex = null
                return@LaunchedEffect
            }
            highlightedIndex = null
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
                if (entries.size <= 1 && isActive) {
                    delay(cycleIntervalMillis)
                } else if (isActive) {
                    delay(500L)
                }
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
                    text = stringResource(id = R.string.no_data_for_selected_view),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
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
fun TutorClassesRow(
    tutorData: TutorWithClasses,
    navController: NavHostController,
    onSummarizeClick: () -> Unit
) {
    val lazyRowState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(tutorData.filteredPastClasses) {
        if (tutorData.filteredPastClasses.isNotEmpty()) {
            scope.launch {
                lazyRowState.scrollToItem(0)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tutorData.tutorInfo.name + " " + tutorData.tutorInfo.surname
                        .split(" ")
                        .firstOrNull()
                        .orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (tutorData.tutorInfo.name.isNotBlank()) {
                    Text(
                        text = tutorData.tutorInfo.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (tutorData.allPastClassesWithStudents.isNotEmpty()) {
                OutlinedButton(onClick = onSummarizeClick) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.summarize_button))
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        if (tutorData.filteredPastClasses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_classes_match_filter_for_tutor),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyRow(
                state = lazyRowState,
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tutorData.filteredPastClasses, key = { it.first }) { (classId, savedClass) ->
                    Box(modifier = Modifier.width(385.dp)) {
                        InstantClassCard(
                            savedClass = savedClass,
                            studentsCount = tutorData.studentsCountMap[classId] ?: 0,
                            onClick = { navController.navigate("instantClassSummaryScreen/$classId") }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}


@RequiresApi(Build.VERSION_CODES.O)
internal fun isDateInPeriod(
    classLocalDate: LocalDate?,
    selectedDate: LocalDate,
    selectedTimeFilter: TimeFilter,
    locale: Locale
): Boolean {
    if (classLocalDate == null) return false
    return when (selectedTimeFilter) {
        TimeFilter.NONE -> true
        TimeFilter.WEEK -> {
            val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
            val startOfWeekSelected = selectedDate.with(firstDayOfWeek)
            val endOfWeekSelected = startOfWeekSelected.plusDays(6)
            !classLocalDate.isBefore(startOfWeekSelected) && !classLocalDate.isAfter(
                endOfWeekSelected
            )
        }

        TimeFilter.MONTH -> YearMonth.from(classLocalDate) == YearMonth.from(
            selectedDate
        )

        TimeFilter.YEAR -> classLocalDate.year == selectedDate.year
    }
}

