package com.example.pitapp.ui.features.careers.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.ui.features.careers.components.CareerGridItem
import com.example.pitapp.ui.features.careers.components.CareerItem
import com.example.pitapp.ui.shared.components.BackScaffold
import java.net.URLEncoder


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareersScreen(navController: NavHostController, authManager: AuthManager) {

    val context = LocalContext.current
    val careers = listOf(
        CareerItem(
            nameResId = R.string.career_biotech,
            url = "https://www.upiit.ipn.mx/oferta-educativa/ver-carrera.html?lg=es&id=35&nombre=Ingenier%C3%ADa-Biotecnol%C3%B3gica",
            imageUrl = "https://upiit.ipn.mx/assets/files/upiit/img/inicio/icn-biotecnologia.jpg"
        ),
        CareerItem(
            nameResId = R.string.career_ai,
            url = "https://www.upiit.ipn.mx/oferta-educativa/ver-carrera.html?lg=es&id=68&nombre=Ingenier%C3%ADa-en-Inteligencia-Artificial",
            imageUrl = "https://upiit.ipn.mx/assets/files/upiit/img/inicio/icn-ia.jpg"
        ),
        CareerItem(
            nameResId = R.string.career_automotive,
            url = "https://www.upiit.ipn.mx/oferta-educativa/ver-carrera.html?lg=es&id=23&nombre=Ingenier%C3%ADa-en-Sistemas-Automotrices",
            imageUrl = "https://upiit.ipn.mx/assets/files/upiit/img/inicio/icn-automotriz.jpg"
        ),
        CareerItem(
            nameResId = R.string.career_transport,
            url = "https://www.upiit.ipn.mx/oferta-educativa/ver-carrera.html?lg=es&id=28&nombre=Ingenier%C3%ADa-en-Transporte",
            imageUrl = "https://upiit.ipn.mx/assets/files/upiit/img/inicio/icn-transporte.jpg"
        ),
        CareerItem(
            nameResId = R.string.career_data_science,
            url = "https://www.upiit.ipn.mx/oferta-educativa/ver-carrera.html?lg=es&id=69&nombre=Licenciatura-en-Ciencia-de-Datos",
            imageUrl = "https://upiit.ipn.mx/assets/files/upiit/img/inicio/icn-datos.jpg"
        ),
        CareerItem(
            nameResId = R.string.career_industrial,
            url = "https://www.ipn.mx/oferta-educativa/educacion-superior/ver-carrera.html?lg=es&id=13&nombre=Ingenier%C3%ADa-Industrial",
            imageUrl = "https://i.imgur.com/bjvR1fm.png"
        )
    )

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(id = R.string.careers_title)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = careers, key = { it.nameResId }) { careerItem ->
                CareerGridItem(context = context, career = careerItem) {
                    if (careerItem.url.isNotEmpty()) {
                        try {
                            val encodedUrl = URLEncoder.encode(careerItem.url, "UTF-8")
                            navController.navigate("careerWebView/$encodedUrl")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}