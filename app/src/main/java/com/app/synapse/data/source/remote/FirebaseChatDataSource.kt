package com.app.synapse.data.source.remote

import android.net.Uri // Added for uploadImage example
import com.app.synapse.core.utils.Constants
import com.app.synapse.data.model.Channel
import com.app.synapse.data.model.Message
import com.app.synapse.core.di.IoDispatcher // Assuming your qualifier is in this package
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage // Added
import kotlinx.coroutines.CoroutineDispatcher // Added
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext // Added for using the dispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseChatDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage, // Added FirebaseStorage
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher // Added CoroutineDispatcher
) {

    fun getChannelsFlow(): Flow<Result<List<Channel>>> = callbackFlow {
        val channelsCollection = firestore.collection(Constants.COLLECTION_CHANNELS)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

        val listenerRegistration = channelsCollection.addSnapshotListener { snapshots, error ->
            if (error != null) {
                trySend(Result.failure(error)).isSuccess
                close(error)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val channels = snapshots.toObjects(Channel::class.java)
                trySend(Result.success(channels)).isSuccess
            } else {
                trySend(Result.success(emptyList())).isSuccess
            }
        }
        awaitClose { listenerRegistration.remove() }
    } // No explicit dispatcher needed here as addSnapshotListener manages its own threads

    suspend fun createChannel(channel: Channel): Result<String> = withContext(ioDispatcher) {
        try {
            val documentReference = firestore.collection(Constants.COLLECTION_CHANNELS).add(channel).await()
            Result.success(documentReference.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChannelDetails(channelId: String): Result<Channel?> = withContext(ioDispatcher) {
        try {
            val documentSnapshot = firestore.collection(Constants.COLLECTION_CHANNELS)
                .document(channelId)
                .get()
                .await()
            val channel = documentSnapshot.toObject(Channel::class.java)
            Result.success(channel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMessagesFlow(channelId: String): Flow<Result<List<Message>>> = callbackFlow {
        val messagesCollection = firestore.collection(Constants.COLLECTION_CHANNELS)
            .document(channelId)
            .collection(Constants.COLLECTION_MESSAGES)
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val listenerRegistration = messagesCollection.addSnapshotListener { snapshots, error ->
            if (error != null) {
                trySend(Result.failure(error)).isSuccess
                close(error)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val messages = snapshots.toObjects(Message::class.java)
                trySend(Result.success(messages)).isSuccess
            } else {
                trySend(Result.success(emptyList())).isSuccess
            }
        }
        awaitClose { listenerRegistration.remove() }
    } // No explicit dispatcher needed here

    suspend fun sendMessage(message: Message): Result<String> = withContext(ioDispatcher) {
        if (message.channelId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Channel ID cannot be blank for sending a message."))
        }
        try {
            val messageRef = firestore.collection(Constants.COLLECTION_CHANNELS)
                .document(message.channelId)
                .collection(Constants.COLLECTION_MESSAGES)
                .add(message)
                .await()

            val channelUpdate = mapOf(
                "lastMessageText" to (message.text ?: (if (message.imageUrl != null) "Image" else "")),
                "lastMessageTimestamp" to message.timestamp
            )
            firestore.collection(Constants.COLLECTION_CHANNELS)
                .document(message.channelId)
                .set(channelUpdate, SetOptions.merge())
                .await()

            Result.success(messageRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Optional: If you need to upload images to Firebase Storage
    suspend fun uploadImage(channelId: String, imageUri: Uri, userId: String): Result<String> = withContext(ioDispatcher) {
        try {
            val fileName = "images/${channelId}_${System.currentTimeMillis()}_${userId}.jpg"
            val imageRef = storage.reference.child(fileName)

            val uploadTask = imageRef.putFile(imageUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            // Timber.e(e, "Error uploading image to channel $channelId")
            Result.failure(e)
        }
    }
}
