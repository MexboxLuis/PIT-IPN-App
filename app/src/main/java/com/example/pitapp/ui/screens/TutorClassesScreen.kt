package com.example.pitapp.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        fireStoreManager.getAllUsersSnapshot { result ->
            if (result.isSuccess) {
                // Filter for teachers (permission == 1)
                users = result.getOrNull()?.filter { it.permission == 1 } ?: emptyList()
            } else if (result.isFailure) {
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
                item {
                    Text(
                        text = "Lista de Clases por Docente:",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(users) { user ->
                    TutorClassList(
                        email = user.email,
                        fireStoreManager = fireStoreManager,
                        navController = navController
                    )
                }
            }
        }
    }
}
