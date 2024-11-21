package com.example.pitapp.ui.screens


import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.data.ClassData
import com.example.pitapp.data.Student
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.ui.model.ClassState
import com.example.pitapp.ui.model.determineClassState
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import kotlinx.coroutines.launch

import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

@Composable
fun ClassDetailScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager,
    classDocumentId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var isError by rememberSaveable { mutableStateOf(false) }
    var showStudents by remember { mutableStateOf(false) }
    val classData = remember { mutableStateOf<ClassData?>(null) }
    val studentsList = remember { mutableStateOf<List<Student>>(emptyList()) }
    var showAddStudentSheet by remember { mutableStateOf(false) }

    LaunchedEffect(classDocumentId) {
        fireStoreManager.getClassDetails(classDocumentId) { fetchedClass ->
            classData.value = fetchedClass.getOrNull()

            fireStoreManager.getStudents(classDocumentId) { result ->
                result.onSuccess { students ->
                    studentsList.value = students
                    isError = false
                    isLoading = false
                }.onFailure {
                    isError = true
                }
            }
        }
    }

    if (isLoading) {
        LoadingScreen()
    } else {
        classData.value?.let { data ->
            BackScaffold(
                navController = navController,
                authManager = authManager,
                topBarTitle = data.topic
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    when (determineClassState(data)) {
                        ClassState.IN_PROGRESS -> {
                            IconButton(
                                onClick = { showAddStudentSheet = true },
                                modifier = Modifier
                                    .size(200.dp) // Tamaño del IconButton más grande
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize() // Deja que el ícono ocupe todo el espacio del botón
                                )
                            }
                            OutlinedButton(onClick = {
                                scope.launch {
                                    fireStoreManager.finishClass(
                                        classDocumentId,
                                        data.startTime
                                    ) { result ->
                                        result.onSuccess {
                                            Toast.makeText(
                                                context,
                                                "Clase finalizada",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }.onFailure {
                                            Toast.makeText(
                                                context,
                                                "Error al finalizar la clase",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }) {
                                Text(text = "Terminar clase")
                            }

                        }

                        ClassState.UPCOMING -> {
                            Text(
                                text = "Esperando...",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        ClassState.FINISHED -> {
                            FinishedButtons(
                                realDuration = data.realDuration,
                                onGenerateExcel = {
                                    Toast.makeText(
                                        context,
                                        "Generando Excel...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    }
                    if (studentsList.value.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                showStudents = !showStudents
                            }
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = if (showStudents) "Ocultar estudiantes" else "Mostrar estudiantes",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = if (showStudents) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                        if (showStudents)
                            StudentListSection(students = studentsList.value)
                    }

                }


            }
        } ?: run {
            ErrorScreen()
        }
    }

    if (showAddStudentSheet) {
        AddStudentBottomSheet(
            onDismiss = { showAddStudentSheet = false },
            onSave = { student ->
                scope.launch {
                    fireStoreManager.addStudentToClass(
                        classDocumentId,
                        student
                    ) { result ->
                        result.onSuccess {
                            Toast.makeText(context, "Estudiante agregado", Toast.LENGTH_SHORT)
                                .show()
                            showAddStudentSheet = false
                            fireStoreManager.getStudents(classDocumentId) { res ->
                                res.onSuccess { students -> studentsList.value = students }
                            }
                        }.onFailure {
                            Toast.makeText(
                                context,
                                "Error al agregar estudiante",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentBottomSheet(
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var studentId by rememberSaveable { mutableStateOf("") }
    var program by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var signatureBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSignatureCaptured by remember { mutableStateOf(false) } // Para mostrar el estado de la firma


    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    LaunchedEffect(Unit) {
        sheetState.expand() // Fuerza que se expanda completamente
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Agregar Estudiante",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = studentId,
                onValueChange = { studentId = it },
                label = { Text("Boleta") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = program,
                onValueChange = { program = it },
                label = { Text("Programa Académico") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )


            Text(
                text = "Firma del Estudiante",
                style = MaterialTheme.typography.titleMedium
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                if (!isSignatureCaptured) {
                    SignaturePad(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        onSignatureCaptured = { bitmap ->
                            signatureBitmap = bitmap
                            isSignatureCaptured = true
                        }
                    )
                } else {
                    // Mostrar la firma capturada
                    Image(
                        bitmap = signatureBitmap!!.asImageBitmap(),
                        contentDescription = "Firma Capturada",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(4.dp)
                    )
                }
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
                OutlinedButton(
                    onClick = {
                        if (name.isNotBlank() && studentId.isNotBlank() && isSignatureCaptured) {
                            val newStudent = Student(
                                name = name,
                                studentId = studentId,
                                academicProgram = program,
                                email = email,
                                status = "Regular",
                                signature = signatureBitmap?.let { bitmapToBase64(it) } ?: ""
                            )
                            onSave(newStudent)
                        }
                    }
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}


fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    onSignatureCaptured: (Bitmap) -> Unit
) {
    val androidPath = remember { Path() }
    val path = remember { androidx.compose.ui.graphics.Path() }

    val density = LocalDensity.current
    val bitmapWidth = with(density) { 300.dp.toPx().toInt() }
    val bitmapHeight = with(density) { 150.dp.toPx().toInt() }

    val bitmap = remember {
        Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
    }

    val paint = Paint().apply {
        color = android.graphics.Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    val canvas = Canvas(bitmap)

    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        path.moveTo(offset.x, offset.y)
                        androidPath.moveTo(offset.x, offset.y)
                    },
                    onDrag = { change, _ ->
                        val position = change.position
                        path.lineTo(position.x, position.y)
                        androidPath.lineTo(position.x, position.y)

                        // Dibuja en el bitmap en tiempo real
                        canvas.drawColor(android.graphics.Color.WHITE) // Limpia el bitmap
                        canvas.drawPath(androidPath, paint)
                    },
                    onDragEnd = {
                        // Enviar el bitmap capturado
                        onSignatureCaptured(bitmap)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPath(
                path = path,
                color = Color.Black,
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}


@Composable
fun StudentListSection(students: List<Student>) {
    var showStudentInfo by rememberSaveable { mutableStateOf(false) }
    var selectedStudent by rememberSaveable { mutableStateOf<Student?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(students) { student ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedStudent = student
                        showStudentInfo = true
                    },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Estudiante",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 16.dp)
                    )
                    Column {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Boleta: ${student.studentId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showStudentInfo && selectedStudent != null) {
        AlertDialog(
            onDismissRequest = { showStudentInfo = false },
            confirmButton = {

            },
            title = {
                Text(
                    text = "Información de ${selectedStudent!!.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Nombre
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Nombre",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedStudent!!.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Boleta
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "Boleta",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedStudent!!.studentId,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Programa Académico
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "Programa Académico",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedStudent!!.academicProgram,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Email
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedStudent!!.email,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Estado
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Estado",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedStudent!!.status,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
        )
    }
}


@Composable
fun FinishedButtons(
    realDuration: Long?,
    onGenerateExcel: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedButton(onClick = onGenerateExcel) {
            Text(text = "Generar Excel")
        }

        realDuration?.let {
            Text(
                text = "Duración: $it minutos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
