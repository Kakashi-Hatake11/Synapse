package com.app.synapse.data.source.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.app.synapse.core.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.remove
import kotlinx.coroutines.withContext

@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private object PreferencesKeys {
        val SESSION_ID = stringPreferencesKey(Constants.KEY_SESSION_ID)
        // val USER_ID = stringPreferencesKey(Constants.KEY_USER_ID) // Example if storing other IDs
    }

    val sessionIdFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                // Timber.e(exception, "Error reading session ID from preferences.") // Optional logging
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SESSION_ID]
        }

    suspend fun saveSessionId(sessionId: String) {
        try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.SESSION_ID] = sessionId
            }
        } catch (exception: IOException) {
            // Timber.e(exception, "Error saving session ID to preferences.") // Optional logging
            // Handle error, e.g., rethrow or return a result
        }
    }

    suspend fun clearSessionId() {
        try {
            dataStore.edit { preferences ->
                preferences.remove(PreferencesKeys.SESSION_ID)
            }
        } catch (exception: IOException) {
            // Timber.e(exception, "Error clearing session ID from preferences.") // Optional logging
        }
    }

    // Example for other preferences:
    // val userIdFlow: Flow<String?> = dataStore.data.map { preferences ->
    //     preferences[PreferencesKeys.USER_ID]
    // }
    //
    // suspend fun saveUserId(userId: String) {
    //     dataStore.edit { preferences ->
    //         preferences[PreferencesKeys.USER_ID] = userId
    //     }
    // }
}

