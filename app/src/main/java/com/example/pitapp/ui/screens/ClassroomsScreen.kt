package com.example.pitapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager


data class Classroom(
    val number: Int = 0,
    val description: String = ""
)

// Caso a considerar: El editar del salon se debe editar tambien en schedules :) y quizas tambien en clases, eso se vera despues jeje (igual si se elimina en el horario  etc.)


@Composable
fun ClassroomsScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    // Estado para la lista de salones (Pair<Number, Classroom>)
    val classrooms = remember { mutableStateOf<List<Pair<Int, Classroom>>>(emptyList()) }
    // Estados de carga y error
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Estados para el diálogo de edición y para agregar
    val classroomToEdit = remember { mutableStateOf<Classroom?>(null) }
    val showAddDialog = remember { mutableStateOf(false) }

    // Escucha en tiempo real la colección "saved_classrooms"
    LaunchedEffect(Unit) {
        fireStoreManager.getClassrooms { result ->
            result.onSuccess { list ->
                val mapped = list.map { it.number to it }
                classrooms.value = mapped.sortedByDescending { it.first }
                isLoading.value = false
                errorMessage.value = null
            }.onFailure { error ->
                errorMessage.value = error.localizedMessage ?: "Error desconocido"
                isLoading.value = false
            }
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = "Salones de Clases"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (isLoading.value) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (errorMessage.value != null) {
                Text(
                    text = "Error: ${errorMessage.value}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(classrooms.value) { (number, classroom) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Número: $number",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Descripción: ${classroom.description}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            // Al pulsar el ícono de editar se abre el diálogo de edición
                            IconButton(onClick = {
                                classroomToEdit.value = classroom
                            }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                            }
                            IconButton(onClick = {
                                fireStoreManager.deleteClassroom(number) { /* Opcional: manejar el resultado */ }
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                            }
                        }
                    }
                }
            }
            // Botón para agregar un nuevo salón
            Button(onClick = {
                showAddDialog.value = true
            }) {
                Text("Añadir Salón")
            }
        }
    }

    // Diálogo para editar un salón
    classroomToEdit.value?.let { originalClassroom ->
        ClassroomDialog(
            title = "Editar Salón",
            initialNumber = originalClassroom.number.toString(),
            initialDescription = originalClassroom.description,
            onConfirm = { newNumber, newDescription ->
                if (newNumber == originalClassroom.number) {
                    // Solo se actualiza la descripción
                    fireStoreManager.updateClassroom(
                        originalClassroom.copy(description = newDescription)
                    ) { /* Opcional: manejar el resultado */ }
                } else {
                    // Se crea un nuevo documento y se elimina el antiguo
                    val newClassroom = Classroom(number = newNumber, description = newDescription)
                    fireStoreManager.addClassroom(newClassroom) { result ->
                        result.onSuccess {
                            fireStoreManager.deleteClassroom(originalClassroom.number) { /* Opcional */ }
                        }
                    }
                }
                classroomToEdit.value = null
            },
            onDismiss = { classroomToEdit.value = null }
        )
    }

    // Diálogo para agregar un salón
    if (showAddDialog.value) {
        ClassroomDialog(
            title = "Añadir Salón",
            initialNumber = "",  // Campo vacío para que el usuario lo complete
            initialDescription = "",
            onConfirm = { newNumber, newDescription ->
                val newClassroom = Classroom(number = newNumber, description = newDescription)
                fireStoreManager.addClassroom(newClassroom) { /* Opcional: manejar el resultado */ }
                showAddDialog.value = false
            },
            onDismiss = { showAddDialog.value = false }
        )
    }
}

@Composable
fun ClassroomDialog(
    title: String,
    initialNumber: String,
    initialDescription: String,
    onConfirm: (number: Int, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var numberText by remember { mutableStateOf(initialNumber) }
    var descriptionText by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                OutlinedTextField(
                    value = numberText,
                    onValueChange = { numberText = it },
                    label = { Text("Número (entero)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Descripción") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val number = numberText.toIntOrNull()
                if (number != null) {
                    onConfirm(number, descriptionText)
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
