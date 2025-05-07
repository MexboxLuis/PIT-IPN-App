package com.example.pitapp.ui.features.auth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pitapp.R

@Composable
fun AuthHeader() {
    Image(
        painter = painterResource(id = R.drawable.pit_logo),
        contentDescription = null,
        modifier = Modifier.size(84.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        " ",
        style = MaterialTheme.typography.headlineLarge,
    )
}
