package com.example.pitapp.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.pitapp.ui.components.MainScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager

@Composable
fun HomeScreen4Admin(
    navController: NavHostController,
    authManager: AuthManager,
    firestoreManager: FireStoreManager,
) {
    MainScaffold(
        navController = navController,
        authManager = authManager,
        fireStoreManager = firestoreManager
    ) {
        // Contenido de la pantalla

        // ...

        // ...

        Text(text = "Pantalla de Admin")
    }

}