package com.app.synapse.core.utils

object Constants {

    // Preferences DataStore Keys (or names)
    const val PREFERENCES_DATASTORE_NAME = "synapse_user_prefs"
    const val KEY_SESSION_ID = "session_id"
    const val KEY_USER_ID = "user_id" // If you decide to store a more persistent anonymous ID

    // Intent Extras
    const val EXTRA_SESSION_ID = "extra_session_id"
    const val EXTRA_CHANNEL_ID = "extra_channel_id"
    const val EXTRA_CHANNEL_NAME = "extra_channel_name" // If needed

    // Firebase Firestore Collection Names
    const val COLLECTION_CHANNELS = "channels"
    const val COLLECTION_MESSAGES = "messages"
    const val COLLECTION_USERS = "users" // If storing any user-specific (even anonymous) data

    // Message Types (for adapter ViewHolders)
    const val VIEW_TYPE_MESSAGE_SENT = 1
    const val VIEW_TYPE_MESSAGE_RECEIVED = 2

    // Other constants
    const val DEFAULT_ANONYMOUS_DISPLAY_NAME = "Anonymous User"
    const val MAX_MESSAGE_LENGTH = 1000

}
