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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
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
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {

                        }
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = "mostrar estudiantes")
                    }
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



    ModalBottomSheet(
        onDismissRequest = onDismiss,
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

            // Espacio para la firma
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
                    Text(
                        text = "Firma capturada.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Green
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
    val androidPath = remember { android.graphics.Path() }
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
fun SignatureScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var signatureBitmap by remember { mutableStateOf<Bitmap?>(null) }

        Text("Firme dentro del cuadro:", style = MaterialTheme.typography.titleMedium)

        SignaturePad(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            onSignatureCaptured = { bitmap ->
                signatureBitmap = bitmap
            }
        )

        Button(onClick = {
            signatureBitmap?.let {

                Toast.makeText(context, "Firma capturada", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Guardar Firma")
        }
    }
}


@Composable
fun StudentListSection(students: List<Student>) {
    var showStudentInfo by rememberSaveable { mutableStateOf(false) }
    var selectedStudent by rememberSaveable { mutableStateOf<Student?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(8.dp))

        students.forEach { student ->
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable {
                        selectedStudent = student
                        showStudentInfo = true
                    }
            ) {
                Text(
                    text = student.studentId,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }

    if (showStudentInfo && selectedStudent != null) {
        AlertDialog(
            onDismissRequest = { showStudentInfo = false },
            confirmButton = {
                Text(
                    text = "Cerrar",
                    modifier = Modifier
                        .clickable { showStudentInfo = false }
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(text = "Información de ${selectedStudent!!.studentId}") },
            text = {
                Column {
                    Text(text = "Nombre: ${selectedStudent!!.name}")
                    Text(text = "Boleta: ${selectedStudent!!.studentId}")
                    Text(text = "Programa Académico: ${selectedStudent!!.academicProgram}")
                    Text(text = "Email: ${selectedStudent!!.email}")
                    Text(text = "Estado: ${selectedStudent!!.status}")
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
