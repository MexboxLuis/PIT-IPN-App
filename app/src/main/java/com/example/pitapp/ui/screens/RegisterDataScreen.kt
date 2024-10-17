package com.example.pitapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterDataScreen(
    navController: NavHostController,
    authManager: AuthManager,
    onRegisterSuccess: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoadingScreen by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()


    fun isValidEmail(email: String): Boolean {
        val ipnEmailPattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]?ipn\\.mx$")
        return ipnEmailPattern.matches(email)
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    if (!isLoadingScreen) {
        BackScaffold(navController = navController, authManager = authManager, topBarTitle = null) {


            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {

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
                            email = it
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
                            password = it
                            errorMessage = null
                        },
                        label = { Text(text = stringResource(id = R.string.password)) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
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
                            imeAction = ImeAction.Next
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text(text = stringResource(id = R.string.confirm_password)) },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                confirmPasswordVisible = !confirmPasswordVisible
                            }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
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
                            color = MaterialTheme.colorScheme.error
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

                            if (password != confirmPassword) {
                                errorMessage = R.string.regex_confirm_password.toString()
                                return@OutlinedButton
                            }

                            coroutineScope.launch {
                                isLoadingScreen = true
                                val authResult = authManager.registerWithEmail(email, password)
                                if (authResult.isSuccess) {
                                    onRegisterSuccess()
                                } else {
                                    errorMessage = R.string.email_already_registered.toString()
                                }
                                delay(1000)
                                isLoadingScreen = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        enabled = email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
                    ) {
                        Text(text = stringResource(id = R.string.register))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    } else {
        LoadingScreen()
    }
}
