package com.example.pitapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.data.ClassData
import com.example.pitapp.data.UserData
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager

@Composable
fun TutorClassesScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    var users by remember { mutableStateOf<List<UserData>>(emptyList()) }
    val classesByUser =
        remember { mutableStateOf<Map<String, List<Pair<String, ClassData>>>>(emptyMap()) }
    var allClasses by remember { mutableStateOf<List<Pair<String, ClassData>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val hiddenClassesByUser = remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var searchQuery by remember { mutableStateOf("") }
    var isGroupedByTutor by remember { mutableStateOf(true) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        fireStoreManager.getAllUsersSnapshot { result ->
            if (result.isSuccess) {
                users = result.getOrNull()?.filter { it.permission == 1 } ?: emptyList()
                users.forEach { user ->
                    fireStoreManager.getClassesByEmail(user.email) { classResult ->
                        if (classResult.isSuccess) {
                            classesByUser.value = classesByUser.value.toMutableMap().apply {
                                this[user.email] = classResult.getOrDefault(emptyList())
                            }
                            hiddenClassesByUser.value =
                                hiddenClassesByUser.value.toMutableMap().apply {
                                    this[user.email] = true
                                }
                            allClasses = classesByUser.value.values.flatten()
                        } else {
                            errorMessage =
                                context.getString(R.string.loading_classes_error, user.email)

                        }
                    }
                }
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(id = R.string.tutor_classes_title)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (!errorMessage.isNullOrEmpty()) {
                Text(text = errorMessage ?: stringResource(id = R.string.unknown_error))
            } else if (allClasses.isEmpty()) {

            }
        }
    }
}

