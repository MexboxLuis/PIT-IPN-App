package com.example.pitapp.ui.features.auth.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.datasource.FireStoreManager
import com.example.pitapp.ui.features.auth.components.AuthActionButton
import com.example.pitapp.ui.features.auth.components.AuthHeader
import com.example.pitapp.ui.features.auth.components.AuthScreenBaseLayout
import com.example.pitapp.ui.features.auth.components.GenericTextField
import com.example.pitapp.ui.features.auth.components.ProfileImagePicker
import com.example.pitapp.ui.features.auth.components.UserRole
import com.example.pitapp.ui.shared.components.BackScaffold
import kotlinx.coroutines.launch


@Composable
fun RegisterAllDataScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager,
    email: String,
    onRegisterDataSuccess: () -> Unit
) {
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri
        }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var name by rememberSaveable { mutableStateOf("") }
    var surname by rememberSaveable { mutableStateOf("") }

    var selectedRole by remember { mutableStateOf(UserRole.TEACHER) }

    var academicProgram by rememberSaveable { mutableStateOf("") }
    var studentId by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }

    var isLoadingScreen by remember { mutableStateOf(false) }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = null
    ) {
        AuthScreenBaseLayout(isLoading = isLoadingScreen) {
            AuthHeader()
            Text(
                text = stringResource(id = R.string.your_email_is, email),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            ProfileImagePicker(
                imageUri = imageUri,
                onImagePick = { launcher.launch("image/*") }
            )
            Spacer(modifier = Modifier.height(24.dp))
            val roles = UserRole.entries.toTypedArray()
            TabRow(
                selectedTabIndex = selectedRole.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                roles.forEach { role ->
                    Tab(
                        selected = selectedRole == role,
                        onClick = {
                            selectedRole = role

                            if (role == UserRole.TEACHER) {
                                academicProgram = ""
                                studentId = ""
                                phoneNumber = ""
                            }
                        },
                        text = { Text(stringResource(id = role.displayNameResId)) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }



            Spacer(modifier = Modifier.height(16.dp))

            GenericTextField(
                value = name,
                onValueChange = { name = it },
                labelResId = R.string.names,
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(16.dp))

            GenericTextField(
                value = surname,
                onValueChange = { surname = it },
                labelResId = R.string.surnames,
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(16.dp))

            GenericTextField(
                value = phoneNumber,
                onValueChange = { newValue ->
                    if (newValue.length <= 10 && newValue.all { it.isDigit() }) {
                        phoneNumber = newValue
                    }
                },
                labelResId = R.string.phone_number,
                keyboardType = KeyboardType.Phone,
                imeAction = if (selectedRole == UserRole.TEACHER) ImeAction.Done else ImeAction.Next,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedRole == UserRole.STUDENT) {
                GenericTextField(
                    value = academicProgram,
                    onValueChange = { academicProgram = it },
                    labelResId = R.string.academic_program,
                    imeAction = ImeAction.Next
                )
                Spacer(modifier = Modifier.height(16.dp))

                GenericTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    labelResId = R.string.student_id,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
                Spacer(modifier = Modifier.height(16.dp))


            }

            val isStudentFieldsValid = if (selectedRole == UserRole.STUDENT) {
                academicProgram.isNotBlank() && studentId.isNotBlank()
            } else {
                true
            }

            val isFormEnabled =
                name.isNotBlank() && surname.isNotBlank() && phoneNumber.length == 10 && isStudentFieldsValid

            AuthActionButton(
                textResId = R.string.register,
                iconVector = Icons.Default.Check,
                onClick = {
                    coroutineScope.launch {
                        isLoadingScreen = true
                        val result = fireStoreManager.registerUserData(
                            email = email,
                            name = name.trim(),
                            surname = surname.trim(),
                            imageUri = imageUri,
                            academicProgram = if (selectedRole == UserRole.STUDENT) academicProgram.trim() else null,
                            studentId = if (selectedRole == UserRole.STUDENT) studentId.trim() else null,
                            phoneNumber = phoneNumber.trim()
                        )

                        if (result.isSuccess) {
                            onRegisterDataSuccess()
                        } else {
                            Toast.makeText(
                                context,
                                result.exceptionOrNull()?.message
                                    ?: context.getString(R.string.error_uploading_data),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        isLoadingScreen = false
                    }
                },
                enabled = isFormEnabled
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}