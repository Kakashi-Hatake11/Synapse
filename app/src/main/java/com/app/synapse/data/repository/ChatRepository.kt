package com.app.synapse.data.repository

import com.app.synapse.data.model.Channel
import com.app.synapse.data.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChannels(): Flow<Result<List<Channel>>>
    suspend fun createChannel(name: String, description: String?, createdByUserId: String?): Result<String>
    suspend fun getChannelDetails(channelId: String): Result<Channel?>
    fun getMessages(channelId: String): Flow<Result<List<Message>>>
    suspend fun sendMessage(channelId: String, text: String?, imageUrl: String?, senderId: String, senderName: String?): Result<String>

    // Optional: if you implement image uploads directly via repository
    // suspend fun sendImageMessage(channelId: String, imageUri: Uri, senderId: String, senderName: String?): Result<String>
}