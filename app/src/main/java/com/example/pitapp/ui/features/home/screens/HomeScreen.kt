package com.example.pitapp.ui.features.home.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.datasource.FireStoreManager
import com.example.pitapp.datasource.PreferencesManager
import com.example.pitapp.ui.shared.screens.ErrorScreen
import com.example.pitapp.ui.shared.screens.LoadingScreen

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun HomeScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager,
    preferencesManager: PreferencesManager
) {
    val userResult by fireStoreManager.getUserData().collectAsState(initial = null)
    val userData = userResult?.getOrNull()
    val isLoading = userResult == null
    var hasNavigatedToRegister by remember { mutableStateOf(false) }

    when {
        isLoading -> {
            LoadingScreen()
        }

        userData == null && authManager.isUserLoggedIn() && !hasNavigatedToRegister -> {
            hasNavigatedToRegister = true
            LaunchedEffect(Unit) {
                navController.navigate("registerAllDataScreen/${authManager.getUserEmail()}") {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
        }

        else -> {
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
                    onClassSchedulesClick = { navController.navigate("classSchedulesScreen") },
                    onStudentsClick = { navController.navigate("studentsScreen") }
                )

                else -> ErrorScreen()
            }
        }
    }
}


