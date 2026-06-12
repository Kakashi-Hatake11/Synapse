package com.app.synapse.ui.verification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.synapse.core.utils.Event
import com.app.synapse.data.repository.AuthRepository
import com.app.synapse.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _verificationState = MutableLiveData<VerificationState>(VerificationState.Idle)
    val verificationState: LiveData<VerificationState> = _verificationState

    // For navigation event to MainActivity or another screen after successful verification
    private val _navigateToMain = MutableLiveData<Event<String>>() // String here is the session ID
    val navigateToMain: LiveData<Event<String>> = _navigateToMain

    init {
        checkIfAlreadyLoggedIn()
    }

    private fun checkIfAlreadyLoggedIn() {
        viewModelScope.launch {
            val existingSessionId = userRepository.getSessionIdOnce()
            if (!existingSessionId.isNullOrBlank() && authRepository.getCurrentUser() != null) {
                // User is already "logged in" with an anonymous session
                _verificationState.value = VerificationState.Success(existingSessionId)
                _navigateToMain.value = Event(existingSessionId)
            }
        }
    }

    fun verifyCode(code: String) {
        if (code.isBlank()) {
            _verificationState.value = VerificationState.Error("Verification code cannot be empty.")
            return
        }

        _verificationState.value = VerificationState.Loading
        viewModelScope.launch {
            // In this app, "123456" is a placeholder to trigger anonymous sign-in
            // and get a session ID (Firebase UID).
            val verificationResult = authRepository.verifyCode(code)

            verificationResult.fold(
                onSuccess = { sessionId ->
                    val saveSessionResult = userRepository.saveSessionId(sessionId)
                    saveSessionResult.fold(
                        onSuccess = {
                            _verificationState.value = VerificationState.Success(sessionId)
                            _navigateToMain.value = Event(sessionId)
                        },
                        onFailure = { saveError ->
                            _verificationState.value = VerificationState.Error(
                                saveError.message ?: "Failed to save session.",
                                saveError
                            )
                        }
                    )
                },
                onFailure = { error ->
                    _verificationState.value = VerificationState.Error(
                        error.message ?: "Verification failed.",
                        error
                    )
                }
            )
        }
    }
}
