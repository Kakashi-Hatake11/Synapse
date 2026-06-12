package com.app.synapse.data.repository

import com.app.synapse.data.source.local.UserPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource
) : UserRepository {

    override val currentSessionId: Flow<String?> = userPreferencesDataSource.sessionIdFlow

    override suspend fun saveSessionId(sessionId: String): Result<Unit> {
        return try {
            userPreferencesDataSource.saveSessionId(sessionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearSessionId(): Result<Unit> {
        return try {
            userPreferencesDataSource.clearSessionId()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSessionIdOnce(): String? {
        return userPreferencesDataSource.sessionIdFlow.firstOrNull()
    }
}

