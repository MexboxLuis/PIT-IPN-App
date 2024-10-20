package com.example.pitapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.ui.components.MainScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavHostController,
    authManager: AuthManager,
    firestoreManager: FireStoreManager
) {
    if (authManager.isUserLoggedIn()) {
        var permissionLevel by rememberSaveable { mutableIntStateOf(-1) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                val result = firestoreManager.getUserData()

                if (result.isSuccess) {
                    result.getOrNull()?.let { userData ->
                        permissionLevel = userData.permission
                        println("Permission obtained: $permissionLevel")
                    }
                } else {
                    println("Failed to retrieve user data: ${result.exceptionOrNull()?.message}")
                    authManager.getUserEmail()?.let { email ->
                        navController.navigate("registerAllDataScreen/$email")
                    }
                }
            }
        }

        if (permissionLevel == -1) {
            LoadingScreen()
        } else {
            when (permissionLevel) {
                1, 2 -> {
                    MainScaffold(
                        navController = navController,
                        authManager = authManager,
                        firestoreManager = firestoreManager
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            val clases = listOf(
                                Clase(
                                    "Carlos Sánchez",
                                    "Matemáticas",
                                    "10/10/2024",
                                    "10:00 AM",
                                    "Salón 101",
                                    listOf("Alumno1", "Alumno2", "Alumno3")
                                ),
                                Clase(
                                    "María López",
                                    "Física",
                                    "11/10/2024",
                                    "12:00 PM",
                                    "Salón 203",
                                    listOf("Alumno4", "Alumno5", "Alumno6")
                                ),
                                Clase(
                                    "Jorge Pérez",
                                    "Química",
                                    "12/10/2024",
                                    "9:00 AM",
                                    "Salón 102",
                                    listOf("Alumno7", "Alumno8", "Alumno9")
                                ),
                                Clase(
                                    "Mamarre Zamora",
                                    "Programación",
                                    "12/10/2024",
                                    "9:00 AM",
                                    "Salón 102",
                                    listOf("Alumno10", "Alumno11", "Alumno12")
                                )
                            )

                            LazyColumn {
                                item {
                                    Text(
                                        text = "Mis Clases:",
                                        style = MaterialTheme.typography.headlineSmall,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(clases) { clase ->
                                    ClaseCard(
                                        clase,
                                        onClick = {
                                            navController.navigate(
                                                "classDetailScreen/${clase.tutoria}/${clase.tutor}/${clase.hora}"
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }


                0 -> {
                    Scaffold { padding ->
                        Box(modifier = Modifier.padding(padding))
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Approval,
                                contentDescription = "Esperando aprobación",
                                modifier = Modifier.size(100.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Tu solicitud está pendiente de aprobación.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Por favor, espera a que algun administrador acepte tu solicitud.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            OutlinedButton(
                                onClick = {
                                    authManager.logout()
                                    navController.navigate("loginScreen")
                                }
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = "Cambiar cuenta"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Iniciar con otra cuenta")
                            }
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Advertencia",
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error de conexión o de permisos.",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Verifica tu conexión a internet o contacta al administrador.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
    else{
        navController.navigate("loginScreen")
    }
}


@Composable
fun ClaseCard(clase: Clase, onClick: () -> Unit = {}) {
    Column(modifier = Modifier
        .fillMaxSize()
        .clickable { onClick() }) {


        HorizontalDivider()
        Column(modifier = Modifier.padding(16.dp)) {

            Text(text = clase.tutoria, style = MaterialTheme.typography.titleLarge)
            Text(text = "Fecha: ${clase.fecha}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Hora: ${clase.hora}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Lugar: ${clase.lugar}", style = MaterialTheme.typography.bodySmall)
            Text(
                text = "No. de Alumnos: ${clase.alumnos.size}",
                style = MaterialTheme.typography.bodySmall
            )

        }
        HorizontalDivider()
    }

}

data class Clase(
    val tutor: String,
    val tutoria: String,
    val fecha: String,
    val hora: String,
    val lugar: String,
    val alumnos: List<String>
)

