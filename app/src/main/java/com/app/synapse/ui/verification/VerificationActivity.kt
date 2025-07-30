package com.app.synapse.ui.verification

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.app.synapse.R
import com.app.synapse.core.base.BaseActivity
import com.app.synapse.core.utils.Constants
import com.app.synapse.core.utils.EventObserver
import com.app.synapse.databinding.ActivityVerificationBinding
import com.app.synapse.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlin.text.trim

@AndroidEntryPoint
class VerificationActivity : BaseActivity<ActivityVerificationBinding>(
    ActivityVerificationBinding::inflate
) {
    private val viewModel: VerificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // No toolbar for this simple screen, or setup if you have one in the layout.
        // setupToolbar(binding.toolbar, R.string.title_verification) // Example

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.buttonVerify.setOnClickListener {
            val code = binding.editTextVerificationCode.text.toString().trim()
            viewModel.verifyCode(code)
            // Optionally hide keyboard
            // currentFocus?.hideKeyboard()
        }

        // You can set a default code for easier testing if desired, or remove this
        binding.editTextVerificationCode.setText(R.string.default_verification_code_for_testing)
    }

    private fun observeViewModel() {
        viewModel.verificationState.observe(this) { state ->
            when (state) {
                is VerificationState.Idle -> {
                    hideProgress()
                    binding.buttonVerify.isEnabled = true
                    binding.textViewError.visibility = View.GONE
                }
                is VerificationState.Loading -> {
                    showProgress()
                    binding.buttonVerify.isEnabled = false
                    binding.textViewError.visibility = View.GONE
                }
                is VerificationState.Success -> {
                    hideProgress()
                    binding.buttonVerify.isEnabled = true // Or navigate away immediately
                    // Navigation is handled by the _navigateToMain event
                }
                is VerificationState.Error -> {
                    hideProgress()
                    binding.buttonVerify.isEnabled = true
                    binding.textViewError.text = state.message
                    binding.textViewError.visibility = View.VISIBLE
                    // showToast(state.message) // Alternative or additional feedback
                }
            }
        }

        viewModel.navigateToMain.observe(this, EventObserver { sessionId ->
            // Successfully verified and session ID obtained
            showToast(getString(R.string.toast_verification_successful))
            val intent = Intent(this, MainActivity::class.java).apply {
                // Pass session ID if MainActivity needs it directly, though it can also get it from UserRepository
                putExtra(Constants.EXTRA_SESSION_ID, sessionId)
                // Clear back stack so user cannot go back to verification
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish() // Finish VerificationActivity
        })
    }
}
