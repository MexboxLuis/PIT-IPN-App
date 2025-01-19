package com.example.pitapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager

@Composable
fun CareersScreen(navController: NavHostController, authManager: AuthManager) {
    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(id = R.string.careers_title)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Esta es la pantalla de Carreras",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
