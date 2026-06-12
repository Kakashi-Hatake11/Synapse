package com.app.synapse.data.repository

import com.app.synapse.data.source.remote.FirebaseAuthDataSource
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource
    // You could also inject a local user data source if you needed to persist
    // auth-related info locally beyond what DataStore provides for session ID.
) : AuthRepository {

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuthDataSource.getCurrentUser()
    }

    override suspend fun signInAnonymously(): Result<FirebaseUser> {
        return firebaseAuthDataSource.signInAnonymously()
    }

    override suspend fun verifyCode(code: String): Result<String> {
        // For this app, "verifyCode" is a conceptual step to ensure an anonymous session
        // is active and to get a "session ID" which is the Firebase UID.
        // The actual code '123456' might not be strictly 'verified' against Firebase
        // in a cryptographic sense for anonymous auth, but rather a trigger.
        return firebaseAuthDataSource.verifyCodeAndEstablishSession(code)
    }

    override suspend fun signOut() {
        // Potentially clear local user data here if needed before signing out from Firebase
        firebaseAuthDataSource.signOut()
    }
}

