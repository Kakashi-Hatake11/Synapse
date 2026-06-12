package com.app.synapse.data.repository

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val currentSessionId: Flow<String?>
    suspend fun saveSessionId(sessionId: String): Result<Unit>
    suspend fun clearSessionId(): Result<Unit>
    suspend fun getSessionIdOnce(): String? // For immediate needs if flow is not ideal

    // Optional: If you decide to store a more persistent anonymous User ID
    // val currentUserId: Flow<String?>
    // suspend fun saveUserId(userId: String): Result<Unit>
}