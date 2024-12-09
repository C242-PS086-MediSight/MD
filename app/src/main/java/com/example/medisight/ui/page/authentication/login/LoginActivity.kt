package com.example.medisight.ui.page.authentication.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medisight.R
import com.example.medisight.data.preferences.UserPreferences
import com.example.medisight.databinding.ActivityLoginBinding
import com.example.medisight.ui.page.bottomnavigation.MainActivity
import com.example.medisight.ui.page.onboarding.OnBoardingActivity
import com.example.medisight.ui.page.resetpassword.SendVerificationActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        setupGoogleSignIn()

        binding.btnLogin.setOnClickListener { loginWithEmail() }
        binding.btnGoogleSignIn.setOnClickListener { loginWithGoogle() }
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, SendVerificationActivity::class.java))
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, OnBoardingActivity::class.java)
            startActivity(intent)
            finish()
        }


        binding.progressBarLoading.visibility = View.GONE
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun loginWithEmail() {
        val email = binding.edLoginEmail.text.toString().trim()
        val password = binding.edLoginPassword.text.toString().trim()

        if (!validateInputs(email, password)) return

        binding.progressBarLoading.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.progressBarLoading.visibility = View.GONE
                binding.btnLogin.isEnabled = true

                if (task.isSuccessful) {
                    UserPreferences(this).setLoggedIn(true)
                    Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.error_invalid_credentials),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun loginWithGoogle() {
        binding.progressBarLoading.visibility = View.VISIBLE
        binding.btnGoogleSignIn.isEnabled = false

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(Exception::class.java) as GoogleSignInAccount
                authenticateWithFirebase(account)
            } catch (e: Exception) {
                binding.progressBarLoading.visibility = View.GONE
                binding.btnGoogleSignIn.isEnabled = true
                Toast.makeText(this, getString(R.string.google_sign_in_failed), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun authenticateWithFirebase(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                binding.progressBarLoading.visibility = View.GONE
                binding.btnGoogleSignIn.isEnabled = true

                if (task.isSuccessful) {
                    UserPreferences(this).setLoggedIn(true)
                    Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.google_sign_in_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        binding.edLoginEmail.error = null
        binding.edLoginPassword.error = null

        if (email.isEmpty()) {
            binding.edLoginEmail.error = getString(R.string.error_empty_email)
            return false
        }
        if (password.isEmpty()) {
            binding.edLoginPassword.error = getString(R.string.error_empty_password)
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edLoginEmail.error = getString(R.string.error_invalid_email)
            return false
        }
        return true
    }
}