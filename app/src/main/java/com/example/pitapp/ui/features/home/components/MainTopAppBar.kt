package com.example.pitapp.ui.features.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pitapp.R
import com.example.pitapp.model.UserData
import com.example.pitapp.datasource.FireStoreManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    fireStoreManager: FireStoreManager,
    onProfileClick: () -> Unit
) {

    var userData by remember { mutableStateOf<UserData?>(null) }
    LaunchedEffect(Unit) {
        fireStoreManager.getUserData { result ->
            userData = if (result.isSuccess) {
                result.getOrNull()
            } else {
                null
            }
        }
    }

    TopAppBar(
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.pit_logo),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 24.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { }
            )
        },
        title = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = stringResource(id = R.string.app_name),
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .fillMaxWidth(0.8f),
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center
                )

            }

        },
        actions = {
            if (userData?.profilePictureUrl != null) {
                AsyncImage(
                    model = userData?.profilePictureUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(42.dp)
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.onSurface,
                            RoundedCornerShape(32.dp)
                        )
                        .clip(CircleShape)
                        .clickable {
                            onProfileClick()
                        }
                )
            } else {
                IconButton(
                    onClick = {
                        onProfileClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    )
                }
            }

        }
    )
}