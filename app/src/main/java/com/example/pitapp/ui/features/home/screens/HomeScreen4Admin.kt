package com.example.pitapp.ui.features.home.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.example.pitapp.datasource.FireStoreManager
import com.example.pitapp.datasource.PreferencesManager
import com.example.pitapp.ui.features.home.components.AdminCard
import com.example.pitapp.ui.features.home.components.AdminCardData
import com.example.pitapp.ui.features.home.components.AdminCardGrid
import com.example.pitapp.ui.features.home.components.AdminScaffold
import com.example.pitapp.ui.shared.components.EmptyState
import kotlinx.coroutines.launch


@Composable
fun HomeScreen4Admin(
    navController: NavHostController,
    firestoreManager: FireStoreManager,
    preferencesManager: PreferencesManager,
    onPermissionRequestsClick: () -> Unit,
    onTutorsClick: () -> Unit,
    onClassesClick: () -> Unit,
    onClassroomsClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onCareersClick: () -> Unit,
    onClassSchedulesClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    val isGridViewFlow = preferencesManager.isGridView.collectAsState(initial = null)
    val isGridView = isGridViewFlow.value

    var searchQuery by remember { mutableStateOf("") }
    val items = listOf(
        AdminCardData(
            title = stringResource(id = R.string.permission_requests_title),
            subtitle = stringResource(id = R.string.permission_requests_subtitle),
            icon = Icons.Default.Email,
            onClick = onPermissionRequestsClick
        ),
        AdminCardData(
            title = stringResource(id = R.string.tutors_title),
            subtitle = stringResource(id = R.string.tutors_subtitle),
            icon = Icons.Default.CoPresent,
            onClick = onTutorsClick
        ),
        AdminCardData(
            title = stringResource(id = R.string.classes_title),
            subtitle = stringResource(id = R.string.classes_subtitle),
            icon = Icons.Default.Class,
            onClick = onClassesClick
        ),
        AdminCardData(
            title = stringResource(id = R.string.classrooms_title),
            subtitle = stringResource(id = R.string.classrooms_subtitle),
            icon = Icons.Default.MeetingRoom,
            onClick = { onClassroomsClick() }
        ),
        AdminCardData(
            title = stringResource(id = R.string.calendar_title),
            subtitle = stringResource(id = R.string.calendar_subtitle),
            icon = Icons.Default.CalendarMonth,
            onClick = { onCalendarClick() }
        ),
        AdminCardData(
            title = stringResource(id = R.string.careers_title),
            subtitle = stringResource(id = R.string.careers_subtitle),
            icon = Icons.Default.Work,
            onClick = { onCareersClick() }
        ),
        AdminCardData(
            title = stringResource(id = R.string.class_schedules_title),
            subtitle = stringResource(id = R.string.class_schedules_subtitle),
            icon = Icons.Default.Schedule,
            onClick = { onClassSchedulesClick() }
        )

    )

    val filteredItems = items.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.subtitle.contains(searchQuery, ignoreCase = true)
    }

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
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(text = stringResource(id = R.string.search_section)) },
                        modifier = Modifier
                            .padding(bottom = 16.dp).fillMaxWidth(0.85f),
                        trailingIcon = {
                            IconButton(
                                onClick = { if (searchQuery.isNotEmpty()) searchQuery = "" }) {
                                Icon(
                                    imageVector = if (searchQuery.isNotEmpty()) Icons.Default.Cancel else Icons.Default.Search,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            scope.launch {
                                preferencesManager.setIsGridView(!isGridView!!)
                            }
                        },
                        modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (!isGridView!!) Icons.Default.GridView else Icons.Default.ViewStream,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                if (searchQuery.isNotEmpty() && filteredItems.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        message = stringResource(id = R.string.no_filtered_section)
                    )
                } else {
                    if (isGridView!!) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredItems) { item ->
                                AdminCardGrid(
                                    title = item.title,
                                    subtitle = item.subtitle,
                                    image = item.icon,
                                    onClick = item.onClick
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredItems) { item ->
                                AdminCard(
                                    title = item.title,
                                    subtitle = item.subtitle,
                                    image = item.icon,
                                    onClick = item.onClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

