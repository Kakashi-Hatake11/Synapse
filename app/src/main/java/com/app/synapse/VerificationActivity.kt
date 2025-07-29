package com.app.synapse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class VerificationActivity : AppCompatActivity() {

    private lateinit var codeEditText: EditText
    private lateinit var verifyButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verification)

        val container = findViewById<View>(R.id.verification_container)
        ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        codeEditText = findViewById(R.id.editText_verification_code)
        verifyButton = findViewById(R.id.button_verify)
        progressBar = findViewById(R.id.progressBar_verification)

        verifyButton.setOnClickListener {
            val code = codeEditText.text.toString().trim()
            if (code.isNotEmpty()) {
                performVerification(code)
            } else {
                Toast.makeText(this, "Please enter a verification code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performVerification(code: String) {
        progressBar.visibility = View.VISIBLE
        verifyButton.isEnabled = false
        codeEditText.isEnabled = false

        // TODO: Implement actual verification logic with Firebase or your backend.
        // This is a placeholder for demonstration.
        // For example, you might have a Firebase Function that checks the code.
        // Or a collection in Firestore/Realtime Database that stores valid codes.

        // Simulate a network delay
        android.os.Handler(mainLooper).postDelayed({
            progressBar.visibility = View.GONE
            verifyButton.isEnabled = true
            codeEditText.isEnabled = true

            // **Replace this with your actual code validation result**
            val isCodeValid = code == "TESTCODE123" // Example: Dummy validation

            if (isCodeValid) {
                Toast.makeText(this, "Verification Successful!", Toast.LENGTH_SHORT).show()

                // Successfully verified, now generate a temporary session ID.
                // This ID should be unique for this session and not personally identifiable.
                val temporarySessionId = "session_${System.currentTimeMillis()}_${(0..1000).random()}"

                // Navigate to the main chat screen (or department/channel selection screen)
                // We'll pass the temporary session ID to the next activity.
                // For now, let's assume MainActivity will be where the user selects a channel or sees a general chat.
                val intent = Intent(this, MainActivity::class.java).apply {
                    // You might want to pass some session identifier or token here
                    // For anonymity, this should NOT be PII.
                    // This could be a one-time token received from your verification backend,
                    // or a locally generated session ID if your verification simply unlocks the app.
                    putExtra("EXTRA_SESSION_ID", temporarySessionId)
                }
                startActivity(intent)
                finish() // Finish VerificationActivity so user can't go back to it with back button
            } else {
                Toast.makeText(this, "Invalid verification code. Please try again.", Toast.LENGTH_LONG).show()
            }
        }, 2000) // Simulate 2 seconds delay
    }
}