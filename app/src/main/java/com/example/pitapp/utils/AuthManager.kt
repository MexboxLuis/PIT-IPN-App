package com.example.pitapp.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthManager(private val auth: FirebaseAuth) {

    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("Email is already in use."))
        } catch (e: Exception) {
            Result.failure(Exception("Registration failed: ${e.localizedMessage}"))
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Invalid credentials. Please try again."))
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("No account found with this email."))
        } catch (e: Exception) {
            Result.failure(Exception("Login failed: ${e.localizedMessage}"))
        }
    }

    suspend fun resetPassword(email: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to send password reset email: ${e.localizedMessage}"))
        }
    }

    fun deleteUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            currentUser.delete()
                .addOnCompleteListener { deleteTask ->
                    if (deleteTask.isSuccessful) {
                        Log.d("AuthManager", "User account deleted.")
                    } else {
                        Log.e("AuthManager", "Failed to delete user: ${deleteTask.exception?.localizedMessage}")
                    }
                }
        } else {
            Log.e("AuthManager", "No user is currently signed in.")
        }
    }




    fun getCurrentUser(): Result<FirebaseUser?> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                Result.success(currentUser)
            } else {
                Result.failure(Exception("No user is currently logged in."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to retrieve current user: ${e.localizedMessage}"))
        }
    }

    fun logout(): Result<Boolean> {
        return try {
            auth.signOut()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to log out: ${e.localizedMessage}"))
        }
    }

    fun getUserEmail(): String? {
        val currentUser = auth.currentUser
        return currentUser?.email
    }

    suspend fun isUserRegistered(email: String): Boolean {
        return try {
            val signInMethods = auth.fetchSignInMethodsForEmail(email).await()
            signInMethods?.signInMethods?.isNotEmpty() ?: false
        } catch (e: FirebaseAuthInvalidUserException) {
            false
        } catch (e: Exception) {
            false
        }
    }


    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

}

