package com.example.pitapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pitapp.data.ClassData
import com.example.pitapp.data.Student
import com.example.pitapp.ui.components.CreateClassSheet
import com.example.pitapp.ui.components.TutorScaffold
import com.example.pitapp.ui.model.ClassState
import com.example.pitapp.ui.model.determineClassState
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen4Tutor(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    val currentUserEmail = authManager.getUserEmail() ?: ""
    val classes = remember { mutableStateOf<List<Pair<String, ClassData>>>(emptyList()) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        fireStoreManager.getClasses(currentUserEmail) { result ->
            classes.value = result.getOrDefault(emptyList())
            classes.value = classes.value.sortedBy { it.second.startTime }
            classes.value = classes.value.reversed()
        }
    }

    TutorScaffold(
        navController = navController,
        authManager = authManager,
        fireStoreManager = fireStoreManager
    ) {
        TutorClassList(
            classes = classes.value,
            navController = navController,
            fireStoreManager = fireStoreManager,
            header = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Search in my classes") },
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth(0.65f)
                    )
                    IconButton(
                        onClick = { /*TODO*/ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)

                        )
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                sheetState.show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddBox,
                            contentDescription = null,
                            modifier = Modifier.size(if (sheetState.isVisible) 64.dp else 32.dp)
                        )
                    }
                }
            }
        )
    }


    CreateClassSheet(
        sheetState = sheetState,
        scope = scope,
        onStartNowClick = { navController.navigate("startClassNowScreen") },
        onScheduleClick = { navController.navigate("scheduleClassScreen") },
        classes = classes.value
    )
}

@Composable
fun TutorClassList(
    navController: NavHostController,
    fireStoreManager: FireStoreManager,
    classes: List<Pair<String, ClassData>>,
    header: @Composable () -> Unit = {},
) {

    val studentsList = remember { mutableStateOf<List<Student>>(emptyList()) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            header()
        }
        items(classes) { (documentId, classItem) ->

            LaunchedEffect(Unit) {
                fireStoreManager.getStudents(documentId) { result ->
                    result.onSuccess { students ->
                        studentsList.value = students
                    }.onFailure {
                        studentsList.value = emptyList()
                    }
                }
            }

            ClassCard(
                classItem = classItem,
                studentsList = studentsList.value,
                onClick = {
                    navController.navigate("classDetailScreen/$documentId")
                }
            )
        }
    }
}


@Composable
fun ClassCard(
    classItem: ClassData,
    studentsList: List<Student>,
    onClick: () -> Unit
) {
    val classState = determineClassState(classItem)
    val opacity = when (classState) {
        ClassState.UPCOMING -> 0.1f
        ClassState.IN_PROGRESS -> 1f
        ClassState.FINISHED -> 0.5f
    }

    val classStateText = when (classState) {
        ClassState.IN_PROGRESS -> "Class is in progress"
        ClassState.UPCOMING -> "Class has not started yet"
        ClassState.FINISHED -> "Class has finished"
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .alpha(opacity)
            .heightIn(min = 154.dp, max = 154.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            // Row for title and student count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = classItem.topic,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                // Icon with student count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (classState) {
                            ClassState.IN_PROGRESS -> Icons.Default.Person
                            ClassState.UPCOMING -> Icons.Default.Schedule
                            ClassState.FINISHED -> Icons.Default.CoPresent
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${studentsList.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.Class,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = classItem.tutoring,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(0.5f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (classState != ClassState.FINISHED) {
                        val text =
                            if (classState == ClassState.IN_PROGRESS) classItem.classroom else classItem.startTime
                        val icon =
                            if (classState == ClassState.IN_PROGRESS) Icons.Default.Place else Icons.Default.AccessTime


                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$text",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = when (classState) {
                        ClassState.IN_PROGRESS -> Icons.Default.PlayCircle
                        ClassState.UPCOMING -> Icons.Default.Schedule
                        ClassState.FINISHED -> Icons.Default.CheckCircle
                    },
                    contentDescription = classStateText,
                    tint = when (classState) {
                        ClassState.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
                        ClassState.UPCOMING -> MaterialTheme.colorScheme.primary
                        ClassState.FINISHED -> MaterialTheme.colorScheme.tertiary
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = classStateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
