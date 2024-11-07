package com.example.pitapp.utils

import android.net.Uri
import com.example.pitapp.data.ClassData
import com.example.pitapp.data.UserData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    fun getUserData(onDataChanged: (Result<UserData?>) -> Unit) {
        val email = authManager.getUserEmail()

        if (email == null) {
            onDataChanged(Result.failure(Exception("User is not logged in or email not available.")))
            return
        }

        val documentReference = firestore.collection("saved_users").document(email)

        documentReference.addSnapshotListener { documentSnapshot, error ->
            if (error != null) {
                onDataChanged(Result.failure(Exception("Error retrieving user data: ${error.localizedMessage}")))
                return@addSnapshotListener
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                val userData = UserData(
                    email = documentSnapshot.getString("email") ?: "",
                    name = documentSnapshot.getString("name") ?: "",
                    surname = documentSnapshot.getString("surname") ?: "",
                    profilePictureUrl = documentSnapshot.getString("profilePictureUrl"),
                    permission = documentSnapshot.getLong("permission")?.toInt() ?: 0
                )
                onDataChanged(Result.success(userData))
            } else {
                onDataChanged(Result.failure(Exception("No user data found for this email.")))
            }
        }
    }

    suspend fun updateUserData(
        name: String,
        surname: String,
        newImageUri: Uri?,
        oldProfilePictureUrl: String?,
        onResult: (Result<Unit>) -> Unit
    ) {
        val email = authManager.getUserEmail()
            ?: return onResult(Result.failure(Exception("User is not logged in or email not available.")))

        val storageRef = storage.reference

        fun updateFireStore(profilePictureUrl: String?) {
            val userUpdates = mapOf(
                "name" to name,
                "surname" to surname,
                "profilePictureUrl" to profilePictureUrl
            )

            firestore.collection("saved_users")
                .document(email)
                .update(userUpdates)
                .addOnSuccessListener { onResult(Result.success(Unit)) }
                .addOnFailureListener { e -> onResult(Result.failure(e)) }
        }

        suspend fun handleImageUpload() {
            try {
                val newProfilePictureUrl: String? = newImageUri?.let { uri ->
                    oldProfilePictureUrl?.let {
                        val oldImageRef = storage.getReferenceFromUrl(it)
                        oldImageRef.delete().await()
                    }

                    val newImageRef = storageRef.child("$email/images/${UUID.randomUUID()}.jpg")
                    newImageRef.putFile(uri).await()
                    newImageRef.downloadUrl.await().toString()
                }

                if (newImageUri == null && oldProfilePictureUrl != null) {
                    val oldImageRef = storage.getReferenceFromUrl(oldProfilePictureUrl)
                    oldImageRef.delete().await()
                    updateFireStore(null)
                } else {
                    updateFireStore(newProfilePictureUrl)
                }

            } catch (e: Exception) {
                onResult(Result.failure(Exception("Failed to upload image: ${e.localizedMessage}")))
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            handleImageUpload()
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

    suspend fun createClass(
        tutoring: String,
        topic: String,
        classroom: String,
        durationHours: Int,
        durationMinutes: Int,
        isFreeTime: Boolean,
        startTime: Timestamp? = null
    ): Result<Boolean> {
        return try {
            val expectedDuration = if (isFreeTime) null else (durationHours * 60 + durationMinutes)

            val classData = hashMapOf(
                "email" to (authManager.getUserEmail() ?: ""),
                "tutoring" to tutoring,
                "topic" to topic,
                "classroom" to classroom,
                "startTime" to (startTime ?: FieldValue.serverTimestamp()),
                "expectedDuration" to expectedDuration,
                "realDuration" to null,
                "students" to null
            )

            firestore.collection("saved_classes")
                .add(classData)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create class: ${e.localizedMessage}"))
        }
    }

    fun fetchClassesForTutor(onResult: (Result<List<ClassData>>) -> Unit) {
        firestore.collection("saved_classes")
            .whereEqualTo("email", authManager.getUserEmail() ?: "")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val fetchedClasses = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val tutoring = doc.getString("tutoring") ?: ""
                        val topic = doc.getString("topic") ?: ""
                        val classroom = doc.getString("classroom") ?: ""
                        val email = doc.getString("email") ?: ""
                        val students = doc.get("students") as? List<String> ?: listOf()
                        val startTime = doc.getTimestamp("startTime") ?: Timestamp.now()
                        val expectedDuration = doc.getLong("expectedDuration")
                        val realDuration = doc.getLong("realDuration")

                        ClassData(
                            id = id,
                            email = email,
                            tutoring = tutoring,
                            topic = topic,
                            classroom = classroom,
                            startTime = startTime,
                            students = students,
                            expectedDuration = expectedDuration,
                            realDuration = realDuration
                        )
                    }
                    onResult(Result.success(fetchedClasses))
                } else {
                    onResult(Result.success(emptyList()))
                }
            }
    }


}

