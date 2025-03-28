package com.example.pitapp.ui.features.home.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.example.pitapp.model.UserData
import com.example.pitapp.ui.shared.screens.ErrorScreen
import com.example.pitapp.ui.shared.screens.LoadingScreen
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.datasource.FireStoreManager
import com.example.pitapp.datasource.PreferencesManager

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager,
    preferencesManager: PreferencesManager
) {

    var userData by remember { mutableStateOf<UserData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasNavigatedToRegister by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true

        fireStoreManager.getUserData { result ->
            if (result.isSuccess) {
                userData = result.getOrNull()
            } else {
                isLoading = false
            }
            isLoading = false
        }
    }

    if (isLoading)
        LoadingScreen()
    else if (userData == null && authManager.isUserLoggedIn() && !hasNavigatedToRegister) {
        hasNavigatedToRegister = true
        navController.navigate("registerAllDataScreen/${authManager.getUserEmail()}") {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
        }
    } else {
        when (userData?.permission) {

            -2 -> RejectedPermissionScreen(
                onExit = {
                    authManager.logout()
                    navController.navigate("loginScreen")
                }
            )

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
                firestoreManager = fireStoreManager,
                preferencesManager = preferencesManager,
                onPermissionRequestsClick = { navController.navigate("permissionRequestsScreen") },
                onTutorsClick = { navController.navigate("tutorsScreen") },
                onClassesClick = { navController.navigate("tutorClassesScreen") },
                onClassroomsClick = { navController.navigate("classroomsScreen") },
                onCalendarClick = { navController.navigate("calendarScreen") },
                onCareersClick = { navController.navigate("careersScreen") },
                onClassSchedulesClick = { navController.navigate("classSchedulesScreen") }
            )


            else -> ErrorScreen()
        }
    }

}

