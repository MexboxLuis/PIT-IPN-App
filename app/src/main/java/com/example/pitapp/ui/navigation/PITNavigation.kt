package com.example.pitapp.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.datasource.FireStoreManager
import com.example.pitapp.datasource.PreferencesManager
import com.example.pitapp.ui.features.auth.screens.LoginScreen
import com.example.pitapp.ui.features.auth.screens.RegisterAllDataScreen
import com.example.pitapp.ui.features.auth.screens.RegisterDataScreen
import com.example.pitapp.ui.features.auth.screens.ResetPasswordScreen
import com.example.pitapp.ui.features.calendar.screens.CalendarScreen
import com.example.pitapp.ui.features.careers.screens.CareerWebViewScreen
import com.example.pitapp.ui.features.careers.screens.CareersScreen
import com.example.pitapp.ui.features.classes.screens.InstantClassDetailsScreen
import com.example.pitapp.ui.features.classes.screens.InstantClassSummaryScreen
import com.example.pitapp.ui.features.classes.screens.StartInstantClassScreen
import com.example.pitapp.ui.features.classes.screens.StudentsScreen
import com.example.pitapp.ui.features.classes.screens.TutorClassesScreen
import com.example.pitapp.ui.features.classrooms.screens.ClassroomsScreen
import com.example.pitapp.ui.features.home.screens.HomeScreen
import com.example.pitapp.ui.features.home.screens.RequestedPermissionScreen
import com.example.pitapp.ui.features.permissions.screens.PermissionRequestsScreen
import com.example.pitapp.ui.features.permissions.screens.TutorsScreen
import com.example.pitapp.ui.features.profile.screens.ProfileScreen
import com.example.pitapp.ui.features.scheduling.screens.ClassSchedulesScreen
import com.example.pitapp.ui.features.scheduling.screens.EditScheduleScreen
import com.example.pitapp.ui.features.scheduling.screens.GenerateScheduleScreen
import com.example.pitapp.ui.shared.screens.ErrorScreen


@Composable
fun PITNavigation(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val activity = LocalContext.current as? Activity
    val isUserLoggedIn = authManager.isUserLoggedIn()
    val startDestination = when {
        !isUserLoggedIn -> "loginScreen"
        isUserLoggedIn -> "homeScreen"
        else -> "errorScreen"
    }
    val context = LocalContext.current
    val preferencesManager = PreferencesManager(context)


    BackHandler(
        enabled = currentRoute in listOf(
            "homeScreen",
            "loginScreen",
            "errorScreen"
        )
    ) {
        activity?.moveTaskToBack(true)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {

        composable(route = "errorScreen") {
            ErrorScreen()
        }

        composable(route = "loginScreen") {
            LoginScreen(
                authManager = authManager,
                onLoginSuccess = {
                    navController.navigate("homeScreen")
                },
                onRegisterClick = { navController.navigate("registerDataScreen") },
                onResetPasswordClick = { navController.navigate("resetPasswordScreen") }
            )
        }

        composable(route = "registerDataScreen") {
            RegisterDataScreen(
                navController = navController,
                authManager = authManager,
                onRegisterSuccess = { navController.navigate("homeScreen") }
            )
        }

        composable(
            route = "registerAllDataScreen/{email}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            RegisterAllDataScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager,
                email = email,
                onRegisterDataSuccess = { navController.navigate("homeScreen") }
            )
        }

        composable(route = "resetPasswordScreen") {
            ResetPasswordScreen(
                navController = navController,
                authManager = authManager,
                onPasswordResetSent = { navController.navigate("loginScreen") },
            )
        }

        composable(route = "homeScreen") {
            HomeScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager,
                preferencesManager = preferencesManager
            )
        }

        composable(route = "requestedScreen") {
            RequestedPermissionScreen(
                onExit = {
                    authManager.logout()
                    navController.navigate("loginScreen")
                }
            )
        }

        composable(route = "permissionRequestsScreen") {
            PermissionRequestsScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager
            )
        }

        composable(route = "tutorsScreen") {
            TutorsScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager
            )
        }

        composable(route = "profileScreen") {
            ProfileScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager
            )
        }

        composable(route = "generateScheduleScreen") {
            GenerateScheduleScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager
            )
        }

        composable(route = "tutorClassesScreen") {
            TutorClassesScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager
            )
        }

        composable(route = "classroomsScreen") {
            ClassroomsScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager
            )
        }

        composable(route = "calendarScreen") {
            CalendarScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager
            )
        }

        composable(route = "careersScreen") {
            CareersScreen(
                navController = navController,
                authManager = authManager
            )
        }

        composable("careerWebView/{encodedUrl}") { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("encodedUrl") ?: ""
            CareerWebViewScreen(
                navController = navController,
                authManager = authManager,
                encodedUrl = encodedUrl
            )
        }

        composable(route = "classSchedulesScreen") {
            ClassSchedulesScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager
            )
        }

        composable(route = "editSchedule/{scheduleId}") {
            val scheduleId = it.arguments?.getString("scheduleId")
            EditScheduleScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager,
                scheduleId = scheduleId
            )
        }

        composable(route = "startInstantClassScreen") {
            StartInstantClassScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager
            )
        }

        composable(route = "studentsScreen"){
            StudentsScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager
            )
        }

        composable(
            "instantClassDetailsScreen/{classDocumentId}",
            arguments = listOf(navArgument("classDocumentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val classDocumentId = backStackEntry.arguments?.getString("classDocumentId") ?: ""

            InstantClassDetailsScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager,
                classDocumentId = classDocumentId
            )

        }

        composable(
            route = "instantClassSummaryScreen/{classDocumentId}",
            arguments = listOf(navArgument("classDocumentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classDocumentId") ?: ""
            InstantClassSummaryScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager,
                classId = classId
            )
        }
    }
}
