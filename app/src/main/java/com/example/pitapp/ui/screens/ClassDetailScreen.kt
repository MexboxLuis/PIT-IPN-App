package com.example.pitapp.ui.screens


import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.data.ClassData
import com.example.pitapp.data.Student
import com.example.pitapp.data.UserData
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.ui.components.SignaturePad
import com.example.pitapp.ui.components.StudentInfoDialog
import com.example.pitapp.ui.model.ClassState
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import com.example.pitapp.utils.bitmapToBase64
import com.example.pitapp.utils.determineClassState
import kotlinx.coroutines.launch

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
    var userData by remember { mutableStateOf<UserData?>(null) }
    var showAddStudentSheet by remember { mutableStateOf(false) }

    LaunchedEffect(classDocumentId) {

        fireStoreManager.getUserData { result ->
            if (result.isSuccess) {
                userData = result.getOrNull()
            }
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

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .padding(bottom = 16.dp)
                                    )
                                    Text(
                                        text = "Clase en Progreso",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            IconButton(
                                                onClick = { showAddStudentSheet = true },
                                                modifier = Modifier.size(150.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.QrCode2,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Text(
                                                text = "Mostrar QR",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            OutlinedButton(
                                                onClick = {
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
                                                }
                                            ) {
                                                Text(text = "Terminar Clase")
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Button(
                                        onClick = { showStudents = !showStudents },
                                        modifier = Modifier.fillMaxWidth(0.6f)
                                    ) {
                                        Text(text = if (showStudents) "Ocultar Estudiantes" else "Mostrar Estudiantes")
                                    }

                                    AnimatedVisibility(visible = showStudents) {

                                        if (studentsList.value.isNotEmpty()) {
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable {
                                                    showStudents = !showStudents
                                                }
                                            ) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                            if (showStudents)
                                                StudentListSection(students = studentsList.value)
                                        }

                                    }
                                }
                            }
                        }


                        ClassState.UPCOMING -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .padding(bottom = 16.dp)
                                    )
                                    Text(
                                        text = "¡Clase Próximamente!",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Detalles de la Clase:",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Tema: ${data.topic}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Tutor: ${data.email}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
//                                    Text(
//                                        text = "Fecha: } ",
//                                        style = MaterialTheme.typography.bodyLarge,
//                                        color = MaterialTheme.colorScheme.onBackground
//                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.5f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Top
                                ) {
                                    Text(
                                        text = "¡Prepárate para tu próxima clase!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Image(
                                        painter = painterResource(id = R.drawable.pit_logo), // Reemplaza con tu recurso de imagen
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(200.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            }
                        }


                        ClassState.FINISHED -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.Top,
                                ) {
                                    Text(
                                        text = "Detalles de la Clase Finalizada",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Tema: ${data.topic}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Tutor: ${data.email}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Inicio: Aqui va el dia y hora",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    data.realDuration?.let {
                                        Text(
                                            text = "Duración: $it minutos",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))

//                                    OutlinedButton(
//                                        onClick = {
//                                            Toast.makeText(
//                                                context,
//                                                "Generando Excel...",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        },
//                                        modifier = Modifier.align(Alignment.CenterHorizontally)
//                                    ) {
//                                        Text(text = "Generar Reporte en Excel")
//                                    }
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.Top,
                                ) {
                                    Text(
                                        text = "Lista de Estudiantes",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (studentsList.value.isNotEmpty()) {
                                        StudentListSection(students = studentsList.value)
                                    } else {
                                        Text(
                                            text = "No hay estudiantes registrados en esta clase.",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )
                                    }
                                }
                            }
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
    var selectedOption by rememberSaveable { mutableStateOf(true) }
    var signatureBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSignatureCaptured by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Agregar Estudiante",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }


            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
            }


            item {
                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text("Boleta") },
                    modifier = Modifier.fillMaxWidth()
                )
            }


            item {
                OutlinedTextField(
                    value = program,
                    onValueChange = { program = it },
                    label = { Text("Programa Académico") },
                    modifier = Modifier.fillMaxWidth()
                )
            }



            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }


            item {

                Text("Tu situacion de estudios es regular?")
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Opción "Sí"
                    RadioButton(
                        selected = selectedOption,
                        onClick = { selectedOption = true }
                    )
                    Text("Sí")

                    Spacer(modifier = Modifier.width(16.dp))


                    RadioButton(
                        selected = !selectedOption,
                        onClick = { selectedOption = false }
                    )
                    Text("No")
                }

            }



            item {
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
                        Image(
                            bitmap = signatureBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }


            item {
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
                                    isRegular = selectedOption,
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
}


@Composable
fun StudentListSection(students: List<Student>) {
    var showStudentInfo by rememberSaveable { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }

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
                        contentDescription = null,
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
                            text = student.studentId,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showStudentInfo && selectedStudent != null) {
        StudentInfoDialog(
            selectedStudent = selectedStudent,
            onDismiss = { showStudentInfo = false }
        )
    }
}


