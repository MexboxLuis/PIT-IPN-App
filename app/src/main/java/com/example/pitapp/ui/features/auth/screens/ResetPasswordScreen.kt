package com.example.pitapp.ui.features.auth.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.ui.features.auth.helpers.isValidEmail
import com.example.pitapp.ui.shared.components.BackScaffold
import kotlinx.coroutines.launch
import com.example.pitapp.ui.features.auth.components.*

@Composable
fun ResetPasswordScreen(
    navController: NavHostController,
    authManager: AuthManager,
    onPasswordResetSent: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = null
    ) {

        val scrollState = rememberScrollState()
        val imeNestedScrollConnection = rememberNestedScrollInteropConnection()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .nestedScroll(imeNestedScrollConnection)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthHeader()
            Spacer(modifier = Modifier.height(32.dp))

            EmailTextField(
                email = email,
                onEmailChange = {
                    email = it.trim()
                    errorMessage = null
                },
                imeAction = ImeAction.Done
            )
            Spacer(modifier = Modifier.height(16.dp))

            ErrorMessageText(errorMessage = errorMessage)

            AuthActionButton(
                textResId = R.string.send_password_reset,
                iconVector = Icons.AutoMirrored.Filled.Send,
                onClick = {
                    if (!isValidEmail(email)) {
                        errorMessage = R.string.regex_email.toString()
                        return@AuthActionButton
                    }
                    coroutineScope.launch {
                        val result = authManager.resetPassword(email)
                        if (result.isSuccess) {
                            errorMessage = null
                            onPasswordResetSent()
                            Toast.makeText(
                                context,
                                R.string.toast_reset_password_sent,
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: context.getString(R.string.toast_reset_password_failed)
                            Toast.makeText(
                                context,
                                errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                enabled = email.isNotEmpty()
            )
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}