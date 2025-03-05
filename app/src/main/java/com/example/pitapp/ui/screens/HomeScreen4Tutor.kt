package com.example.pitapp.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NoBackpack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.data.ClassData
import com.example.pitapp.data.Student
import com.example.pitapp.ui.components.ClassCard
import com.example.pitapp.ui.components.CreateClassSheet
import com.example.pitapp.ui.components.EmptyState
import com.example.pitapp.ui.components.TutorScaffold
import com.example.pitapp.ui.model.SortOrder
import com.example.pitapp.utils.FireStoreManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen4Tutor(
    navController: NavHostController,
    fireStoreManager: FireStoreManager
) {

    val classes = remember { mutableStateOf<List<Pair<String, ClassData>>>(emptyList()) }
    val filteredClasses = remember { mutableStateOf<List<Pair<String, ClassData>>>(emptyList()) }
    var searchText by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.NEWEST) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val infiniteTransition = rememberInfiniteTransition(label = "blinkingTransition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ), label = "blinkingAnimation"
    )

    LaunchedEffect(Unit) {
        fireStoreManager.getClasses { result ->
            classes.value = result.getOrDefault(emptyList())
            filteredClasses.value = classes.value.sortedByDescending { it.second.startTime }
        }
    }

    LaunchedEffect(searchText, sortOrder) {
        filteredClasses.value = classes.value
            .filter { pair ->
                pair.second.tutoring.contains(searchText, ignoreCase = true) ||
                        pair.second.topic.contains(searchText, ignoreCase = true) ||
                        pair.second.classroom.contains(searchText, ignoreCase = true)
            }
            .let { filtered ->
                when (sortOrder) {
                    SortOrder.NEWEST -> filtered.sortedByDescending { it.second.startTime }
                    SortOrder.OLDEST -> filtered.sortedBy { it.second.startTime }
                }
            }
    }

    TutorScaffold(
        navController = navController,
        fireStoreManager = fireStoreManager
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {


            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {


                    if (classes.value.isNotEmpty()) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            label = { Text(stringResource(id = R.string.search_my_classes)) },
                            trailingIcon = {
                                if (searchText.isNotEmpty()) {
                                    IconButton(onClick = { searchText = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                sortOrder = if (sortOrder == SortOrder.NEWEST) {
                                    SortOrder.OLDEST
                                } else {
                                    SortOrder.NEWEST
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (sortOrder == SortOrder.NEWEST) {
                                    Icons.Default.ArrowDownward
                                } else {
                                    Icons.Default.ArrowUpward
                                },
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    IconButton(
                        onClick = {
                            scope.launch {
                                sheetState.show()
                            }
                        },

                        ) {
                        Icon(
                            imageVector = Icons.Default.AddBox,
                            contentDescription = null,
                            modifier = Modifier.size(if (sheetState.isVisible) 64.dp else 32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            if (classes.value.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon = Icons.Default.NoBackpack,
                            message = stringResource(R.string.no_classes_found)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .align(Alignment.TopEnd)
                                .graphicsLayer(alpha = alpha),
                        )
                    }

                }
            } else if (filteredClasses.value.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.NoBackpack,
                        message = stringResource(R.string.no_filtered_classes)
                    )
                }
            } else {

                items(filteredClasses.value) { (documentId, classItem) ->
                    var studentsList by remember { mutableStateOf<List<Student>>(emptyList()) }

                    LaunchedEffect(documentId) {
                        fireStoreManager.getStudents(documentId) { result ->
                            result.onSuccess { students ->
                                studentsList = students
                            }.onFailure {
                                studentsList = emptyList()
                            }
                        }
                    }

                    ClassCard(
                        classItem = classItem,
                        studentsList = studentsList,
                        onClick = {
                            navController.navigate("classDetailScreen/$documentId")
                        }
                    )
                }
            }
        }
    }

    CreateClassSheet(
        sheetState = sheetState,
        scope = scope,
        onStartNowClick = { navController.navigate("startInstantClassScreen") },
        onScheduleClick = { navController.navigate("generateScheduleScreen") },
        classes = classes.value
    )
}



