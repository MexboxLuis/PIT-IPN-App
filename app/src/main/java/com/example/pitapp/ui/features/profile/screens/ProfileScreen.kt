package com.example.pitapp.ui.features.profile.screens

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.datasource.FireStoreManager
import com.example.pitapp.ui.features.profile.components.ProfileImage
import com.example.pitapp.ui.features.profile.helpers.formatPhoneNumber
import com.example.pitapp.ui.shared.components.BackScaffold
import kotlinx.coroutines.Dispatchers
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

    var newImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var isPhoneError by remember { mutableStateOf(false) }
    var shouldDeleteImage by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        newImageUri = uri
        shouldDeleteImage = false
    }

    val userResult by fireStoreManager.getUserData().collectAsState(initial = null)
    val userData = userResult?.getOrNull()

    var name by rememberSaveable { mutableStateOf("") }
    var surname by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var profilePictureUrl by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(userData) {
        if (!isEditing && userData != null) {
            name = userData.name
            surname = userData.surname
            phone = userData.phoneNumber.orEmpty()
            profilePictureUrl = userData.profilePictureUrl
        }
    }

    LaunchedEffect(userData, isEditing) {
        if (!isEditing) {
            newImageUri = null
            shouldDeleteImage = false
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
            if (userResult == null && !isLoading) {
                CircularProgressIndicator()
            } else if (userResult?.isFailure == true) {
                val errorMessage = userResult?.exceptionOrNull()?.message.orEmpty()
                Text(text = stringResource(id = R.string.error_loading_user, errorMessage))
            } else {
                userData?.let { user ->
                    val isStudent = user.studentId != null || user.academicProgram != null
                    val currentDisplayImageUrl = when {
                        shouldDeleteImage -> null
                        newImageUri != null -> newImageUri.toString()
                        else -> profilePictureUrl
                    }

                    Box(
                        contentAlignment = Alignment.TopCenter,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfileImage(imageUrl = currentDisplayImageUrl)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            isStudent -> stringResource(id = R.string.student)
                            userData.permission == 2 -> stringResource(id = R.string.admin)
                            else -> stringResource(id = R.string.tutor)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )


                    Spacer(modifier = Modifier.height(16.dp))
                    if (isEditing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {

                            OutlinedButton(
                                onClick = { launcher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth(0.5f)
                            ) {
                                val textId = if (currentDisplayImageUrl == null) {
                                    R.string.add_pic
                                } else {
                                    R.string.change_pic
                                }
                                Text(text = stringResource(id = textId))
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            if (isEditing && profilePictureUrl != null && !shouldDeleteImage && newImageUri == null) {
                                OutlinedButton(
                                    onClick = {
                                        shouldDeleteImage = true
                                        newImageUri = null
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }


                    if (isEditing) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(text = stringResource(id = R.string.names)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = false
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
                            singleLine = true,
                            enabled = false
                        )
                    } else {
                        Text(
                            text = surname,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))


                    user.studentId?.let {
                        if (isEditing)
                            OutlinedTextField(
                                value = it,
                                onValueChange = {},
                                label = { Text(text = stringResource(id = R.string.student_id)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                readOnly = true,
                                enabled = false
                            )
                        else
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium
                            )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    user.academicProgram?.let {
                        if (isEditing)
                            OutlinedTextField(
                                value = it,
                                onValueChange = {},
                                label = { Text(text = stringResource(id = R.string.academic_program)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                readOnly = true,
                                enabled = false
                            )
                        else
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium
                            )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() } && newValue.length <= 10) {
                                    phone = newValue
                                    isPhoneError = newValue.length != 10
                                }
                            },
                            label = { Text(text = stringResource(id = R.string.phone_number)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = isPhoneError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )



                        if (isPhoneError)
                            Text(
                                text = stringResource(id = R.string.error_phone_digits),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                    } else if (phone.isNotBlank()) {
                        Text(
                            text = formatPhoneNumber(phone),
                            style = MaterialTheme.typography.bodyMedium
                        )
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
                                        name = user.name
                                        surname = user.surname
                                        phone = user.phoneNumber.orEmpty()
                                        profilePictureUrl = user.profilePictureUrl
                                        newImageUri = null
                                        shouldDeleteImage = false
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
                                            val result = when {
                                                shouldDeleteImage -> {
                                                    fireStoreManager.updateUserData(
                                                        name = name,
                                                        surname = surname,
                                                        newImageUri = null,
                                                        phoneNumber = phone,
                                                        setProfilePictureToNull = true
                                                    )
                                                }

                                                newImageUri != null -> {
                                                    fireStoreManager.updateUserData(
                                                        name = name,
                                                        surname = surname,
                                                        newImageUri = newImageUri,
                                                        phoneNumber = phone,
                                                        setProfilePictureToNull = false
                                                    )
                                                }

                                                else -> {
                                                    fireStoreManager.updateUserData(
                                                        name = name,
                                                        surname = surname,
                                                        newImageUri = null,
                                                        phoneNumber = phone,
                                                        setProfilePictureToNull = false
                                                    )
                                                }
                                            }

                                            withContext(Dispatchers.Main) {
                                                if (result.isSuccess) {
                                                    Toast.makeText(
                                                        context,
                                                        context.getString(R.string.success_profile_saved),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    navController.popBackStack()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        result.exceptionOrNull()?.message
                                                            ?: context.getString(R.string.error_saving),
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                                isEditing = false
                                                newImageUri = null
                                                shouldDeleteImage = false
                                                isLoading = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = {
                                        val hasTextChanged = name != user.name ||
                                                surname != user.surname ||
                                                phone != (user.phoneNumber ?: "")
                                        val hasImageChanged =
                                            newImageUri != null || (shouldDeleteImage && user.profilePictureUrl != null)
                                        val isNameValid = name.isNotBlank()
                                        val isSurnameValid = surname.isNotBlank()

                                        (hasTextChanged || hasImageChanged) &&
                                                isNameValid &&
                                                isSurnameValid &&
                                                !isPhoneError
                                    }()
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
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                    } else {
                        Spacer(modifier = Modifier.height(64.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(32.dp))
                        OutlinedButton(
                            onClick = { isEditing = true },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Text(text = stringResource(id = R.string.edit_data))
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        }
                    }
                } ?: run {
                    if (userResult?.isSuccess == true) {
                        Text(text = stringResource(id = R.string.no_user_data))
                    }
                }
            }
        }
    }
}



