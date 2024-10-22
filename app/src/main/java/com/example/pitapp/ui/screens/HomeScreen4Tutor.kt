package com.example.pitapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.ui.components.MainScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager

@Composable
fun HomeScreen4Tutor(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager,
) {

    MainScaffold(
        navController = navController,
        authManager = authManager,
        fireStoreManager = fireStoreManager
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
                    listOf("Alumno4", "Alumno6")
                ),
                Clase(
                    "Jorge Pérez",
                    "Química",
                    "12/10/2024",
                    "9:00 AM",
                    "Salón 102",
                    listOf(
                        "Alumno7",
                        "Alumno8",
                        "Alumno9",
                        "Alumno10",
                        "Alumno11",
                        "Alumno12",
                        "Alumno10",
                        "Alumno11",
                        "Alumno12"
                    )
                ),
                Clase(
                    "Mamarre Zamora",
                    "Programación",
                    "12/10/2024",
                    "9:00 AM",
                    "Salón 102",
                    listOf("Alumno10", "Alumno11", "Alumno12", "Alumno10", "Alumno11", "Alumno12")
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
