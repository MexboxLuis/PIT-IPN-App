package com.example.pitapp.utils

import android.net.Uri
import com.example.pitapp.data.ClassData
import com.example.pitapp.data.Student
import com.example.pitapp.data.UserData
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
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
        newImageUri: Uri?
    ): Result<Unit> {
        val email = authManager.getUserEmail()
            ?: return Result.failure(Exception("User is not logged in or email not available."))

        val storageRef = storage.reference

        suspend fun updateFireStore(profilePictureUrl: String?) {
            val userUpdates = if (profilePictureUrl != null) {
                mapOf(
                    "name" to name,
                    "surname" to surname,
                    "profilePictureUrl" to profilePictureUrl
                )
            } else {
                mapOf(
                    "name" to name,
                    "surname" to surname,
                    "profilePictureUrl" to null
                )
            }

            try {
                firestore.collection("saved_users")
                    .document(email)
                    .update(userUpdates)
                    .await()
            } catch (e: Exception) {
                throw Exception(e.localizedMessage)
            }
        }

        suspend fun deleteOldImageIfNeeded(oldUrl: String?) {
            oldUrl?.let {
                val oldImageRef = storage.getReferenceFromUrl(it)
                try {
                    oldImageRef.delete().await()
                } catch (e: StorageException) {
                    if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                        throw e
                    } else {

                    }
                }
            }
        }

        return try {
            val currentUserData = firestore.collection("saved_users").document(email).get().await()
            val oldProfilePictureUrl = currentUserData.getString("profilePictureUrl")

            val newProfilePictureUrl: String? = when {
                newImageUri != null -> {
                    deleteOldImageIfNeeded(oldProfilePictureUrl)
                    val newImageRef = storageRef.child("$email/images/${UUID.randomUUID()}.jpg")
                    newImageRef.putFile(newImageUri).await()
                    newImageRef.downloadUrl.await().toString()
                }
                else -> null
            }

            updateFireStore(newProfilePictureUrl)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update user data: ${e.localizedMessage}"))
        }
    }

    suspend fun deleteImageFromStorage(imageUrl: String?) {
        imageUrl?.let {
            try {
                val imageRef = storage.getReferenceFromUrl(it)
                imageRef.delete().await()
            } catch (e: StorageException) {
                if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                    throw e
                } else {

                }
            }
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
            )

            firestore.collection("saved_classes")
                .add(classData)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create class: ${e.localizedMessage}"))
        }
    }

    fun getClasses(email: String, onResult: (Result<List<Pair<String, ClassData>>>) -> Unit) {
        if (email.isEmpty()) {
            onResult(Result.failure(Exception("User email is required.")))
            return
        }

        firestore.collection("saved_classes")
            .whereEqualTo("email", email)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onResult(Result.failure(Exception("Error fetching class data: ${error.localizedMessage}")))
                    return@addSnapshotListener
                }

                val classList = snapshot?.documents?.mapNotNull { document ->
                    val tutoring = document.getString("tutoring") ?: ""
                    val topic = document.getString("topic") ?: ""
                    val classroom = document.getString("classroom") ?: ""
                    val startTime = document.getTimestamp("startTime") ?: Timestamp.now()
                    val expectedDuration = document.getLong("expectedDuration")
                    val realDuration = document.getLong("realDuration")

                    document.id to ClassData(
                        email = email,
                        tutoring = tutoring,
                        topic = topic,
                        classroom = classroom,
                        startTime = startTime,
                        expectedDuration = expectedDuration,
                        realDuration = realDuration,

                        )
                } ?: emptyList()

                onResult(Result.success(classList))
            }
    }

    fun getClassDetails(documentId: String, onResult: (Result<ClassData?>) -> Unit) {
        firestore.collection("saved_classes")
            .document(documentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onResult(Result.failure(Exception("Error fetching class details: ${error.localizedMessage}")))
                    return@addSnapshotListener
                }

                val classData = snapshot?.toObject(ClassData::class.java)
                onResult(Result.success(classData))
            }
    }


    fun finishClass(documentId: String, startTime: Timestamp, onResult: (Result<Unit>) -> Unit) {
        val now = Timestamp.now()
        val realDuration = (now.seconds - startTime.seconds) / 60

        firestore.collection("saved_classes")
            .document(documentId)
            .update("realDuration", realDuration)
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                onResult(Result.failure(Exception("Error at the end of the class: ${e.localizedMessage}")))
            }
    }


//    mi idea en el futuro es que en la clase se mande si es regular o irregular con un booleano y tambien que
//    se guarde la signature en null y en la clase el bitmap jiji
    fun addStudentToClass(
        classDocumentId: String,
        student: Student,
        callback: (Result<Unit>) -> Unit
    ) {
        val studentsCollection = FirebaseFirestore.getInstance().collection("saved_classes")
            .document(classDocumentId)
            .collection("students")

        val savedStudentsCollection = FirebaseFirestore.getInstance().collection("saved_students")

        savedStudentsCollection.document(student.studentId)
            .set(student)
            .addOnSuccessListener {
                studentsCollection.document(student.studentId)
                    .set(mapOf("studentId" to student.studentId))
                    .addOnSuccessListener {
                        callback(Result.success(Unit))
                    }
                    .addOnFailureListener { exception ->
                        callback(Result.failure(exception))
                    }
            }
            .addOnFailureListener { exception ->
                callback(Result.failure(exception))
            }
    }

    fun getStudents(classDocumentId: String, callback: (Result<List<Student>>) -> Unit) {
        val studentsCollection = FirebaseFirestore.getInstance().collection("saved_classes")
            .document(classDocumentId)
            .collection("students")

        studentsCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val studentIds = querySnapshot.documents.mapNotNull { it.getString("studentId") }
                if (studentIds.isEmpty()) {
                    callback(Result.success(emptyList()))
                    return@addOnSuccessListener
                }

                val savedStudentsCollection =
                    FirebaseFirestore.getInstance().collection("saved_students")
                val studentDetailsTasks = studentIds.map { studentId ->
                    savedStudentsCollection.document(studentId).get()
                }

                Tasks.whenAllComplete(studentDetailsTasks)
                    .addOnSuccessListener { tasks ->
                        val students = tasks.mapNotNull { task ->
                            val result = (task.result as? DocumentSnapshot)
                            result?.toObject(Student::class.java)
                        }
                        callback(Result.success(students))
                    }
                    .addOnFailureListener { exception ->
                        callback(Result.failure(exception))
                    }

                    .addOnFailureListener { exception ->
                        callback(Result.failure(exception))
                    }
            }
    }


}

