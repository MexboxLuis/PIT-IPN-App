package com.example.pitapp.navigation

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
import com.example.pitapp.ui.screens.AgendarClaseScreen
import com.example.pitapp.ui.screens.ClassDetailScreen
import com.example.pitapp.ui.screens.HomeScreen
import com.example.pitapp.ui.screens.LoginScreen
import com.example.pitapp.ui.screens.RegisterAllDataScreen
import com.example.pitapp.ui.screens.RegisterDataScreen
import com.example.pitapp.ui.screens.RequestsScreen
import com.example.pitapp.ui.screens.ResetPasswordScreen
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager


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

    BackHandler(enabled = currentRoute in listOf("homeScreen", "loginScreen")) {
        activity?.moveTaskToBack(true)
    }

    NavHost(
        navController = navController,
        startDestination = if (isUserLoggedIn) "homeScreen" else "loginScreen",
        modifier = Modifier.fillMaxSize()
    ) {
        composable(route = "loginScreen") {
            LoginScreen(
                authManager = authManager,
                onLoginSuccess = { navController.navigate("homeScreen") },
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
                firestoreManager = fireStoreManager
            )
        }

        composable(route = "requestsScreen") {
            RequestsScreen(
                navController = navController,
                authManager = authManager,
                firestoreManager = fireStoreManager
            )
        }

        composable(route = "agendarScreen") {
            AgendarClaseScreen(navController, authManager)
        }

        composable(
            route = "classDetailScreen/{className}/{tutor}/{startTime}",
            arguments = listOf(
                navArgument("className") { type = NavType.StringType },
                navArgument("tutor") { type = NavType.StringType },
                navArgument("startTime") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val className = backStackEntry.arguments?.getString("className") ?: ""
            val tutor = backStackEntry.arguments?.getString("tutor") ?: ""
            val startTime = backStackEntry.arguments?.getString("startTime") ?: ""
            val studentList =
                listOf("Alvara Garcia Vasquez", "Emilia Rodríguez", "Ernesto Sánchez", "Mequizboxix Luis")

            ClassDetailScreen(
                navController = navController,
                authManager = authManager,
                className = className,
                tutor = tutor,
                startTime = startTime,
                studentList = studentList
            )
        }


    }
}
