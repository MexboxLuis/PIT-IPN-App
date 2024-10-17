package com.example.pitapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.pitapp.data.UserData
import com.example.pitapp.ui.components.MainScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import kotlinx.coroutines.launch

@Composable
fun RequestsScreen(
    navController: NavHostController,
    authManager: AuthManager,
    firestoreManager: FireStoreManager
) {
    var users by remember { mutableStateOf<List<UserData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Escuchar actualizaciones en tiempo real de la colección 'saved_users'
    LaunchedEffect(Unit) {
        firestoreManager.getAllUsersSnapshot { result ->
            if (result.isSuccess) {
                // Filtrar solo los usuarios con permiso 0
                users = result.getOrNull()?.filter { it.permission == 0 } ?: emptyList()
            } else if (result.isFailure) {
                errorMessage = result.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }

    MainScaffold(
        navController = navController,
        authManager = authManager,
        firestoreManager = firestoreManager
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text(
                text = errorMessage ?: "Error desconocido",
                color = MaterialTheme.colorScheme.error
            )
        } else {
            LazyColumn {
                items(users) { user ->
                    UserRow(user, firestoreManager)
                }
            }
        }
    }
}


@Composable
fun UserRow(user: UserData, firestoreManager: FireStoreManager) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (user.profilePictureUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(user.profilePictureUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default Profile Icon",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = "${user.name} ${user.surname}",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(text = user.email, style = MaterialTheme.typography.bodySmall)

        }

        IconButton(
            onClick = {
                scope.launch {
                    val result = firestoreManager.updateUserPermission(user.email, 1)  // Usar el email para buscar el documento
                    if (result.isSuccess) {
                        println("Permiso actualizado correctamente.")
                        // Aquí actualizas la lista de usuarios si es necesario para reflejar el cambio en la UI
                        // users = users.map { if (it.email == user.email) it.copy(permission = 1) else it }
                    } else if (result.isFailure) {
                        println("Error al actualizar el permiso: ${result.exceptionOrNull()?.message}")
                    }
                }
            }
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
        }


    }
}
