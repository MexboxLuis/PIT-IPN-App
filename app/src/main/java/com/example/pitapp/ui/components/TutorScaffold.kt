package com.example.pitapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
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

    val actualRoute = currentRoute(navController)
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()



    Scaffold(
        topBar = {
            MainTopAppBar(
                fireStoreManager = fireStoreManager, onProfileClick = {
                    navController.navigate("profileScreen")
                }
            )
        },

        bottomBar = {
//            BottomAppBar {
//                NavigationBar {
//                    NavigationBarItem(
//                        selected = actualRoute == "homeScreen",
//                        onClick = {
//                            if (actualRoute != "homeScreen")
//                                navController.navigate("homeScreen")
//                        },
//                        icon = {
//                            Icon(
//                                imageVector = Icons.Default.Home,
//                                contentDescription = null
//                            )
//                        },
//                        label = { Text(text = stringResource(id = R.string.home)) }
//                    )
//                }
//
//            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        sheetState.show()
                    }
                },
                shape = CircleShape,
                modifier = Modifier.size(if (sheetState.isVisible) 64.dp else 52.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }

    CreateClassSheet(
        sheetState = sheetState,
        scope = scope,
        onStartNowClick = { navController.navigate("startClassNowScreen") },
        onScheduleClick = { navController.navigate("scheduleClassScreen") }
    )
}