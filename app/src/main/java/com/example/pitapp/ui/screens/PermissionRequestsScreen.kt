package com.example.pitapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.pitapp.R
import com.example.pitapp.data.UserData
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import kotlinx.coroutines.launch

@Composable
fun PermissionRequestsScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    var users by remember { mutableStateOf<List<UserData>>(emptyList()) }
    var rejectedUsers by remember { mutableStateOf<List<UserData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        fireStoreManager.getAllUsersSnapshot { result ->
            if (result.isSuccess) {
                users = result.getOrNull()?.filter { it.permission == 0 } ?: emptyList()
                rejectedUsers = result.getOrNull()?.filter { it.permission == -2 } ?: emptyList()
            } else if (result.isFailure) {
                errorMessage = result.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(id = R.string.permission_requests_title)
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            ErrorScreen()
        } else if (users.isEmpty() && rejectedUsers.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonOff,
                    contentDescription = null,
                    modifier = Modifier.size(128.dp)
                )
                Text(text = stringResource(id = R.string.no_requests))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                LazyColumn {
                    items(users) { user ->
                        UserRow(user, fireStoreManager)
                    }
                    if (rejectedUsers.isNotEmpty()) {
                        item {
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Rejected users",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()

                        }
                        items(rejectedUsers) { user ->
                            UserRow(user, fireStoreManager)
                        }

                    }
                }

            }
        }
    }
}


@Composable
fun UserRow(user: UserData, fireStoreManager: FireStoreManager) {

    val scope = rememberCoroutineScope()
    var classCount by remember { mutableIntStateOf(0) }
    if (user.permission == 1) {

        LaunchedEffect(Unit) {
            fireStoreManager.getClasses(user.email) { result ->
                if (result.isSuccess) {
                    classCount = result.getOrNull()?.size ?: 0
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (user.profilePictureUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(user.profilePictureUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = "${user.name} ${user.surname}",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(text = user.email, style = MaterialTheme.typography.bodySmall)
            if (user.permission == 1) {
                Text(
                    text = "Cantidad de clases: $classCount",
                    style = MaterialTheme.typography.bodySmall
                )
            }

        }

        if (user.permission == 0 || user.permission == 1)
            IconButton(
                onClick = {
                    scope.launch {
                        when (user.permission) {
                            0 -> {
                                val result = fireStoreManager.updateUserPermission(user.email, 1)
                                if (result.isSuccess) {
                                    println("Permiso actualizado correctamente.")
                                } else if (result.isFailure) {
                                    println("Error al actualizar el permiso: ${result.exceptionOrNull()?.message}")
                                }

                            }

                            1 -> {
                                val result = fireStoreManager.updateUserPermission(user.email, 2)
                                if (result.isSuccess) {
                                    println("Permiso actualizado correctamente.")
                                }
                            }

                        }
                    }
                }
            ) {
                Icon(
                    imageVector = when (user.permission) {
                        0 -> Icons.Default.CheckCircle
                        1 -> Icons.Default.ArrowCircleUp
                        else -> {
                            Icons.Default.CheckCircle
                        }
                    }, contentDescription = null
                )
            }


        IconButton(
            onClick = {
                scope.launch {
                    when (user.permission) {
                        1 -> {
                            val result = fireStoreManager.updateUserPermission(user.email, 0)
                            if (result.isSuccess) {
                                println("Permiso actualizado correctamente.")
                            }
                        }

                        0 -> {
                            val result = fireStoreManager.updateUserPermission(user.email, -2)
                            if (result.isSuccess)
                                println("Permiso actualizado correctamente.")
                            else if (result.isFailure) {
                                println("Error al rechazar la solicitud: ${result.exceptionOrNull()?.message}")
                            }
                        }

                    }
                }
            }
        ) {
            Icon(
                imageVector = when (user.permission) {
                    1 -> Icons.Default.ArrowCircleDown
                    0 -> Icons.Default.Cancel
                    else -> Icons.Default.Cancel
                }, contentDescription = null
            )
        }


    }
}
