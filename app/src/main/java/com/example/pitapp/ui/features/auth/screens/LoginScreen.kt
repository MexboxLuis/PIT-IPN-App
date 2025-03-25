package com.example.pitapp.ui.features.auth.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import com.example.pitapp.ui.screens.LoadingScreen
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.isValidEmail
import com.example.pitapp.utils.isValidPassword
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
    val scrollState = rememberScrollState()
    val imeNestedScrollConnection = rememberNestedScrollInteropConnection()

    Scaffold {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!isLoadingScreen) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp)
                        .nestedScroll(imeNestedScrollConnection)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pit_logo),
                        contentDescription = null,
                        modifier = Modifier.size(84.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        " ",
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it.trimEnd()
                            errorMessage = null
                        },
                        label = { Text(text = stringResource(id = R.string.email)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it.trimEnd()
                            errorMessage = null
                        },
                        label = { Text(text = stringResource(id = R.string.password)) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    errorMessage?.let {
                        Text(
                            text = stringResource(id = it.toInt()),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedButton(
                        onClick = {


                            if (!isValidEmail(email)) {
                                errorMessage = R.string.regex_email.toString()
                                return@OutlinedButton
                            }

                            if (!isValidPassword(password)) {
                                errorMessage = R.string.regex_password.toString()
                                return@OutlinedButton
                            }

                            coroutineScope.launch {

                                val result = authManager.loginWithEmail(email, password)
                                isLoadingScreen = true
                                if (result.isSuccess) {
                                    errorMessage = null
                                    onLoginSuccess()
                                    delay(1000)
                                    isLoadingScreen = false
                                } else {
                                    errorMessage = R.string.login_failed.toString()
                                    delay(1000)
                                    isLoadingScreen = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(horizontal = 16.dp),
                        enabled = email.isNotEmpty() && password.isNotEmpty()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = stringResource(id = R.string.login))
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = null
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onRegisterClick) {
                        Text(text = stringResource(id = R.string.register_here))
                    }
                    TextButton(onClick = onResetPasswordClick) {
                        Text(text = stringResource(id = R.string.forgot_password))
                    }
                }
            } else {
                LoadingScreen()
            }
        }
    }
}
