package com.example.pitapp.utils

import android.net.Uri
import com.example.pitapp.data.UserData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.Date
import java.util.UUID

class FireStoreManager(
    private val authManager: AuthManager,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    suspend fun registerUserData(
        email: String,
        name: String,
        surname: String,
        imageUri: Uri?
    ): Result<Boolean> {
        return try {
            // Subir la imagen si no es nula
            val profilePictureUrl: String? = imageUri?.let { uri ->
                // Crear una referencia única para la imagen
                val storageRef = storage.reference.child("$email/images/${UUID.randomUUID()}.jpg")

                // Subir la imagen
                storageRef.putFile(uri).await()

                // Obtener la URL de descarga
                storageRef.downloadUrl.await().toString()
            }

            // Preparar los datos del usuario
            val data = mutableMapOf(
                "email" to email,
                "name" to name,
                "surname" to surname,
                "permission" to 0,
            )

            // Añadir la URL de la imagen si existe
            profilePictureUrl?.let {
                data["profilePictureUrl"] = it
            }

            // Guardar los datos en Firestore
            firestore.collection("saved_users").add(data).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save data: ${e.localizedMessage}"))
        }
    }


    suspend fun getUserDataByEmail(email: String): Result<UserData> {
        return try {
            val querySnapshot = firestore.collection("saved_users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents.first()
                val userData = document.toObject(UserData::class.java)
                if (userData != null) {
                    Result.success(userData)
                } else {
                    Result.failure(Exception("No se pudo convertir los datos del usuario."))
                }
            } else {
                Result.failure(Exception("No se encontraron datos para este usuario."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener los datos del usuario: ${e.localizedMessage}"))
        }
    }

    fun getAllUsersSnapshot(onResult: (Result<List<UserData>>) -> Unit) {
        firestore.collection("saved_users")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    onResult(Result.failure(exception))
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val users = snapshot.documents.mapNotNull { document ->
                        document.toObject(UserData::class.java)
                    }
                    onResult(Result.success(users))
                } else {
                    onResult(Result.success(emptyList()))
                }
            }
    }


    suspend fun updateUserPermission(email: String, newPermission: Int): Result<Unit> {
        return try {
            val querySnapshot = firestore.collection("saved_users")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0]
                document.reference.update("permission", newPermission).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No se encontró ningún usuario con el email: $email"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar el permiso: ${e.localizedMessage}"))
        }
    }





//
//    suspend fun updateDocumentText(documentId: String, newText: String): Result<Boolean> {
//        return try {
//            firestore.collection("saved_texts")
//                .document(documentId)
//                .update("text", newText)
//                .await()
//
//            Result.success(true)
//        } catch (e: Exception) {
//            Result.failure(Exception("Failed to update document: ${e.localizedMessage}"))
//        }
//    }
//
//    suspend fun deleteDocument(documentId: String, imageUrl: String): Result<Boolean> {
//        return try {
//            firestore.collection("saved_texts")
//                .document(documentId)
//                .delete()
//                .await()
//
//            val storageRef = storage.getReferenceFromUrl(imageUrl)
//            storageRef.delete().await()
//            Result.success(true)
//
//        } catch (e: Exception) {
//            Result.failure(Exception("Failed to delete document and image: ${e.localizedMessage}"))
//        }
//    }
}
