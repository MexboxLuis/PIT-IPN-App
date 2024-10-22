package com.example.pitapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.pitapp.R
import com.example.pitapp.data.UserData
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import com.example.pitapp.utils.currentRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager,
    content: @Composable () -> Unit
) {
    var userData by remember { mutableStateOf<UserData?>(null) }
    val actualRoute = currentRoute(navController)

    LaunchedEffect(Unit) {
        fireStoreManager.getUserData { result ->
            userData = if (result.isSuccess) {
                result.getOrNull()
            } else {
                null
            }
        }
    }

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
                    if (userData?.profilePictureUrl != null) {
                        AsyncImage(
                            model = userData?.profilePictureUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(42.dp)
                                .border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(32.dp))
                                .clip(CircleShape)
                                .clickable {
                                    navController.navigate("profileScreen")
                                }
                        )
                    } else {
                        IconButton(
                            onClick = {
                                navController.navigate("profileScreen")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            )
                        }
                    }


                }
            )
        },

        bottomBar = {
            BottomAppBar {
                NavigationBar {
                    NavigationBarItem(
                        selected = actualRoute == "homeScreen",
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
                        label = { Text(text = stringResource(id = R.string.home)) }
                    )
                    userData?.let { user ->
                        if (user.permission == 2) {
                            NavigationBarItem(
                                selected = actualRoute == "requestsScreen",
                                onClick = {
                                    if (actualRoute != "requestsScreen")
                                        navController.navigate("requestsScreen")
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.AddTask,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(text = stringResource(id = R.string.requests)) }
                            )
                        }
                    }
                }

            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("scheduleClassScreen") },
                containerColor = Color.Transparent,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}