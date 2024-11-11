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
import com.example.pitapp.ui.screens.ClassDetailScreen
import com.example.pitapp.ui.screens.ErrorScreen
import com.example.pitapp.ui.screens.HomeScreen
import com.example.pitapp.ui.screens.LoginScreen
import com.example.pitapp.ui.screens.PermissionRequestsScreen
import com.example.pitapp.ui.screens.ProfileScreen
import com.example.pitapp.ui.screens.RegisterAllDataScreen
import com.example.pitapp.ui.screens.RegisterDataScreen
import com.example.pitapp.ui.screens.RequestedPermissionScreen
import com.example.pitapp.ui.screens.ResetPasswordScreen
import com.example.pitapp.ui.screens.ScheduleClassScreen
import com.example.pitapp.ui.screens.StartClassNowScreen
import com.example.pitapp.ui.screens.TutorsScreen
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
    val startDestination = when {
        !isUserLoggedIn -> "loginScreen"
        isUserLoggedIn -> "homeScreen"
        else -> "errorScreen"
    }


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
                fireStoreManager = fireStoreManager
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

        composable(route = "startClassNowScreen") {
            StartClassNowScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager
            )
        }

        composable(route = "scheduleClassScreen") {
            ScheduleClassScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager
            )
        }

        composable(
            route = "classDetailScreen",
        ) {
            ClassDetailScreen(
                navController = navController,
                authManager = authManager,
            )
        }
    }
}
