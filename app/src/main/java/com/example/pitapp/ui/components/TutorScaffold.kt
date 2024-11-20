package com.example.pitapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager

@Composable
fun TutorScaffold(
    navController: NavHostController,
    authManager: AuthManager,
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