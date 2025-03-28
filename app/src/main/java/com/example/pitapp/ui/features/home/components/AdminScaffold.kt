package com.example.pitapp.ui.features.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.pitapp.datasource.FireStoreManager


@Composable
fun AdminScaffold(
    navController: NavHostController,
    fireStoreManager: FireStoreManager,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            MainTopAppBar(
                fireStoreManager = fireStoreManager,
                onProfileClick = {
                    navController.navigate("profileScreen")
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}