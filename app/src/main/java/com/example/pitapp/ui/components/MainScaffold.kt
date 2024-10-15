package com.example.pitapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.currentRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController,
    authManager: AuthManager,
    content: @Composable () -> Unit
) {

    val actualRoute = currentRoute(navController)

    Scaffold(

        topBar = {
            TopAppBar(
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.pit_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { }
                    )
                },
                title = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "PIT App",
                            modifier = Modifier.padding(start = 10.dp),
                            style = MaterialTheme.typography.titleLarge
                        )

                    }

                },
                actions = {
                    IconButton(
                        onClick = {
                            authManager.logout()
                            navController.navigate("loginScreen")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null
                        )
                    }
                }
            )
        },

        bottomBar = {
            BottomAppBar {
                NavigationBar {
                    NavigationBarItem(
//                        selected = actualRoute == "homeScreen",
                        selected = false,
                        onClick = {
                            if (actualRoute != "homeScreen")
                                navController.navigate("homeScreen")
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null
                            )
                        },
                        label = { Text("Home") }
                    )
                }

            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("agendarScreen") },
                modifier = Modifier.alpha(1f)
            ) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = null)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }

}