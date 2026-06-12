package com.app.synapse.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.app.synapse.data.repository.AuthRepository
import com.app.synapse.data.repository.AuthRepositoryImpl
import com.app.synapse.data.repository.ChatRepository
import com.app.synapse.data.repository.ChatRepositoryImpl
import com.app.synapse.data.repository.UserRepository
import com.app.synapse.data.repository.UserRepositoryImpl
import com.app.synapse.data.source.local.UserPreferencesDataSource
import com.app.synapse.data.source.remote.FirebaseAuthDataSource
import com.app.synapse.data.source.remote.FirebaseChatDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

// DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Firebase Services
//    @Provides
//    @Singleton
//    fun provideFirebaseAuth(): FirebaseAuth{
//        return FirebaseAuth.getInstance()
//    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

//    @Provides
//    @Singleton
//    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    // DataSources
    @Provides
    @Singleton
    fun provideFirebaseAuthDataSource(
        firebaseAuth: FirebaseAuth,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): FirebaseAuthDataSource =
        FirebaseAuthDataSource(firebaseAuth, ioDispatcher)

    @Provides
    @Singleton
    fun provideFirebaseChatDataSource(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): FirebaseChatDataSource =
        FirebaseChatDataSource(firestore, storage, ioDispatcher)

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideUserPreferencesDataSource(
        dataStore: DataStore<Preferences>,
       // @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): UserPreferencesDataSource =
        UserPreferencesDataSource(dataStore)


    // Repositories
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuthDataSource: FirebaseAuthDataSource
        // No local auth data source needed for this simple case beyond session ID
    ): AuthRepository =
        AuthRepositoryImpl(firebaseAuthDataSource)

    @Provides
    @Singleton
    fun provideUserRepository(
        userPreferencesDataSource: UserPreferencesDataSource
    ): UserRepository =
        UserRepositoryImpl(userPreferencesDataSource)

    @Provides
    @Singleton
    fun provideChatRepository(
        firebaseChatDataSource: FirebaseChatDataSource,
        userPreferencesDataSource: UserPreferencesDataSource // May not be strictly needed by ChatRepositoryImpl directly
        // if senderId is always passed from ViewModel,
        // but kept for consistency or future use.
    ): ChatRepository =
        ChatRepositoryImpl(firebaseChatDataSource, userPreferencesDataSource)


    // Coroutine Dispatchers (Optional, but good practice)
    @DefaultDispatcher
    @Provides
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @IoDispatcher
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}

// Qualifier annotations for dispatchers
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher
