package com.example.pitapp.ui.features.classes.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.datasource.FireStoreManager
import com.example.pitapp.model.SavedClass
import com.example.pitapp.model.SavedStudent
import com.example.pitapp.ui.features.classes.components.AttendanceStatsCard
import com.example.pitapp.ui.features.classes.components.ClassSummaryCard
import com.example.pitapp.ui.features.classes.components.StudentRow
import com.example.pitapp.ui.features.classes.helpers.generateStudentsCsv
import com.example.pitapp.ui.features.classes.helpers.saveFileToDownloads
import com.example.pitapp.ui.features.classes.helpers.shareFile
import com.example.pitapp.ui.shared.components.BackScaffold
import com.example.pitapp.ui.shared.components.EmptyState

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun InstantClassSummaryScreen(
    classId: String,
    authManager: AuthManager,
    navController: NavHostController,
    fireStoreManager: FireStoreManager
) {
    val savedClass = remember { mutableStateOf<SavedClass?>(null) }
    val students = remember { mutableStateOf<List<SavedStudent>>(emptyList()) }
    val userEmail = authManager.getUserEmail() ?: ""
    val isLoading = remember { mutableStateOf(true) }
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }

    val filteredStudents = remember(searchQuery, students.value) {
        if (searchQuery.isBlank()) {
            students.value
        } else {
            val query = searchQuery.trim().lowercase()
            students.value.filter { student ->
                student.name.lowercase().contains(query) ||
                        student.email.lowercase().contains(query) ||
                        student.studentId.lowercase().contains(query) ||
                        student.academicProgram.lowercase().contains(query)
            }
        }
    }

    LaunchedEffect(classId) {
        isLoading.value = true
        var classLoaded = false
        var studentsLoaded = false

        val checkCompletion = {
            if (classLoaded && studentsLoaded) {
                isLoading.value = false
            }
        }

        fireStoreManager.getInstantClassDetails(classId) { result ->
            result.onSuccess { retrievedClass ->
                savedClass.value = retrievedClass
            }.onFailure {
                savedClass.value = null
            }
            classLoaded = true
            checkCompletion()
        }

        fireStoreManager.getStudentsNow(classId) { studentResult ->
            studentResult.onSuccess { studentList ->
                students.value = studentList.sortedBy { it.name }
            }.onFailure {
                students.value = emptyList()
            }
            studentsLoaded = true
            checkCompletion()
        }
    }


    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(id = R.string.instant_class_summary),
    ) {

        when {
            isLoading.value -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            savedClass.value != null -> {
                val currentClass = savedClass.value!!
                val currentStudents = students.value

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ClassSummaryCard(
                            userEmail = userEmail,
                            savedClass = currentClass,
                            enabled = true,
                            onClick = {
                                val csvFile =
                                    generateStudentsCsv(
                                        listOf(currentClass to currentStudents),
                                        context
                                    )
                                shareFile(context, csvFile)
                                saveFileToDownloads(context, csvFile, "text/csv")
                            }
                        )
                    }

                    if (currentStudents.isNotEmpty()) {
                        item {
                            AttendanceStatsCard(students = currentStudents)
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = {
                                Text(
                                    text = stringResource(
                                        id = R.string.search_students,
                                        filteredStudents.size
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (searchQuery.isNotEmpty()) searchQuery = ""
                                    }
                                ) {
                                    if (searchQuery.isNotEmpty())
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null
                                        )
                                    else
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null
                                        )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    if (filteredStudents.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.PersonSearch,
                                message = stringResource(id = R.string.no_students_found)
                            )
                        }
                    } else {
                        items(filteredStudents) { student ->
                            StudentRow(student = student)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_class_info),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
