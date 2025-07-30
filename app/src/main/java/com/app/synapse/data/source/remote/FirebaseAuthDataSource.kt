package com.app.synapse.data.source.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val ioDispatcher: CoroutineDispatcher // Add ioDispatcher here
) {

    fun getCurrentUser(): FirebaseUser? {
        // This is a synchronous call, dispatcher might not be strictly needed here,
        // but shown for consistency if other methods use it.
        return firebaseAuth.currentUser
    }

    suspend fun signInAnonymously(): Result<FirebaseUser> {
        // Use withContext if you want to ensure this block runs on the ioDispatcher
        return withContext(ioDispatcher) {
            try {
                val authResult = firebaseAuth.signInAnonymously().await()
                authResult.user?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Firebase user is null after anonymous sign-in."))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun verifyCodeAndEstablishSession(code: String): Result<String> {
        return withContext(ioDispatcher) { // Example usage
            if (code == "123456") {
                try {
                    val user = firebaseAuth.currentUser ?: firebaseAuth.signInAnonymously().await().user
                    user?.uid?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Failed to establish anonymous session or get UID."))
                } catch (e: Exception) {
                    Result.failure(Exception("Error during anonymous session establishment: ${e.message}", e))
                }
            } else {
                Result.failure(Exception("Invalid verification code provided."))
            }
        }
    }

    suspend fun signOut() {
        withContext(ioDispatcher) { // Example usage
            try {
                firebaseAuth.signOut()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
