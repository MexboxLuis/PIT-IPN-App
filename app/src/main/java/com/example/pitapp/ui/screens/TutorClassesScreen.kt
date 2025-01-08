package com.example.pitapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.data.ClassData
import com.example.pitapp.data.Student
import com.example.pitapp.data.UserData
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager

@Composable
fun TutorClassesScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    var users by remember { mutableStateOf<List<UserData>>(emptyList()) }
    val classesByUser = remember { mutableStateOf<Map<String, List<Pair<String, ClassData>>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val hiddenClassesByUser = remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    LaunchedEffect(Unit) {
        fireStoreManager.getAllUsersSnapshot { result ->
            if (result.isSuccess) {
                users = result.getOrNull()?.filter { it.permission == 1 } ?: emptyList()

                users.forEach { user ->
                    fireStoreManager.getClasses(user.email) { classResult ->
                        if (classResult.isSuccess) {
                            classesByUser.value = classesByUser.value.toMutableMap().apply {
                                this[user.email] = classResult.getOrDefault(emptyList())
                            }
                            // Initialize the visibility state for the user
                            hiddenClassesByUser.value = hiddenClassesByUser.value.toMutableMap().apply {
                                this[user.email] = false
                            }
                        } else {
                            errorMessage = "Error fetching classes for ${user.email}"
                        }
                    }
                }
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = "Clases de Docentes"
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (!errorMessage.isNullOrEmpty()) {
            Text(text = errorMessage ?: "Error desconocido")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                classesByUser.value.forEach { (userEmail, classes) ->
                    item {
                        val user = users.find { it.email == userEmail }
                        if (classes.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "Clases de ${user?.name ?: userEmail}",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                                // Use the userEmail to get the correct visibility state
                                IconButton(onClick = {
                                    hiddenClassesByUser.value = hiddenClassesByUser.value.toMutableMap().apply {
                                        this[userEmail] = !this[userEmail]!!
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (hiddenClassesByUser.value[userEmail]!!) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                        contentDescription = null
                                    )
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                    // Use the userEmail to get the correct visibility state
                    if (!hiddenClassesByUser.value[userEmail]!!) {
                        items(classes) { (documentId, classData) ->
                            var studentsList by remember { mutableStateOf<List<Student>>(emptyList()) }
                            LaunchedEffect(documentId) {
                                fireStoreManager.getStudents(documentId) { result ->
                                    result.onSuccess { students ->
                                        studentsList = students
                                    }.onFailure {
                                        studentsList = emptyList()
                                    }
                                }
                            }
                            ClassCard(
                                classItem = classData,
                                studentsList = studentsList,
                                onClick = {
                                    navController.navigate("classDetailScreen/$documentId")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

