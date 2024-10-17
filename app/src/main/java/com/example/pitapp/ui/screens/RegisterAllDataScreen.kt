package com.example.pitapp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.pitapp.R
import com.example.pitapp.ui.components.BackScaffold
import com.example.pitapp.utils.AuthManager
import com.example.pitapp.utils.FireStoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



@Composable
fun RegisterAllDataScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager,
    email: String,
    password: String,
    onRegisterSuccess: () -> Unit
) {
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri.value = uri
        }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val name = remember { mutableStateOf(TextFieldValue("")) }
    val surname = remember { mutableStateOf(TextFieldValue("")) }
    var isLoadingScreen by rememberSaveable { mutableStateOf(false) }

    if (!isLoadingScreen) {
        BackScaffold(navController = navController, authManager = authManager, topBarTitle = null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
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

                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(Color.Gray, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri.value != null) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUri.value),
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Placeholder",
                                modifier = Modifier.size(100.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { launcher.launch("image/*") }) {
                        Text("Upload Image")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name.value,
                        onValueChange = { name.value = it },
                        label = { Text("Name(s)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = surname.value,
                        onValueChange = { surname.value = it },
                        label = { Text("Surname(s)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            isLoadingScreen = true
                            coroutineScope.launch {
                                val selectedImageUri = imageUri.value
                                    val dataResult = fireStoreManager.registerUserData(
                                        email,
                                        name.value.text,
                                        surname.value.text,
                                        selectedImageUri
                                    )
                                    if (dataResult.isSuccess) {
                                        onRegisterSuccess()
                                        authManager.loginWithEmail(email, password)
                                    } else {
                                        authManager.deleteUser()
                                        Toast.makeText(
                                            context,
                                            "Error al registrar datos: ${dataResult.exceptionOrNull()?.localizedMessage}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                delay(1000)
                                isLoadingScreen = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        enabled = name.value.text.isNotEmpty() && surname.value.text.isNotEmpty()
                    ) {
                        Text("Register")
                    }
                }
            }
        }
    } else {
        LoadingScreen()
    }
}

