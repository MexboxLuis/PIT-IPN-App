package com.example.pitapp.ui.screens

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
import androidx.navigation.NavHostController
import com.example.pitapp.data.UserData
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager

@Composable
fun TutorsScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    var tutors by remember { mutableStateOf<List<UserData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        fireStoreManager.getAllUsersSnapshot { result ->
            if (result.isSuccess) {
                tutors = result.getOrNull()?.filter { it.permission == 1 } ?: emptyList()
            } else if (result.isFailure) {
                errorMessage = result.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = "Tutores"
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error
            )
        } else {
            LazyColumn {
                items(tutors) { tutor ->
                    UserRow(tutor, fireStoreManager)
                }
            }
        }
    }
}