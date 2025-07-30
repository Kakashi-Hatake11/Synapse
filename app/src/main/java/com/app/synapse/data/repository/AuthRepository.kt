package com.app.synapse.data.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    fun getCurrentUser(): FirebaseUser?
    suspend fun signInAnonymously(): Result<FirebaseUser>

/**
 * Verifies a code and establishes a session.
 * For anonymous auth, this might just mean ensuring an anonymous user exists
 * and returning their UID as a session identifier.
 * @param code The verification code (e.g.
, "123456" for this app's purpose).
 * @return Result containing the session ID (Firebase UID in this case) on success.
 */
suspend fun verifyCode(code: String): Result<String> // Returns session ID (UID)

    suspend fun signOut()
}

