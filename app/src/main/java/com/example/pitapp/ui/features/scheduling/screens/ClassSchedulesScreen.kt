package com.example.pitapp.ui.features.scheduling.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pending
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.ui.components.EmptyState
import com.example.pitapp.ui.features.scheduling.components.ScheduleItem
import com.example.pitapp.ui.features.scheduling.model.Schedule
import com.example.pitapp.ui.screens.ErrorScreen
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ClassSchedulesScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabTitles = listOf(
        stringResource(R.string.defined_schedules),
        stringResource(R.string.pending_schedules)
    )
    val context = LocalContext.current

    val schedules = remember { mutableStateOf<List<Pair<String, Schedule>>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }


    LaunchedEffect(Unit) {
        fireStoreManager.getSchedules { result ->
            result.onSuccess { list ->
                schedules.value = list.sortedWith(
                    compareBy<Pair<String, Schedule>> { it.second.startYear }
                        .thenBy { it.second.startMonth }
                        .thenBy { it.second.endYear }
                        .thenBy { it.second.endMonth }
                )
                isLoading.value = false
            }.onFailure {
                errorMessage.value =
                    it.localizedMessage ?: context.getString(R.string.unknown_error)
                isLoading.value = false
            }
        }
    }


    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(R.string.class_schedules_title)
    ) {
        when {
            isLoading.value -> {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize())
            }

            errorMessage.value != null -> {
                ErrorScreen()
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(text = title, fontWeight = FontWeight.Bold)
                                        Icon(
                                            imageVector = if (index == 0) Icons.Default.CheckCircle else Icons.Default.Pending,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                        }
                    }

                    val filteredSchedules = when (selectedTabIndex) {
                        0 -> schedules.value.filter { it.second.approved }
                        1 -> schedules.value.filter { !it.second.approved }
                        else -> emptyList()
                    }

                    if (filteredSchedules.isEmpty()) {
                        EmptyState(
                            icon = if (selectedTabIndex == 0) Icons.Default.CheckCircle else Icons.Default.Pending,
                            message = if (selectedTabIndex == 0) stringResource(R.string.no_approved_schedules) else stringResource(
                                R.string.no_pending_schedules
                            )
                        )
                    } else {
                        val groupedSchedules = filteredSchedules.groupBy { it.second.tutorEmail }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            groupedSchedules.forEach { (_, schedulesForTutor) ->
                                item {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        )
                                    ) {
                                        items(schedulesForTutor) { (scheduleId, schedule) ->
                                            Box(modifier = Modifier.width(350.dp)) {
                                                ScheduleItem(
                                                    schedule = schedule,
                                                    onEdit = { navController.navigate("editSchedule/$scheduleId") },
                                                    onApprove = {
                                                        CoroutineScope(Dispatchers.Main).launch {
                                                            val isClassroomOverlapping =
                                                                fireStoreManager.checkForOverlap(
                                                                    schedule
                                                                )
                                                            if (isClassroomOverlapping) {
                                                                Toast.makeText(
                                                                    context,
                                                                    context.getString(R.string.classroom_overlap),
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                                return@launch
                                                            }
                                                            val isEmailOverlapping =
                                                                fireStoreManager.checkForEmailOverlap(
                                                                    schedule
                                                                )
                                                            if (isEmailOverlapping) {
                                                                Toast.makeText(
                                                                    context,
                                                                    context.getString(R.string.tutor_overlap),
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                                return@launch
                                                            }
                                                            fireStoreManager.approveSchedule(
                                                                scheduleId
                                                            )
                                                            { result ->
                                                                selectedTabIndex = 0
                                                                if (result.isFailure) {
                                                                    Toast.makeText(
                                                                        context,
                                                                        context.getString(R.string.approval_error),
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }
                                                        }
                                                    },
                                                    onDisapprove = {
                                                        CoroutineScope(Dispatchers.Main).launch {
                                                            fireStoreManager.disapproveSchedule(
                                                                scheduleId
                                                            ) {
                                                                Toast.makeText(
                                                                    context,
                                                                    context.getString(R.string.disapproval_success),
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }
                                                    },
                                                    onDelete = {
                                                        CoroutineScope(Dispatchers.Main).launch {
                                                            fireStoreManager.deleteSchedule(
                                                                scheduleId
                                                            ) {
                                                                Toast.makeText(
                                                                    context,
                                                                    context.getString(R.string.deletion_success),
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }
                                                    }
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
    }
}