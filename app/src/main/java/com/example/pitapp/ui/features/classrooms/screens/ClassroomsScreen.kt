package com.example.pitapp.ui.features.classrooms.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.datasource.FireStoreManager
import com.example.pitapp.model.Classroom
import com.example.pitapp.ui.features.classrooms.components.ClassroomCard
import com.example.pitapp.ui.features.classrooms.components.ClassroomDialog
import com.example.pitapp.ui.features.classrooms.components.DeleteClassroomDialog
import com.example.pitapp.ui.features.classrooms.components.SortOption
import com.example.pitapp.ui.shared.components.BackScaffold
import java.util.Locale


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ClassroomsScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    val context = LocalContext.current
    val classrooms = remember { mutableStateOf<List<Classroom>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val classroomToEdit = remember { mutableStateOf<Classroom?>(null) }
    val showAddDialog = remember { mutableStateOf(false) }
    val addDialogError = remember { mutableStateOf<String?>(null) }
    val editDialogError = remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.NUMBER_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }

    var classroomToDelete by remember { mutableStateOf<Classroom?>(null) }
    var showDeleteClassroomDialog by remember { mutableStateOf(false) }
    var scale by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(Unit) {
        isLoading.value = true
        fireStoreManager.getClassrooms { result ->
            result.onSuccess { list ->
                classrooms.value = list
                isLoading.value = false
                errorMessage.value = null
            }.onFailure { error ->
                errorMessage.value =
                    error.localizedMessage ?: context.getString(R.string.unknown_error)
                isLoading.value = false
            }
        }
        animate(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        ) { value, _ ->
            scale = value
        }
    }

    val filteredAndSortedClassrooms = remember(classrooms.value, searchQuery, sortOption) {
        val filtered = classrooms.value.filter {
            it.number.toString().contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
        when (sortOption) {
            SortOption.NUMBER_DESC -> filtered.sortedByDescending { it.number }
            SortOption.NUMBER_ASC -> filtered.sortedBy { it.number }
            SortOption.DESCRIPTION_ASC -> filtered.sortedBy { it.description.lowercase(Locale.getDefault()) }
            SortOption.DESCRIPTION_DESC -> filtered.sortedByDescending {
                it.description.lowercase(
                    Locale.getDefault()
                )
            }
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(id = R.string.classrooms_title),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                if (classrooms.value.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(text = stringResource(R.string.search_classrooms)) },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Filled.Clear, contentDescription = null)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.3f
                                )
                            )
                        )

                        Spacer(Modifier.width(8.dp))


                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(R.string.sort_number_asc)) },
                                    onClick = {
                                        sortOption = SortOption.NUMBER_ASC; showSortMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.ArrowUpward,
                                            contentDescription = null
                                        )
                                    },
                                    trailingIcon = {
                                        if (sortOption == SortOption.NUMBER_ASC) Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(R.string.sort_number_desc)) },
                                    onClick = {
                                        sortOption = SortOption.NUMBER_DESC; showSortMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.ArrowDownward,
                                            contentDescription = null
                                        )
                                    },
                                    trailingIcon = {
                                        if (sortOption == SortOption.NUMBER_DESC) Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(R.string.sort_description_asc)) },
                                    onClick = {
                                        sortOption = SortOption.DESCRIPTION_ASC; showSortMenu =
                                        false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.SortByAlpha,
                                            contentDescription = null
                                        )
                                    },
                                    trailingIcon = {
                                        if (sortOption == SortOption.DESCRIPTION_ASC)
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = null
                                            )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(R.string.sort_description_desc)) },
                                    onClick = {
                                        sortOption = SortOption.DESCRIPTION_DESC; showSortMenu =
                                        false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.SortByAlpha,
                                            contentDescription = null
                                        )
                                    },
                                    trailingIcon = {
                                        if (sortOption == SortOption.DESCRIPTION_DESC)
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = null
                                            )
                                    }
                                )
                            }
                        }
                    }
                }

                when {
                    isLoading.value -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    errorMessage.value != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = errorMessage.value ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }

                    filteredAndSortedClassrooms.isEmpty() && searchQuery.isNotEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.SearchOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.no_classrooms_found),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    filteredAndSortedClassrooms.isEmpty() && classrooms.value.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.MeetingRoom,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.no_classrooms_yet),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = {
                                    addDialogError.value = null
                                    showAddDialog.value = true
                                }) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(text = stringResource(R.string.add_classroom))
                                }
                            }
                        }
                    }

                    filteredAndSortedClassrooms.isEmpty() && classrooms.value.isNotEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.classroom_no_match),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredAndSortedClassrooms, key = { it.number }) { classroom ->
                                ClassroomCard(
                                    classroom = classroom,
                                    onEditClick = {
                                        editDialogError.value = null
                                        classroomToEdit.value = classroom
                                    },
                                    onDeleteClick = {
                                        classroomToDelete = classroom
                                        showDeleteClassroomDialog = true
                                    },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }

            if (!isLoading.value && errorMessage.value == null && classrooms.value.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        addDialogError.value = null
                        showAddDialog.value = true
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    Text(
                        text = stringResource(R.string.add_classroom),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.AddCircleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        classroomToEdit.value?.let { originalClassroom ->
            val title =
                stringResource(R.string.edit_classroom_dialog_title_prefix) + " ${originalClassroom.number}"
            ClassroomDialog(
                titleIcon = Icons.Outlined.Edit,
                title = title,
                initialNumber = originalClassroom.number.toString(),
                initialDescription = originalClassroom.description,
                errorMessage = editDialogError.value,
                isEditMode = true,
                onConfirm = { newNumber, newDescription ->
                    if (newNumber != originalClassroom.number) {
                        editDialogError.value =
                            context.getString(R.string.error_cannot_change_number_edit)
                    } else {
                        val updatedClassroom = originalClassroom.copy(description = newDescription)
                        if (updatedClassroom != originalClassroom) {
                            fireStoreManager.updateClassroom(updatedClassroom) { result ->
                                result.onSuccess {
                                    classroomToEdit.value = null
                                    editDialogError.value = null
                                }
                                result.onFailure { e ->

                                    editDialogError.value =
                                        context.getString(R.string.error_update_failed_prefix) + (e.localizedMessage
                                            ?: context.getString(R.string.unknown_error))
                                }
                            }
                        } else {
                            classroomToEdit.value = null
                            editDialogError.value = null
                        }
                    }
                },
                onDismiss = {
                    classroomToEdit.value = null
                    editDialogError.value = null
                }
            )
        }

        if (showAddDialog.value) {
            ClassroomDialog(
                titleIcon = Icons.Outlined.AddCircleOutline,
                title = stringResource(R.string.add_classroom_dialog_title),
                initialNumber = "",
                initialDescription = "",
                errorMessage = addDialogError.value,
                isEditMode = false,
                onConfirm = { newNumber, newDescription ->
                    val trimmedDescription = newDescription.trim()
                    if (classrooms.value.any { it.number == newNumber }) {
                        addDialogError.value =
                            context.getString(R.string.error_classroom_exists, newNumber)
                    } else {
                        val newClassroom =
                            Classroom(number = newNumber, description = trimmedDescription)
                        fireStoreManager.addClassroom(newClassroom) { result ->
                            result.onSuccess {
                                showAddDialog.value = false
                                addDialogError.value = null
                            }
                            result.onFailure { e ->
                                addDialogError.value =
                                    context.getString(R.string.error_add_failed_prefix) + (e.localizedMessage
                                        ?: context.getString(R.string.unknown_error))
                            }
                        }
                    }
                },
                onDismiss = {
                    showAddDialog.value = false
                    addDialogError.value = null
                }
            )
        }

        if (showDeleteClassroomDialog) {
            DeleteClassroomDialog(
                classroom = classroomToDelete,
                onConfirm = {
                    classroomToDelete?.let { classToDelete ->
                        fireStoreManager.deleteClassroom(classToDelete.number) { }
                    }
                    showDeleteClassroomDialog = false
                    classroomToDelete = null
                },
                onDismiss = {
                    showDeleteClassroomDialog = false
                    classroomToDelete = null
                }
            )
        }
    }
}