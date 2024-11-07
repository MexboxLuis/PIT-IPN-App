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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.pitapp.R
import com.example.pitapp.data.UserData
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager

@Composable
fun TutorsScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    var tutors by remember { mutableStateOf<List<UserData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        fireStoreManager.getAllUsersSnapshot { result ->
            if (result.isSuccess) {
                tutors = result.getOrNull()?.filter { it.permission == 1 } ?: emptyList()
            } else if (result.isFailure) {
                errorMessage = result.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(id = R.string.requests)
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error
            )
        } else {
            LazyColumn {
                items(tutors) { tutor ->
                    TutorCard(tutor, fireStoreManager)
                }
            }
        }
    }
}

@Composable
fun TutorCard(tutor: UserData, fireStoreManager: FireStoreManager) {

    var classCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(tutor.email) {
        fireStoreManager.fetchClassesForTutor { result ->
            if (result.isSuccess) {
                classCount = result.getOrNull()?.size ?: 0
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (tutor.profilePictureUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(tutor.profilePictureUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${tutor.name} ${tutor.surname}",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(text = tutor.email, style = MaterialTheme.typography.bodySmall)
            Text(text = "Cantidad de clases: $classCount", style = MaterialTheme.typography.bodySmall)
        }
    }
}
