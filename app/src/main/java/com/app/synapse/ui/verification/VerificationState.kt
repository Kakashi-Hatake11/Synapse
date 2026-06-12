package com.app.synapse.ui.verification

/**
 * Represents the different states for the Verification screen.
 */
sealed class VerificationState {
    object Idle : VerificationState() // Initial state
    object Loading : VerificationState()
    data class Success(val sessionId: String) : VerificationState()
    data class Error(val message: String, val exception: Throwable? = null) : VerificationState()
}
