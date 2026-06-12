package com.app.synapse.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.synapse.core.utils.Constants
import com.app.synapse.core.utils.Event
import com.app.synapse.data.model.Message
import com.app.synapse.data.repository.AuthRepository
import com.app.synapse.data.repository.ChatRepository
import com.app.synapse.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository // To get current user's session ID for sending messages
) : ViewModel() {

    private val _uiState = MutableLiveData<ChatUiState>()
    val uiState: LiveData<ChatUiState> = _uiState

    private val _messageSentStatus = MutableLiveData<Event<Boolean>>()
    val messageSentStatus: LiveData<Event<Boolean>> = _messageSentStatus

    private var currentChannelId: String? = null
    private var currentUserId: String? = null
    private var currentUserName: String? = Constants.DEFAULT_ANONYMOUS_DISPLAY_NAME // Default

    init {
        viewModelScope.launch {
            currentUserId = userRepository.getSessionIdOnce()
            // In a real app with profiles, you might fetch the user's display name
            // For anonymous, FirebaseUser.displayName is usually null unless set manually after creation
            authRepository.getCurrentUser()?.let { _ ->
                // currentUserName = firebaseUser.displayName ?: currentUserName
            }
        }
    }

    fun loadMessages(channelId: String) {
        if (channelId == currentChannelId && _uiState.value is ChatUiState.Success) {
            // Already loaded and viewing this channel's messages
            return
        }
        currentChannelId = channelId
        _uiState.value = ChatUiState.Loading

        viewModelScope.launch {
            if (currentUserId.isNullOrBlank()) {
                currentUserId = userRepository.getSessionIdOnce() // Ensure we have it
                if (currentUserId.isNullOrBlank()) {
                    _uiState.value = ChatUiState.Error("User session not found. Cannot load messages.")
                    // Potentially navigate back or show a permanent error
                    return@launch
                }
            }

            chatRepository.getMessages(channelId).collectLatest { result ->
                result.fold(
                    onSuccess = { messages ->
                        if (messages.isEmpty()) {
                            _uiState.value = ChatUiState.Empty
                        } else {
                            _uiState.value = ChatUiState.Success(messages)
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = ChatUiState.Error(
                            error.message ?: "Failed to load messages.",
                            error
                        )
                    }
                )
            }
        }
    }

    fun sendMessage(text: String) {
        val channelId = currentChannelId ?: return // Should not happen if UI is active
        val senderId = currentUserId ?: return // Should have this if user is in chat

        if (text.isBlank()) {
            // Optionally, provide feedback that message can't be empty
            _messageSentStatus.value = Event(false) // Indicate failure to send (empty)
            return
        }

        viewModelScope.launch {
            // Optimistic UI update can be done here by adding to a local list
            // and then confirming or reverting based on repository result.
            // For simplicity, we wait for the repo.

            val result = chatRepository.sendMessage(
                channelId = channelId,
                text = text,
                imageUrl = null, // For now, only text messages
                senderId = senderId,
                senderName = currentUserName // Or a more dynamic name if available
            )

            result.fold(
                onSuccess = { _ ->
                    _messageSentStatus.value = Event(true)
                    // List will auto-update due to Firestore listener in getMessagesFlow
                },
                onFailure = { error ->
                    _messageSentStatus.value = Event(false)
                    // Optionally, update UI state to show a temporary send error for this message
                    _uiState.value = ChatUiState.Error( // Or a more specific error type
                        error.message ?: "Failed to send message.",
                        error
                    )
                }
            )
        }
    }

    fun getCurrentUserId(): String? {
        return currentUserId
    }
}
