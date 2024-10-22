package com.example.pitapp.utils

import android.net.Uri
import com.example.pitapp.data.UserData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.type.Date
import kotlinx.coroutines.tasks.await
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
            val profilePictureUrl: String? = imageUri?.let { uri ->
                val storageRef = storage.reference.child("$email/images/${UUID.randomUUID()}.jpg")
                storageRef.putFile(uri).await()
                storageRef.downloadUrl.await().toString()
            }

            val data = mutableMapOf(
                "email" to email,
                "name" to name,
                "surname" to surname,
                "permission" to 0,
                "profilePictureUrl" to profilePictureUrl
            )

            firestore.collection("saved_users")
                .document(email)
                .set(data)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save data: ${e.localizedMessage}"))
        }
    }

    suspend fun getUserData(): Result<UserData?> {
        return try {
            val email = authManager.getUserEmail()
                ?: return Result.failure(Exception("User is not logged in or email not available."))

            val documentSnapshot = firestore.collection("saved_users")
                .document(email)
                .get()
                .await()

            if (documentSnapshot.exists()) {
                val userData = UserData(
                    email = documentSnapshot.getString("email") ?: "",
                    name = documentSnapshot.getString("name") ?: "",
                    surname = documentSnapshot.getString("surname") ?: "",
                    profilePictureUrl = documentSnapshot.getString("profilePictureUrl"),
                    permission = documentSnapshot.getLong("permission")?.toInt() ?: 0
                )
                Result.success(userData)
            } else {
                Result.failure(Exception("No user data found for this email."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error retrieving user data: ${e.localizedMessage}"))
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
            val documentRef = firestore.collection("saved_users").document(email)

            documentRef.update("permission", newPermission).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error updating the permit: ${e.localizedMessage}"))
        }
    }



}

