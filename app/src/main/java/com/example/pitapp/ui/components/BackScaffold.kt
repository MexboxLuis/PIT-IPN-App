package com.example.pitapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.currentRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackScaffold(
    navController: NavHostController,
    authManager: AuthManager,
    topBarTitle: String?,
    content: @Composable () -> Unit
) {

    val actualRoute = currentRoute(navController)
    val routePattern = Regex("^registerAllDataScreen/.+$")

    Scaffold(

        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (routePattern.matches(actualRoute ?: "")) {
                                authManager.deleteUser()
                            }

                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                title = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

//                        if (topBarTitle != null) {
//                            Text(
//                                text = if (routePattern.matches(actualRoute ?: "")) {
//                                    "authManager.deleteUser()"
//                                } else topBarTitle,
//                                modifier = Modifier.padding(start = 10.dp),
//                                style = MaterialTheme.typography.titleLarge
//                            )
//                        }
                        Text(
                            text = if (routePattern.matches(actualRoute ?: "")) {
                                "Delete User" // Or any appropriate title
                            } else topBarTitle ?: "",
                            // ...
                        )


                    }

                },
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }

}