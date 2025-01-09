package com.example.pitapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.currentRoute
import com.example.pitapp.utils.registerData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackScaffold(
    navController: NavHostController,
    authManager: AuthManager,
    topBarTitle: String?,
    content: @Composable () -> Unit
) {

    val actualRoute = currentRoute(navController)
    val isRegisteringDataScreen = actualRoute?.let { registerData(it) }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    if (showLogoutDialog) {
        LogoutDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                navController.navigate("loginScreen"){
                    popUpTo("homeScreen") {
                        inclusive = true
                    }
                }
                showLogoutDialog = false
                authManager.logout()
            }

        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (isRegisteringDataScreen == false) {
                        IconButton(
                            onClick = {
                                navController.navigateUp()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                title = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (topBarTitle != null) {
                            Text(
                                text = topBarTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                },
                actions = {
                    if (isRegisteringDataScreen == true || actualRoute == "profileScreen") {
                        IconButton(
                            onClick = { showLogoutDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                            )
                        }
                    }

                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }

}