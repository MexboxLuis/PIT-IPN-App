package com.example.pitapp.ui.features.permissions.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.pitapp.model.UserData
import com.example.pitapp.datasource.FireStoreManager
import kotlinx.coroutines.launch

@Composable
fun UserRow(user: UserData, fireStoreManager: FireStoreManager) {
    LocalContext.current
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
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Serif
            )
            Text(text = user.email, style = MaterialTheme.typography.bodySmall)
        }

        var isLoading by remember { mutableStateOf(false) }

        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center),
                    strokeWidth = 3.dp
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                try {
                                    when (user.permission) {
                                        1 -> {
                                            fireStoreManager.updateUserPermission(user.email, 0)
                                        }

                                        0 -> {
                                            fireStoreManager.updateUserPermission(user.email, -2)
                                        }

                                        else -> {
                                            fireStoreManager.updateUserPermission(user.email, 0)
                                        }
                                    }
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = when (user.permission) {
                                1 -> Icons.Default.ArrowCircleDown
                                0 -> Icons.Default.PersonOff
                                else -> Icons.Default.PersonAddAlt1
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (user.permission == 0 || user.permission == 1) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    try {
                                        when (user.permission) {
                                            0 -> {
                                                fireStoreManager.updateUserPermission(user.email, 1)
                                            }

                                            1 -> {
                                                fireStoreManager.updateUserPermission(user.email, 2)
                                            }
                                        }
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = when (user.permission) {
                                    0 -> Icons.Default.CheckCircle
                                    1 -> Icons.Default.ArrowCircleUp
                                    else -> Icons.Default.CheckCircle
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}