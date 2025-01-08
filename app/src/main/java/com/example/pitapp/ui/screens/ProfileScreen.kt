package com.example.pitapp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.data.UserData
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.ui.components.ProfileImage
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun ProfileScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val imeNestedScrollConnection = rememberNestedScrollInteropConnection()
    var isLoading by remember { mutableStateOf(false) }
    var userData by remember { mutableStateOf<UserData?>(null) }
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var profilePictureUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var newImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var shouldDeleteImage by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        newImageUri = uri
        shouldDeleteImage = false
    }

    LaunchedEffect(Unit) {
        fireStoreManager.getUserData { result ->
            if (result.isSuccess) {
                userData = result.getOrNull()
                userData?.let {
                    name = it.name
                    surname = it.surname
                    profilePictureUrl = it.profilePictureUrl
                }
            }
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(id = R.string.profile)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .nestedScroll(imeNestedScrollConnection)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            userData?.let { user ->
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProfileImage(
                        imageUrl = when {
                            shouldDeleteImage -> null
                            newImageUri != null -> newImageUri.toString()
                            profilePictureUrl != null -> profilePictureUrl
                            else -> null
                        }
                    )

                    if (isEditing && profilePictureUrl != null && !shouldDeleteImage) {
                        OutlinedButton(
                            onClick = {
                                shouldDeleteImage = true
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .fillMaxWidth(0.35f)
                        ) {
                            Text(text = stringResource(id = R.string.delete))
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                            )
                        }
                    }
                }

                if (isEditing) {
                    Spacer(modifier = Modifier.height(32.dp))
                    OutlinedButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        val textId = if (profilePictureUrl == null && newImageUri == null) {
                            R.string.add_pic
                        } else {
                            R.string.change_pic
                        }
                        Text(stringResource(id = textId))
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(64.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(text = stringResource(id = R.string.names)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = surname,
                        onValueChange = { surname = it },
                        label = { Text(text = stringResource(id = R.string.surnames)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    Text(
                        text = surname,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isEditing) {
                    if (!isLoading) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isEditing = false
                                    shouldDeleteImage = false
                                    newImageUri = null
                                    name = user.name
                                    surname = user.surname
                                    profilePictureUrl = user.profilePictureUrl
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = stringResource(id = R.string.cancel))
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = {
                                    isLoading = true
                                    scope.launch {
                                        val result = if (shouldDeleteImage) {
                                            fireStoreManager.deleteImageFromStorage(user.profilePictureUrl)
                                            fireStoreManager.updateUserData(name, surname, null)
                                        } else {
                                            fireStoreManager.updateUserData(name, surname, newImageUri)
                                        }

                                        withContext(Dispatchers.Main) {
                                            isEditing = false
                                            if (result.isSuccess) {
                                                    newImageUri?.let {
                                                        fireStoreManager.getUserData { result ->
                                                            if (result.isSuccess) {
                                                                userData = result.getOrNull()
                                                                userData?.let {
                                                                    profilePictureUrl = it.profilePictureUrl
                                                                }
                                                            }
                                                        }
                                                        ""
                                                    } ?: user.profilePictureUrl
                                                newImageUri = null
                                                isEditing = false
                                                navController.popBackStack()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.error_saving),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }

                                            delay(1000)
                                            isLoading = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = shouldDeleteImage || name.isNotEmpty() && surname.isNotEmpty() &&
                                        (name != user.name || surname != user.surname || newImageUri != null)
                            ) {
                                Text(text = stringResource(id = R.string.save_changes))
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Update,
                                    contentDescription = null
                                )
                            }
                        }
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(64.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(32.dp))
                    OutlinedButton(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth(0.9f),
                    ) {
                        Text(stringResource(id = R.string.edit_data))
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}




