package com.example.pitapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.ui.components.AdminScaffold
import com.example.pitapp.ui.components.TutorScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager

@Composable
fun HomeScreen4Admin(
    navController: NavHostController,
    authManager: AuthManager,
    firestoreManager: FireStoreManager,
    onPermissionRequestsClick: () -> Unit,
    onTutorsClick: () -> Unit,
    onClassesClick: () -> Unit
) {

    var isGridView by remember { mutableStateOf(false) }

    AdminScaffold(
        navController = navController,
        fireStoreManager = firestoreManager
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (!isGridView) Icons.Default.GridView else Icons.Default.ViewStream,
                            contentDescription = null
                        )
                    }
                }

            }

            item {
                AdminCard(
                    title = stringResource(id = R.string.permission_requests_title),
                    subtitle = stringResource(id = R.string.permission_requests_subtitle),
                    image = Icons.Default.Email,
                    onClick = { onPermissionRequestsClick() }
                )
            }
            item {
                AdminCard(
                    title = stringResource(id = R.string.tutors_title),
                    subtitle = stringResource(id = R.string.tutors_subtitle),
                    image = Icons.Default.CoPresent,
                    onClick = { onTutorsClick() }
                )
            }
            item {
                AdminCard(
                    title = stringResource(id = R.string.classes_title),
                    subtitle = stringResource(id = R.string.classes_subtitle),
                    image = Icons.Default.Class,
                    onClick = { onClassesClick() }
                )
            }
        }
    }
}

@Composable
fun AdminCard(
    title: String,
    subtitle: String,
    image: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = image,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

