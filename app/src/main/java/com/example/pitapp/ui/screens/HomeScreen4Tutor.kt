package com.example.pitapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.data.ClassData
import com.example.pitapp.ui.components.TutorScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import com.google.firebase.Timestamp

@Composable
fun HomeScreen4Tutor(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    val currentUserEmail = authManager.getUserEmail() ?: ""
    TutorScaffold(
        navController = navController,
        authManager = authManager,
        fireStoreManager = fireStoreManager
    ) {
        TutorClassList(
            email = currentUserEmail,
            fireStoreManager = fireStoreManager,
            navController = navController
        )
    }
}

@Composable
fun TutorClassList(
    email: String,
    fireStoreManager: FireStoreManager,
    navController: NavHostController
) {

    val classes = remember { mutableStateOf<List<Pair<String, ClassData>>>(emptyList()) }

    LaunchedEffect(Unit) {
        fireStoreManager.getClasses(email) { result ->
            classes.value = result.getOrDefault(emptyList())
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "My Classes:",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(classes.value) { (documentId, classItem) ->
            val classState = determineClassState(classItem)
            val opacity = if (classState == ClassState.UPCOMING) 0.5f else 1f
            val studentCount =
                classItem.students?.size.takeIf { classState == ClassState.IN_PROGRESS } ?: 0

            ClassCard(
                classItem = classItem,
                opacity = opacity,
                studentCount = studentCount,
                classState = classState,
                onClick = {
                    navController.navigate("classDetailScreen/$documentId")
                }
            )
        }
    }
}


enum class ClassState { IN_PROGRESS, UPCOMING, FINISHED }

fun determineClassState(classData: ClassData): ClassState {
    val now = Timestamp.now().seconds
    return when {
        classData.realDuration != null -> ClassState.FINISHED
        classData.startTime.seconds <= now -> ClassState.IN_PROGRESS
        else -> ClassState.UPCOMING
    }
}

@Composable
fun ClassCard(
    classItem: ClassData,
    opacity: Float,
    studentCount: Int,
    classState: ClassState,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onClick() }
            .alpha(opacity)
    ) {
        HorizontalDivider()
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = classItem.topic, style = MaterialTheme.typography.titleLarge)
            Text(text = "Tutor: ${classItem.email}")
            Text(
                text = "Classroom: ${classItem.classroom}",
                style = MaterialTheme.typography.bodySmall
            )
            if (studentCount > 0) {
                Text(
                    text = "No. of Students: $studentCount",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            // Display class state visually
            Text(
                text = when (classState) {
                    ClassState.IN_PROGRESS -> "Class is in progress"
                    ClassState.UPCOMING -> "Class has not started yet"
                    ClassState.FINISHED -> "Class has finished"
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
        HorizontalDivider()
    }
}
