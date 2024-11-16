package com.example.pitapp.ui.screens


import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
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
    var viewStudentsList by rememberSaveable {
        mutableStateOf(false)
    }


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
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (determineClassState(data)) {
                        ClassState.UPCOMING -> {

                            Text(
                                text = "Esperando...",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )


                        }

                        ClassState.IN_PROGRESS -> {

                            InProgressButtons(
                                onFinishClass = {
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
                                },
                                onAddStudent = {
                                    val newStudent = Student(
                                        name = "Nuevo Estudiante",
                                        studentId = "12345",
                                        academicProgram = "Ingeniería",
                                        email = "estudiante@ipn.mx",
                                        status = "Regular",
                                        signature = "FirmaDelEstudiante"
                                    )

                                    scope.launch {

                                        // Verificar si el estudiante ya existe antes de agregarlo
                                        fireStoreManager.isStudentExists(newStudent.studentId) { exists ->
                                            if (exists) {
                                                // Solo agregar el `studentId` en la clase si ya existe en `saved_students`
                                                fireStoreManager.addStudentToClass(
                                                    classDocumentId,
                                                    newStudent
                                                ) { result ->
                                                    result.onSuccess {
                                                        Toast.makeText(
                                                            context,
                                                            "Estudiante agregado a la clase",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        // Obtener la lista actualizada de estudiantes
                                                        fireStoreManager.getStudents(classDocumentId) { res ->
                                                            res.onSuccess { students ->
                                                                studentsList.value = students
                                                            }
                                                        }
                                                    }.onFailure {
                                                        Toast.makeText(
                                                            context,
                                                            "Error al agregar estudiante",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            } else {
                                                // Crear el nuevo estudiante y luego agregar el `studentId` en la clase
                                                fireStoreManager.addStudentToClass(
                                                    classDocumentId,
                                                    newStudent
                                                ) { result ->
                                                    result.onSuccess {
                                                        Toast.makeText(
                                                            context,
                                                            "Nuevo estudiante creado y agregado a la clase",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        fireStoreManager.getStudents(classDocumentId) { res ->
                                                            res.onSuccess { students ->
                                                                studentsList.value = students
                                                            }
                                                        }
                                                    }.onFailure {
                                                        Toast.makeText(
                                                            context,
                                                            "Error al crear y agregar el estudiante",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
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

                    if (studentsList.value.isNotEmpty())
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 128.dp)
                                .clickable { viewStudentsList = !viewStudentsList },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {

                            Text(
                                text = if (viewStudentsList) "Ocultar lista de estudiantes" else "Ver lista de estudiantes",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = if (viewStudentsList) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )

                        }
                    if (viewStudentsList) {
                        StudentListSection(students = studentsList.value)
                    }
                }
            }
        } ?: run {
            ErrorScreen()
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
fun InProgressButtons(
    onFinishClass: () -> Unit,
    onAddStudent: () -> Unit
) {
    IconButton(onClick = onAddStudent) {
        Icon(
            imageVector = Icons.Default.QrCode2,
            contentDescription = null,
        )
    }
    OutlinedButton(onClick = onFinishClass) {
        Text(text = "Terminar clase")
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
