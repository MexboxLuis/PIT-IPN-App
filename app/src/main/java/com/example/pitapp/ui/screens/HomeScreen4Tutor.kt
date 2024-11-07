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
    val classes = remember { mutableStateOf<List<ClassData>>(emptyList()) }
    val currentUserEmail = authManager.getUserEmail() ?: ""

    LaunchedEffect(currentUserEmail) {
        fireStoreManager.fetchClassesForTutor { fetchedClasses ->
            classes.value = fetchedClasses.getOrDefault(emptyList())
        }
    }
    TutorScaffold(
        navController = navController,
        authManager = authManager,
        fireStoreManager = fireStoreManager
    ) {

        LazyColumn {
            item {
                Text(
                    text = "My Classes:",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(classes.value) { classItem ->
                val isUpcoming = classItem.startTime.seconds > Timestamp.now().seconds
                val opacity = if (isUpcoming) 0.5f else 1f // Apply opacity if upcoming
                val studentCount = classItem.students.size.takeIf { !isUpcoming }
                    ?: 0 // Show students only if started

                ClassCard(
                    classItem = classItem,
                    opacity = opacity,
                    studentCount = studentCount,
                    onClick = {

                    }
                )
            }
        }
    }
}

@Composable
fun ClassCard(classItem: ClassData, opacity: Float, studentCount: Int, onClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(vertical = 8.dp, horizontal = 16.dp)
        .clickable { onClick() }
        .alpha(opacity)) {

        HorizontalDivider()
        Column(modifier = Modifier.padding(16.dp)) {

            Text(text = classItem.topic, style = MaterialTheme.typography.titleLarge)
            Text(text = "Tutor: ${classItem.email}")
            Text(
                text = "Classroom: ${classItem.classroom}",
                style = MaterialTheme.typography.bodySmall
            )
            if (studentCount >= 0) {
                Text(
                    text = "No. of Students: $studentCount",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            ///time left
            if (classItem.expectedDuration == null) {
//                Text(
//                    text = ,
//                    style = MaterialTheme.typography.bodySmall
//                )
            }
            else {
                val startTime = classItem.startTime.toDate()
                val expectedTime = classItem.expectedDuration.let {
                    startTime.time + it * 60 * 1000
                }
                val timeDifference = expectedTime.minus(System.currentTimeMillis()) ?: 0
                Text(
                    text = if (timeDifference <= 0) "Class Finished" else "Class time left: ${timeDifference / (1000 * 60)} minutes",
                    style = MaterialTheme.typography.bodySmall
                )
            }

        }
        HorizontalDivider()
    }
}




