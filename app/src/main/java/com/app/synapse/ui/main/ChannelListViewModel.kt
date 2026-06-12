package com.app.synapse.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.synapse.core.utils.Event
import com.app.synapse.data.model.Channel
import com.app.synapse.data.repository.AuthRepository
import com.app.synapse.data.repository.ChatRepository
import com.app.synapse.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository, // For current user info
    private val userRepository: UserRepository // For session/user ID if needed for creation
) : ViewModel() {

    private val _uiState = MutableLiveData<ChannelListUiState>(ChannelListUiState.Loading)
    val uiState: LiveData<ChannelListUiState> = _uiState

    // For navigation to ChatActivity
    private val _navigateToChat = MutableLiveData<Event<Channel>>()
    val navigateToChat: LiveData<Event<Channel>> = _navigateToChat

    // For navigation back to Verification if session is lost/invalid
    private val _navigateToVerification = MutableLiveData<Event<Unit>>()
    val navigateToVerification: LiveData<Event<Unit>> = _navigateToVerification

    private var currentUserId: String? = null

    init {
        fetchCurrentUserAndChannels()
    }

    private fun fetchCurrentUserAndChannels() {
        viewModelScope.launch {
            // Ensure user is still valid
            val user = authRepository.getCurrentUser()
            currentUserId = user?.uid
            if (user == null || currentUserId.isNullOrBlank()) {
                // User session lost or invalid, navigate back to verification
                userRepository.clearSessionId() // Clear potentially stale session ID
                _navigateToVerification.value = Event(Unit)
                return@launch
            }
            loadChannels()
        }
    }


    private fun loadChannels() {
        viewModelScope.launch {
            _uiState.value = ChannelListUiState.Loading
            chatRepository.getChannels().collectLatest { result ->
                result.fold(
                    onSuccess = { channels ->
                        if (channels.isEmpty()) {
                            _uiState.value = ChannelListUiState.Empty
                        } else {
                            _uiState.value = ChannelListUiState.Success(channels)
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = ChannelListUiState.Error(
                            error.message ?: "Failed to load channels.",
                            error
                        )
                    }
                )
            }
        }
    }

    fun onChannelClicked(channel: Channel) {
        _navigateToChat.value = Event(channel)
    }

    fun createNewChannel(channelName: String, description: String?) {
        if (channelName.isBlank()) {
            // Optionally, expose an error LiveData for the dialog/UI to show this
            // _createChannelError.value = Event("Channel name cannot be empty.")
            return
        }

        viewModelScope.launch {
            // _uiState.value = ChannelListUiState.Loading // Or a specific creating state
            val result = chatRepository.createChannel(channelName, description, currentUserId)
            result.fold(
                onSuccess = { _ ->
                    // Channel created, list will refresh due to Firestore listener
                    // Optionally, navigate to the new channel directly or show a success message
                    // For simplicity, we just let the list refresh.
                },
                onFailure = { error ->
                    _uiState.value = ChannelListUiState.Error( // Revert to previous state or show specific error
                        error.message ?: "Failed to create channel.",
                        error
                    )
                }
            )
        }
    }

    fun refreshChannels() {
        fetchCurrentUserAndChannels() // Re-check user and reload
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            userRepository.clearSessionId()
            _navigateToVerification.value = Event(Unit)
        }
    }
}
