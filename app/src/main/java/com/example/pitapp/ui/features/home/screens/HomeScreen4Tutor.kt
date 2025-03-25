package com.example.pitapp.ui.features.home.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
//import manifest permission
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NoBackpack
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.data.ClassData
import com.example.pitapp.data.Student
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.ui.components.CreateClassSheet
import com.example.pitapp.ui.components.EmptyState
import com.example.pitapp.ui.components.TutorScaffold
import com.example.pitapp.ui.model.SortOrder
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import java.util.jar.Manifest
import com.example.pitapp.ui.features.classes.model.SavedClass
import com.example.pitapp.ui.features.classes.model.SavedStudent

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
    val savedInstantClasses = remember { mutableStateOf<List<Pair<String, SavedClass>>>(emptyList()) }
    val filteredSavedInstantClasses = remember { mutableStateOf<List<Pair<String, SavedClass>>>(emptyList()) }
    val studentsCountMap = remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var searchText by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.NEWEST) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val infiniteTransition = rememberInfiniteTransition(label = "blinkingTransition")
    val currentTimeMillis = remember { mutableLongStateOf(System.currentTimeMillis()) }

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

    LaunchedEffect(searchText, sortOrder, savedInstantClasses.value, instantClasses.value, currentTimeMillis.longValue) {
        val visibleIds = instantClasses.value.filter { (_, savedClass) ->
            val classCalendar = Calendar.getInstance().apply { time = savedClass.date.toDate() }
            val currentCalendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis.longValue }
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
        val currentCalendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis.longValue }
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

    TutorScaffold(
        navController = navController,
        fireStoreManager = fireStoreManager
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (instantClasses.value.isNotEmpty() || savedInstantClasses.value.isNotEmpty()) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            label = { Text(stringResource(id = R.string.search_my_classes)) },
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
                            onClick = { sortOrder = if (sortOrder == SortOrder.NEWEST) SortOrder.OLDEST else SortOrder.NEWEST },
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
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
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
    }

    CreateClassSheet(
        sheetState = sheetState,
        scope = scope,
        onStartNowClick = { navController.navigate("startInstantClassScreen") },
        onScheduleClick = { navController.navigate("generateScheduleScreen") },
        instantClasses = instantClasses.value
    )
}

@Composable
fun InstantClassCard(
    savedClass: SavedClass,
    studentsCount: Int,
    onClick: () -> Unit
) {
    // Formateo de la hora y la fecha
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(savedClass.date.toDate())
    val dateFormat = SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(savedClass.date.toDate())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)  // Dimensión fija para mantener consistencia
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Encabezado con el subject (título) resaltado en un fondo primario
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .height(48.dp)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = savedClass.subject,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (studentsCount > 0) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$studentsCount",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f), // ✅ Permite que el otro Row tenga espacio
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = savedClass.topic,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = savedClass.classroom,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Fila 3: Hora y Fecha (separadas)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hora
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    // Fecha
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun InstantClassSummaryScreen(
    classId: String,
    authManager: AuthManager,
    navController: NavHostController,
    fireStoreManager: FireStoreManager
) {
    // Estados para almacenar la clase y la lista de alumnos
    val savedClass = remember { mutableStateOf<SavedClass?>(null) }
    val students = remember { mutableStateOf<List<SavedStudent>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val context = LocalContext.current  // Necesario para crear archivos y compartir

    // Efecto para cargar los datos de la clase y sus alumnos
    LaunchedEffect(classId) {
        fireStoreManager.getInstantClassDetails(classId) { result ->
            result.onSuccess { retrievedClass ->
                savedClass.value = retrievedClass
            }
        }
        fireStoreManager.getStudentsNow(classId) { studentResult ->
            studentResult.onSuccess { studentList ->
                students.value = studentList
                isLoading.value = false
            }
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = "Detalles de la Clase"
    ) {
        if (isLoading.value) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
        } else {
            savedClass.value?.let { savedClass ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "Materia: ${savedClass.subject}", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Tutor: ${savedClass.tutorEmail}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Salón: ${savedClass.classroom}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Tópico: ${savedClass.topic}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Fecha: ${savedClass.date.toDate()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Alumnos Inscritos:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón para generar y compartir el CSV de estudiantes
                    Button(onClick = {
                        val csvFile = generateStudentsCsv(savedClass, students.value, context)
                        shareFile(context, csvFile)
                    }) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Descargar CSV de Alumnos")
                    }


                    if (students.value.isEmpty()) {
                        Text(text = "No hay alumnos inscritos.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        LazyColumn {
                            items(students.value) { student ->
                                StudentItem(student)
                            }
                        }
                    }
                }
            } ?: run {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No se encontró información para esta clase.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}



// Función para generar el CSV de estudiantes
fun generateStudentsCsv(savedClass: SavedClass, students: List<SavedStudent>, context: Context): File {
    val file = File(context.filesDir, "asistencia_alumnos_${savedClass}.csv")

    csvWriter().open(file, append = false) {
        writeRow(
            listOf(
                "Fecha",
                "Nombre del alumno asesorado",
                "Boleta",
                "Horario",
                "Tema Visto",
                "Programa Educativo",
                "Correo Electronico",
                "Regular o irregular"
            )
        )

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:00", Locale.getDefault()) // Corrected time format
        val date = dateFormat.format(savedClass.date.toDate())


        for (student in students) {
            // Extract start hour and calculate end hour.  Handle potential parsing errors.
            val startTime = try {
                timeFormat.format(savedClass.date.toDate())  // Use HH:mm format
            } catch (e: Exception) {
                "Error" // Or some other default value
            }

            val endTime = try {
                val calendar = Calendar.getInstance().apply {
                    time = savedClass.date.toDate()
                    add(Calendar.HOUR_OF_DAY, 1) // Add one hour
                }
                timeFormat.format(calendar.time)  //Use HH:mm
            } catch(e: Exception){
                "Error"
            }


            val schedule = "$startTime - $endTime"

            writeRow(
                listOf(
                    date,
                    student.name,
                    student.studentId,
                    schedule,  // Corrected schedule
                    savedClass.topic,
                    student.academicProgram,
                    student.email,
                    if (student.isRegular) "Regular" else "Irregular"
                )
            )
        }
    }
    return file
}


//funcion para compartir
fun shareFile(context: Context, file: File) {
    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",  // Usar el provider correcto.
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir CSV"))
}

@Composable
fun StudentItem(student: SavedStudent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = student.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = "ID: ${student.studentId}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Programa: ${student.academicProgram}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Email: ${student.email}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

