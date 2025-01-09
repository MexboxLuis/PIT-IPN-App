package com.example.pitapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.ui.components.AdminCard
import com.example.pitapp.ui.components.AdminCardGrid
import com.example.pitapp.ui.components.AdminScaffold
import com.example.pitapp.utils.FireStoreManager
import com.example.pitapp.utils.PreferencesManager
import kotlinx.coroutines.launch


@Composable
fun HomeScreen4Admin(
    navController: NavHostController,
    firestoreManager: FireStoreManager,
    preferencesManager: PreferencesManager,
    onPermissionRequestsClick: () -> Unit,
    onTutorsClick: () -> Unit,
    onClassesClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    val isGridViewFlow = preferencesManager.isGridView.collectAsState(initial = null)
    val isGridView = isGridViewFlow.value

    if (isGridView != null && isLoading) {
        isLoading = false
    }

    AdminScaffold(
        navController = navController,
        fireStoreManager = firestoreManager
    ) {
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            scope.launch {
                                preferencesManager.setIsGridView(!isGridView!!)
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (!isGridView!!) Icons.Default.GridView else Icons.Default.ViewStream,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                if (isGridView!!) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AdminCardGrid(
                                title = stringResource(id = R.string.permission_requests_title),
                                subtitle = stringResource(id = R.string.permission_requests_subtitle),
                                image = Icons.Default.Email,
                                onClick = { onPermissionRequestsClick() }
                            )
                        }
                        item {
                            AdminCardGrid(
                                title = stringResource(id = R.string.tutors_title),
                                subtitle = stringResource(id = R.string.tutors_subtitle),
                                image = Icons.Default.CoPresent,
                                onClick = { onTutorsClick() }
                            )
                        }
                        item {
                            AdminCardGrid(
                                title = stringResource(id = R.string.classes_title),
                                subtitle = stringResource(id = R.string.classes_subtitle),
                                image = Icons.Default.Class,
                                onClick = { onClassesClick() }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AdminCard(
                                title = stringResource(id = R.string.permission_requests_title),
                                subtitle = stringResource(id = R.string.permission_requests_subtitle),
                                image = Icons.Default.Email,
                                onClick = { onPermissionRequestsClick() }
                            )
                        }
                        item {
                            AdminCard(
                                title = stringResource(id = R.string.tutors_title),
                                subtitle = stringResource(id = R.string.tutors_subtitle),
                                image = Icons.Default.CoPresent,
                                onClick = { onTutorsClick() }
                            )
                        }
                        item {
                            AdminCard(
                                title = stringResource(id = R.string.classes_title),
                                subtitle = stringResource(id = R.string.classes_subtitle),
                                image = Icons.Default.Class,
                                onClick = { onClassesClick() }
                            )
                        }
                    }
                }
            }
        }
    }
}