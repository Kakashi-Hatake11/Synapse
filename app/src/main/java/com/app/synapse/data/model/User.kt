package com.app.synapse.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Represents a user in the system.
 * For an anonymous chat app, this might be very minimal or even implicitly handled by session IDs.
 * However, creating a model can be useful if you decide to store any user-specific (even if anonymous)
 * preferences or metadata in Firestore later.
 *
 * @param id The unique ID of the user (often the Firebase Auth UID).
 * @param displayName A display name, which could be auto-generated or "Anonymous".
 * @param lastSeen Timestamp of when the user was last active (optional, for presence).
 * @param createdAt Timestamp of when the user record was created.
 */
data class User(
    val id: String = "", // Firebase Auth UID
    val displayName: String? = null,
    // val photoUrl: String? = null, // If you ever allow profile pictures
    @ServerTimestamp
    val lastSeen: Date? = null, // For basic presence, Firestore will set this
    @ServerTimestamp
    val createdAt: Date? = null // Firestore will set this
) {
    // No-argument constructor for Firestore deserialization
    constructor() : this("", null, null, null)
}
