package com.example.pitapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.example.pitapp.data.UserData
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {

    var userData by remember { mutableStateOf<UserData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasNavigatedToRegister by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true

        fireStoreManager.getUserData { result ->
            if (result.isSuccess) {
                userData = result.getOrNull()
                println("User data retrieved: $userData")
            } else {
                println("Failed to retrieve user data: ${result.exceptionOrNull()?.message}")
            }

            isLoading = false
        }
    }


    if (isLoading) {
        LoadingScreen()
    }
    else if (userData == null && authManager.isUserLoggedIn() && !hasNavigatedToRegister) {
        hasNavigatedToRegister = true
        navController.navigate("registerAllDataScreen/${authManager.getUserEmail()}"){
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
        }
    } else {
        when (userData?.permission) {
            0 -> RequestedPermissionScreen(
                onExit = {
                    authManager.logout()
                    navController.navigate("loginScreen")
                }
            )

            1 -> HomeScreen4Tutor(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager
            )

            2 -> HomeScreen4Admin(
                navController = navController,
                authManager = authManager,
                firestoreManager = fireStoreManager,
                onPermissionRequestsClick = { navController.navigate("permissionRequestsScreen") }
            )
            else -> ErrorScreen()
        }

    }


}

