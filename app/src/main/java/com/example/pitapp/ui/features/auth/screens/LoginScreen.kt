package com.example.pitapp.ui.features.auth.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.ui.features.auth.components.AuthActionButton
import com.example.pitapp.ui.features.auth.components.AuthHeader
import com.example.pitapp.ui.features.auth.components.AuthScreenBaseLayout
import com.example.pitapp.ui.features.auth.components.EmailTextField
import com.example.pitapp.ui.features.auth.components.ErrorMessageText
import com.example.pitapp.ui.features.auth.components.PasswordTextField
import com.example.pitapp.ui.features.auth.helpers.isValidEmail
import com.example.pitapp.ui.features.auth.helpers.isValidPassword
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen(
    authManager: AuthManager,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onResetPasswordClick: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoadingScreen by remember { mutableStateOf(false) }

    Scaffold {
        AuthScreenBaseLayout(isLoading = isLoadingScreen) {
            AuthHeader()
            Spacer(modifier = Modifier.height(16.dp))

            EmailTextField(
                email = email,
                onEmailChange = {
                    email = it.trim()
                    errorMessage = null
                },
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(16.dp))

            PasswordTextField(
                password = password,
                onPasswordChange = {
                    password = it.trim()
                    errorMessage = null
                },
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                imeAction = ImeAction.Done
            )
            Spacer(modifier = Modifier.height(16.dp))

            ErrorMessageText(errorMessage = errorMessage)

            AuthActionButton(
                textResId = R.string.login,
                iconVector = Icons.AutoMirrored.Filled.Login,
                onClick = {
                    if (!isValidEmail(email)) {
                        errorMessage = R.string.regex_email.toString()
                        return@AuthActionButton
                    }
                    if (!isValidPassword(password)) {
                        errorMessage = R.string.regex_password.toString()
                        return@AuthActionButton
                    }
                    coroutineScope.launch {
                        isLoadingScreen = true
                        val result = authManager.loginWithEmail(email, password)
                        delay(1000)
                        if (result.isSuccess) {
                            errorMessage = null
                            onLoginSuccess()
                        } else {
                            errorMessage = R.string.login_failed.toString()
                        }
                        isLoadingScreen = false
                    }
                },
                enabled = email.isNotEmpty() && password.isNotEmpty()
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onRegisterClick) {
                Text(text = stringResource(id = R.string.register_here))
            }
            TextButton(onClick = onResetPasswordClick) {
                Text(text = stringResource(id = R.string.forgot_password))
            }
        }
    }
}
