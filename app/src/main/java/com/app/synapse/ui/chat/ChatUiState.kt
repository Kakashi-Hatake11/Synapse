package com.app.synapse.ui.chat

import com.app.synapse.data.model.Message

/**
 * Represents the different UI states for the Chat screen.
 */
sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(val messages: List<Message>) : ChatUiState()
    object Empty : ChatUiState() // When there are no messages but the fetch was successful
    data class Error(val message: String, val exception: Throwable? = null) : ChatUiState()
}