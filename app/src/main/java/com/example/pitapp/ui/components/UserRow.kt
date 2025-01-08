package com.example.pitapp.ui.components

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.pitapp.R
import com.example.pitapp.data.UserData
import com.example.pitapp.utils.FireStoreManager
import kotlinx.coroutines.launch

@Composable
fun UserRow(user: UserData, fireStoreManager: FireStoreManager) {
    val context = LocalContext.current
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
                    text = stringResource(id = R.string.class_count, classCount),
                    style = MaterialTheme.typography.bodySmall
                )
            }

        }

        var isLoading by remember { mutableStateOf(false) }

        IconButton(
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        when (user.permission) {
                            1 -> {
                                val result = fireStoreManager.updateUserPermission(user.email, 0)
                                if (result.isSuccess) {
                                    Toast.makeText(
                                        context,
                                        R.string.descend_to_guest,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            0 -> {
                                val result = fireStoreManager.updateUserPermission(user.email, -2)
                                if (result.isSuccess) {
                                    Toast.makeText(
                                        context,
                                        R.string.reject_as_tutor,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        R.string.error_reject_request,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            else -> {
                                val result = fireStoreManager.updateUserPermission(user.email, 0)
                                if (result.isSuccess) {
                                    Toast.makeText(
                                        context,
                                        R.string.descend_to_guest,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        R.string.permission_error,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = when (user.permission) {
                        1 -> Icons.Default.ArrowCircleDown
                        0 -> Icons.Default.PersonOff
                        else -> Icons.Default.PersonAddAlt1
                    },
                    contentDescription = null
                )
            }
        }


        if (user.permission == 0 || user.permission == 1) {

            IconButton(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            when (user.permission) {
                                0 -> {
                                    val result =
                                        fireStoreManager.updateUserPermission(user.email, 1)
                                    if (result.isSuccess) {
                                        Toast.makeText(
                                            context,
                                            R.string.ascend_to_tutor,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            R.string.permission_error,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                1 -> {
                                    val result =
                                        fireStoreManager.updateUserPermission(user.email, 2)
                                    if (result.isSuccess) {
                                        Toast.makeText(
                                            context,
                                            R.string.ascend_to_admin,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = when (user.permission) {
                            0 -> Icons.Default.CheckCircle
                            1 -> Icons.Default.ArrowCircleUp
                            else -> Icons.Default.CheckCircle
                        },
                        contentDescription = null
                    )
                }
            }
        }
    }
}