package com.example.pitapp.ui.features.permissions.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.model.UserData
import com.example.pitapp.ui.shared.components.BackScaffold
import com.example.pitapp.ui.shared.components.EmptyState
import com.example.pitapp.ui.features.permissions.components.UserRow
import com.example.pitapp.ui.shared.screens.ErrorScreen
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.datasource.FireStoreManager

@Composable
fun PermissionRequestsScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    var users by remember { mutableStateOf<List<UserData>>(emptyList()) }
    var rejectedUsers by remember { mutableStateOf<List<UserData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        fireStoreManager.getAllUsersSnapshot { result ->
            if (result.isSuccess) {
                users = result.getOrNull()?.filter { it.permission == 0 } ?: emptyList()
                rejectedUsers = result.getOrNull()?.filter { it.permission == -2 } ?: emptyList()
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(id = R.string.permission_requests_title)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize())
            }
            errorMessage != null -> {
                ErrorScreen()
            }
            else -> {
                var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(stringResource(id = R.string.tutor_requests))
                                    Icon(
                                        imageVector = Icons.Default.HowToVote,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(stringResource(id = R.string.rejected_users))
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (selectedTabIndex) {
                            0 -> {
                                if (users.isNotEmpty()) {
                                    items(users) { user ->
                                        UserRow(user, fireStoreManager)
                                    }
                                } else {
                                    item {
                                        EmptyState(
                                            icon = Icons.Default.PersonOff,
                                            message = stringResource(R.string.no_requests)
                                        )
                                    }
                                }
                            }
                            1 -> {
                                if (rejectedUsers.isNotEmpty()) {
                                    items(rejectedUsers) { user ->
                                        UserRow(user, fireStoreManager)
                                    }
                                } else {
                                    item {
                                        EmptyState(
                                            icon = Icons.Default.Cancel,
                                            message = stringResource(R.string.no_rejected_users)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



