package com.example.pitapp.ui.features.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.pitapp.datasource.FireStoreManager

@Composable
fun TutorScaffold(
    navController: NavHostController,
    fireStoreManager: FireStoreManager,
    onFabClick: (() -> Unit)? = null,
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
        },
        floatingActionButton = {
            if (onFabClick != null)
                ExtendedFloatingActionButton(
                    text = { Text(text = "Resumir Clases") },
                    icon = { Icon(Icons.Filled.AutoGraph, contentDescription = null) },
                    onClick = onFabClick
                )
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }

}