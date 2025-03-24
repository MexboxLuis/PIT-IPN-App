package com.example.pitapp.utils

import android.net.Uri
import com.example.pitapp.data.ClassData
import com.example.pitapp.data.Student
import com.example.pitapp.data.UserData
import com.example.pitapp.ui.features.scheduling.model.Schedule
import com.example.pitapp.ui.screens.Classroom
import com.example.pitapp.ui.screens.NonWorkingDay
import com.example.pitapp.ui.screens.Period
import com.example.pitapp.ui.screens.SavedClass
import com.example.pitapp.ui.screens.SavedStudent
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
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

    fun getClasses(onResult: (Result<List<Pair<String, ClassData>>>) -> Unit) {
        val email = authManager.getUserEmail()
        if (email.isNullOrEmpty()) {
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

    fun getClassesByEmail(email: String, onResult: (Result<List<Pair<String, ClassData>>>) -> Unit) {
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
        val studentsCollection = firestore.collection("saved_classes")
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



    suspend fun addNonWorkingDay(date: Timestamp) {
        val calendar = Calendar.getInstance().apply { time = date.toDate() }
        val year = calendar.get(Calendar.YEAR).toString()
        val nonWorkingDaysRef = firestore.collection("saved_calendar")
            .document(year)
            .collection("nonWorkingDays")
        val newDay = NonWorkingDay(date)
        nonWorkingDaysRef.add(newDay).await()
    }

    suspend fun addPeriod(year: String, startDate: Timestamp, endDate: Timestamp) {
        val periodsRef = firestore.collection("saved_calendar")
            .document(year)
            .collection("periods")
        val newPeriod = Period(startDate, endDate)
        periodsRef.add(newPeriod).await()
    }

    suspend fun getNonWorkingDays(year: String): List<NonWorkingDay> = withContext(Dispatchers.IO) {
        val nonWorkingDaysRef = firestore.collection("saved_calendar")
            .document(year)
            .collection("nonWorkingDays")
        try {
            val snapshot = nonWorkingDaysRef.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(NonWorkingDay::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPeriods(year: String): List<Period> = withContext(Dispatchers.IO) {
        val periodsRef = firestore.collection("saved_calendar")
            .document(year)
            .collection("periods")
        try {
            val snapshot = periodsRef.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Period::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteNonWorkingDay(date: Timestamp) = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance().apply { time = date.toDate() }
        val year = calendar.get(Calendar.YEAR).toString()
        val nonWorkingDaysRef = firestore.collection("saved_calendar")
            .document(year)
            .collection("nonWorkingDays")
        try {
            val querySnapshot = nonWorkingDaysRef
                .whereEqualTo("date", date)
                .get()
                .await()
            for (doc in querySnapshot.documents) {
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deletePeriod(period: Period) = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance().apply { time = period.startDate.toDate() }
        val year = calendar.get(Calendar.YEAR).toString()
        val periodsRef = firestore.collection("saved_calendar")
            .document(year)
            .collection("periods")
        try {
            val querySnapshot = periodsRef
                .whereEqualTo("startDate", period.startDate)
                .whereEqualTo("endDate", period.endDate)
                .get()
                .await()
            for (doc in querySnapshot.documents) {
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            throw e
        }
    }


    fun addClassroom(classroom: Classroom, callback: (Result<Unit>) -> Unit) {
        firestore.collection("saved_classrooms")
            .document(classroom.number.toString())
            .set(
                mapOf(
                    "number" to classroom.number,
                    "description" to classroom.description
                )
            )
            .addOnSuccessListener { callback(Result.success(Unit)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun updateClassroom(classroom: Classroom, callback: (Result<Unit>) -> Unit) {
        firestore.collection("saved_classrooms")
            .document(classroom.number.toString())
            .update("description", classroom.description)
            .addOnSuccessListener { callback(Result.success(Unit)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun deleteClassroom(number: Int, callback: (Result<Unit>) -> Unit) {
        firestore.collection("saved_classrooms")
            .document(number.toString())
            .delete()
            .addOnSuccessListener { callback(Result.success(Unit)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun getClassrooms(callback: (Result<List<Classroom>>) -> Unit) {
        firestore.collection("saved_classrooms")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    callback(Result.failure(error))
                    return@addSnapshotListener
                }
                val classrooms = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Classroom::class.java)
                } ?: emptyList()
                callback(Result.success(classrooms))
            }
    }

    fun createSchedule(
        schedule: Schedule,
        callback: (Result<Unit>) -> Unit
    ) {
        firestore.collection("saved_schedules")
            .add(schedule)
            .addOnSuccessListener { callback(Result.success(Unit)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun getSchedules(callback: (Result<List<Pair<String, Schedule>>>) -> Unit) {
        firestore.collection("saved_schedules")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    callback(Result.failure(error))
                    return@addSnapshotListener
                }
                val schedules = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Schedule::class.java)?.let { schedule ->
                        doc.id to schedule
                    }
                } ?: emptyList()
                callback(Result.success(schedules))
            }
    }

    fun approveSchedule(documentId: String, callback: (Result<Unit>) -> Unit) {
        firestore.collection("saved_schedules")
            .document(documentId)
            .update("approved", true)
            .addOnSuccessListener { callback(Result.success(Unit)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun updateSchedule(
        documentId: String,
        schedule: Schedule,
        callback: (Result<Unit>) -> Unit
    ) {
        firestore.collection("saved_schedules")
            .document(documentId)
            .set(schedule)
            .addOnSuccessListener { callback(Result.success(Unit)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun getScheduleById(scheduleId: String, callback: (Result<Schedule>) -> Unit) {
        firestore.collection("saved_schedules")
            .document(scheduleId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val schedule = document.toObject(Schedule::class.java)
                    if (schedule != null) {
                        callback(Result.success(schedule))
                    } else {
                        callback(Result.failure(Exception("No se pudo convertir el documento a Schedule"))) // o un error más específico
                    }
                } else {
                    callback(Result.failure(Exception("El horario no existe"))) // o un error de "No encontrado"
                }
            }
            .addOnFailureListener {
                callback(Result.failure(it))
            }
    }

    fun disapproveSchedule(documentId: String, callback: (Result<Unit>) -> Unit) {
        firestore.collection("saved_schedules")
            .document(documentId)
            .update("approved", false)
            .addOnSuccessListener { callback(Result.success(Unit)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun deleteSchedule(documentId: String, callback: (Result<Unit>) -> Unit) {
        firestore.collection("saved_schedules")
            .document(documentId)
            .delete()
            .addOnSuccessListener { callback(Result.success(Unit)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }


    fun getClassroomByNumber(number:String ,callback: (Result<Classroom>) -> Unit){
        firestore.collection("saved_classrooms")
            .whereEqualTo("number", number.toInt())
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val classroom = querySnapshot.documents[0].toObject(Classroom::class.java)
                    classroom?.let{ callback(Result.success(it))}
                }
                else{
                    callback(Result.failure(Exception("Classroom Not found")))
                }

            }
            .addOnFailureListener{
                callback(Result.failure(it))
            }
    }

    suspend fun checkForOverlap(newSchedule: Schedule): Boolean = withContext(Dispatchers.IO) {
        val overlappingSchedules = firestore.collection("saved_schedules")
            .whereEqualTo("salonId", newSchedule.salonId)
            .whereEqualTo("approved", true)
            .get()
            .await()
            .toObjects(Schedule::class.java)

        for (existingSchedule in overlappingSchedules) {
            if (schedulesOverlap(newSchedule, existingSchedule)) {
                return@withContext true
            }
        }
        return@withContext false
    }

    suspend fun checkForUpdatedOverlap(updatedSchedule: Schedule, scheduleId: String?): Boolean = withContext(Dispatchers.IO) {

        val overlappingSchedules = firestore.collection("saved_schedules")
            .whereEqualTo("salonId", updatedSchedule.salonId)
            .whereEqualTo("approved", true)
            .get()
            .await()
            .documents
            .filter { it.id != scheduleId }
            .mapNotNull { it.toObject(Schedule::class.java) }

        for (existingSchedule in overlappingSchedules) {
            if (schedulesOverlap(updatedSchedule, existingSchedule)) {
                return@withContext true
            }
        }
        return@withContext false
    }

    suspend fun checkForEmailOverlap(newSchedule: Schedule, scheduleId: String? = null): Boolean = withContext(Dispatchers.IO) {

        val querySnapshot = firestore.collection("saved_schedules")
            .whereEqualTo("tutorEmail", newSchedule.tutorEmail)
            .whereEqualTo("approved", true)
            .get()
            .await()

        val overlappingSchedules = querySnapshot.documents
            .filter { it.id != scheduleId }
            .mapNotNull { it.toObject(Schedule::class.java) }

        for (existingSchedule in overlappingSchedules) {
            if (schedulesOverlap(newSchedule, existingSchedule)) {
                return@withContext true
            }
        }
        return@withContext false
    }

    private fun schedulesOverlap(schedule1: Schedule, schedule2: Schedule): Boolean {
        if (schedule1.startYear > schedule2.endYear || (schedule1.startYear == schedule2.endYear && schedule1.startMonth > schedule2.endMonth)) {
            return false
        }
        if (schedule2.startYear > schedule1.endYear || (schedule2.startYear == schedule1.endYear && schedule2.startMonth > schedule1.endMonth)) {
            return false
        }

        for (session1 in schedule1.sessions) {
            for (session2 in schedule2.sessions) {
                if (session1.dayOfWeek == session2.dayOfWeek && session1.startTime == session2.startTime) {
                    return true
                }
            }
        }
        return false
    }

    fun startInstantClass(savedClass: SavedClass, callback: (Result<String>) -> Unit) {
        val classId = UUID.randomUUID().toString()
        val documentId = "${savedClass.tutorEmail}-$classId"

        firestore.collection("saved_instant_classes")
            .document(documentId)
            .set(savedClass)
            .addOnSuccessListener {
                callback(Result.success(documentId))
            }
            .addOnFailureListener {
                callback(Result.failure(it))
            }
    }

    fun addStudent(
        classDocumentId: String,
        student: SavedStudent,
        callback: (Result<Unit>) -> Unit
    ) {
        val classRef = firestore.collection("saved_instant_classes")
            .document(classDocumentId)

        classRef.collection("students").add(student)
            .addOnSuccessListener {
                callback(Result.success(Unit))
            }
            .addOnFailureListener {
                callback(Result.failure(it))
            }
    }




    fun getCurrentSchedules(tutorEmail: String, callback: (Result<List<Schedule>>) -> Unit) {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH) + 1

        firestore.collection("saved_schedules")
            .whereEqualTo("tutorEmail", tutorEmail)
            .whereEqualTo("approved", true)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    callback(Result.failure(error))
                    return@addSnapshotListener
                }
                val schedules = querySnapshot?.documents?.mapNotNull { document ->
                    val schedule = document.toObject(Schedule::class.java) ?: return@mapNotNull null
                    if (currentYear in schedule.startYear..schedule.endYear &&
                        isMonthWithinRange(currentYear, currentMonth, schedule)
                    ) {
                        schedule
                    } else {
                        null
                    }
                } ?: emptyList()
                callback(Result.success(schedules))
            }
    }


    fun isMonthWithinRange(currentYear: Int, currentMonth: Int, schedule: Schedule): Boolean {
        return when {
            schedule.startYear == schedule.endYear -> currentMonth in schedule.startMonth..schedule.endMonth
            currentYear == schedule.startYear -> currentMonth >= schedule.startMonth
            currentYear == schedule.endYear -> currentMonth <= schedule.endMonth
            else -> true
        }
    }


    fun getUpcomingSchedules(
        tutorEmail: String,
        callback: (Result<List<Schedule>>) -> Unit
    ) {
        firestore.collection("saved_schedules")
            .whereEqualTo("tutorEmail", tutorEmail)
            .whereEqualTo("approved", true)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    callback(Result.failure(error))
                    return@addSnapshotListener
                }
                val schedules = querySnapshot?.documents?.mapNotNull { document ->
                    document.toObject(Schedule::class.java)
                } ?: emptyList()
                callback(Result.success(schedules))
            }
    }



    fun getInstantClassDetails(classDocumentId: String, callback: (Result<SavedClass>) -> Unit) {
        firestore.collection("saved_instant_classes")
            .document(classDocumentId)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    callback(Result.failure(error))
                    return@addSnapshotListener
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val savedClass = documentSnapshot.toObject(SavedClass::class.java)
                    if (savedClass != null) {
                        callback(Result.success(savedClass))
                    } else {
                        callback(Result.failure(Exception("Clase no encontrada")))
                    }
                } else {
                    callback(Result.failure(Exception("Documento nulo o no existe")))
                }
            }
    }

    fun getStudentsNow(
        classDocumentId: String,
        callback: (Result<List<SavedStudent>>) -> Unit
    ) {
        firestore.collection("saved_instant_classes")
            .document(classDocumentId)
            .collection("students")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    callback(Result.failure(error))
                    return@addSnapshotListener
                }
                val students = querySnapshot?.documents?.mapNotNull { document ->
                    document.toObject(SavedStudent::class.java)
                } ?: emptyList()
                callback(Result.success(students))
            }
    }

    fun getInstantClasses(
        callback: (Result<List<Pair<String, SavedClass>>>) -> Unit
    ) {
        val email = authManager.getUserEmail()
        firestore.collection("saved_instant_classes")
            .whereEqualTo("tutorEmail", email)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    callback(Result.failure(error))
                    return@addSnapshotListener
                }

                val classes = querySnapshot?.documents?.mapNotNull { document ->
                    val savedClass = document.toObject(SavedClass::class.java)
                    if (savedClass != null) {
                        Pair(document.id, savedClass)
                    } else {
                        null
                    }
                } ?: emptyList()

                callback(Result.success(classes))
            }
    }


}

