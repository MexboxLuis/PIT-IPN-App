package com.example.pitapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.example.pitapp.core.devicepolicy.AppGuard
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.datasource.FireStoreManager
import com.example.pitapp.ui.navigation.PITNavigation
import com.example.pitapp.ui.theme.PITAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PITAppTheme {
                AppGuard{
                    PITApp()
                }
            }
        }
    }
}

@Composable
fun PITApp() {
    val navController = rememberNavController()
    val authManager = AuthManager(FirebaseAuth.getInstance())
    val fireStoreManager = remember {
        FireStoreManager(
            authManager = authManager,
            firestore = FirebaseFirestore.getInstance(),
            storage = FirebaseStorage.getInstance()
        )
    }

    PITNavigation(
        navController = navController,
        authManager = authManager,
        fireStoreManager = fireStoreManager
    )
}






