package com.app.synapse.ui.main

import com.app.synapse.data.model.Channel

/**
 * Represents the different UI states for the Channel List screen.
 */
sealed class ChannelListUiState {
    object Loading : ChannelListUiState()
    data class Success(val channels: List<Channel>) : ChannelListUiState()
    object Empty : ChannelListUiState() // When there are no channels but the fetch was successful
    data class Error(val message: String, val exception: Throwable? = null) : ChannelListUiState()
}
