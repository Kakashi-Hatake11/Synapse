package com.app.synapse.data.repository

import com.app.synapse.data.model.Channel
import com.app.synapse.data.model.Message
import com.app.synapse.data.source.local.UserPreferencesDataSource // May not be needed here directly if senderId comes from ViewModel
import com.app.synapse.data.source.remote.FirebaseChatDataSource
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val firebaseChatDataSource: FirebaseChatDataSource,
    private val userPreferencesDataSource: UserPreferencesDataSource // Used to get current user's session ID if needed for some ops
) : ChatRepository {

    override fun getChannels(): Flow<Result<List<Channel>>> {
        return firebaseChatDataSource.getChannelsFlow()
    }

    override suspend fun createChannel(name: String, description: String?, createdByUserId: String?): Result<String> {
        val newChannel = Channel(
            name = name,
            description = description,
            createdAt = Date(), // Will be overwritten by @ServerTimestamp if model uses it
            createdBy = createdByUserId
        )
        return firebaseChatDataSource.createChannel(newChannel)
    }

    override suspend fun getChannelDetails(channelId: String): Result<Channel?> {
        return firebaseChatDataSource.getChannelDetails(channelId)
    }

    override fun getMessages(channelId: String): Flow<Result<List<Message>>> {
        return firebaseChatDataSource.getMessagesFlow(channelId)
    }

    override suspend fun sendMessage(
        channelId: String,
        text: String?,
        imageUrl: String?,
        senderId: String,
        senderName: String?
    ): Result<String> {
        if (text.isNullOrBlank() && imageUrl.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Message must have text or an image URL."))
        }
        if (senderId.isBlank()) {
            return Result.failure(IllegalArgumentException("Sender ID cannot be blank."))
        }

        val messageType = if (imageUrl != null) Message.MESSAGE_TYPE_IMAGE else Message.MESSAGE_TYPE_TEXT

        val message = Message(
            channelId = channelId,
            senderId = senderId,
            senderName = senderName ?: "Anonymous", // Default sender name
            text = text,
            imageUrl = imageUrl,
            timestamp = Date(), // Will be overwritten by @ServerTimestamp in Firestore model if used
            type = messageType
        )
        return firebaseChatDataSource.sendMessage(message)
    }

    // Example if implementing image upload directly in repository
    // override suspend fun sendImageMessage(channelId: String, imageUri: Uri, senderId: String, senderName: String?): Result<String> {
    //     // 1. Get current user ID (session ID) if needed, though senderId is passed
    //     // val currentSenderId = userPreferencesDataSource.sessionIdFlow.firstOrNull() ?: return Result.failure(Exception("User not authenticated"))

    //     // 2. Upload image to Firebase Storage
    //     val uploadResult = firebaseChatDataSource.uploadImage(channelId, imageUri, senderId)
    //     return uploadResult.fold(
    //         onSuccess = { downloadUrl ->
    //             // 3. Send message with the download URL
    //             sendMessage(channelId, text = null, imageUrl = downloadUrl, senderId = senderId, senderName = senderName)
    //         },
    //         onFailure = { exception ->
    //             Result.failure(exception)
    //         }
    //     )
    // }
}

