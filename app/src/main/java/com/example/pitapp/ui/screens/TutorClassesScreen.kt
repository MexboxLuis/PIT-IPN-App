package com.example.pitapp.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.data.ClassData
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
    val allClasses = remember { mutableStateOf<List<Pair<String, ClassData>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        fireStoreManager.getAllUsersSnapshot { result ->
            if (result.isSuccess) {
                users = result.getOrNull()?.filter { it.permission == 1 } ?: emptyList()

                users.forEach { user ->
                    fireStoreManager.getClasses(user.email) { classResult ->
                        if (classResult.isSuccess) {
                            allClasses.value += classResult.getOrDefault(emptyList())
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
            TutorClassList(
                classes = allClasses.value,
                fireStoreManager = fireStoreManager,
                navController = navController
            )
        }
    }
}

