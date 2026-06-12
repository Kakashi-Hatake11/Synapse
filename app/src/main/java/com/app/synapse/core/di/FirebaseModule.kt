package com.app.synapse.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

//    @Provides
//    @Singleton
//    fun provideFirebaseFirestore(): FirebaseFirestore {
//        return Firebase.firestore
//        // Example with settings:
//        // val settings = FirebaseFirestoreSettings.Builder()
//        //    .setPersistenceEnabled(true) // Enable offline persistence
//        //    .build()
//        // val firestore = FirebaseFirestore.getInstance()
//        // firestore.firestoreSettings = settings
//        // return firestore
//    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        // This is provided if you plan to use Firebase Cloud Storage, e.g., for images.
        // If not, you can remove this provider.
        return FirebaseStorage.getInstance()
    }
}
