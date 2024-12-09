package com.example.medisight.ui.page.resetpassword


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medisight.R
import com.example.medisight.ui.custom.EmailEditText
import com.example.medisight.ui.page.authentication.login.LoginActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class SendVerificationActivity : AppCompatActivity() {
    private lateinit var emailInput: EmailEditText
    private lateinit var btnVerification: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_verification)

        emailInput = findViewById(R.id.ed_login_email)
        backButton = findViewById(R.id.btnBack)
        btnVerification = findViewById(R.id.btnVerification)
        progressBar = findViewById(R.id.progressBarLoading)
        progressBar.visibility = View.GONE

        firebaseAuth = FirebaseAuth.getInstance()

        btnVerification.setOnClickListener {
            sendVerificationCode()
        }

        backButton.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun sendVerificationCode() {
        val email = emailInput.text.toString().trim().lowercase()

        if (email.isEmpty()) {
            emailInput.error = getString(R.string.error_empty_email)
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = getString(R.string.error_invalid_email)
            return
        }

        progressBar.visibility = View.VISIBLE

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    Log.d("SendVerification", "Password reset email sent to $email")
                    Toast.makeText(
                        this,
                        "Password reset email sent. Please check your inbox.",
                        Toast.LENGTH_SHORT
                    ).show()

                    val loginIntent = Intent(this, LoginActivity::class.java)
                    startActivity(loginIntent)
                    finish()
                } else {
                    Log.e("SendVerification", "Error sending password reset email", task.exception)
                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}