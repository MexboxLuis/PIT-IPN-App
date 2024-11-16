package com.example.pitapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.data.ClassData
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import com.example.pitapp.utils.currentRoute
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
                fireStoreManager = fireStoreManager, onProfileClick = {
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