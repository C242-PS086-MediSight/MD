package com.example.medisight.ui.page.authentication.register


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.medisight.R
import com.example.medisight.domain.result.AuthResult
import com.example.medisight.data.preferences.UserPreferences
import com.example.medisight.data.repository.AuthRepository
import com.example.medisight.databinding.ActivityRegisterBinding
import com.example.medisight.domain.usecase.LoginUseCase
import com.example.medisight.domain.usecase.RegisterUseCase
import com.example.medisight.factory.AuthViewModelFactory
import com.example.medisight.ui.page.setupprofile.SetupProfileActivity
import com.example.medisight.ui.page.authentication.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            LoginUseCase(AuthRepository(FirebaseAuth.getInstance())),
            RegisterUseCase(AuthRepository(FirebaseAuth.getInstance()))
        )
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.result
                account?.idToken?.let {
                    firebaseAuthWithGoogle(it)
                } ?: showToast(getString(R.string.google_sign_in_failed))
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Sign-in failed", e)
                showToast(getString(R.string.google_sign_in_failed))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnSignUp.setOnClickListener {
            val email = binding.edRegisterEmail.text.toString().trim()
            val password = binding.edRegisterPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()

            if (validateInput(email, password, confirmPassword)) {
                toggleLoading(true)
                viewModel.register(email, password)
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            signUpWithGoogle()
        }

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun signUpWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        toggleLoading(true)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                toggleLoading(false)
                if (task.isSuccessful) {
                    UserPreferences(this).setLoggedIn(true)
                    showToast(getString(R.string.registration_success))
                    navigateToSetUpProfile()
                } else {
                    Log.e("FirebaseAuth", "Google sign-in failed", task.exception)
                    showToast(getString(R.string.registration_failed))
                }
            }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.authState.collect { result ->
                toggleLoading(false)
                when (result) {
                    is AuthResult.Success -> {
                        UserPreferences(this@RegisterActivity).setLoggedIn(true)
                        showToast(getString(R.string.registration_success))
                        navigateToSetUpProfile()
                    }

                    is AuthResult.Error -> {
                        Log.e("AuthViewModel", "Registration error: ${result.message}")
                        showToast(result.message)
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun validateInput(
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edRegisterEmail.error = getString(R.string.error_invalid_email)
            return false
        }
        if (password.length < 8 || !password.any { it.isDigit() } || !password.any { it.isUpperCase() }) {
            binding.edRegisterPassword.error = getString(R.string.error_weak_password)
            return false
        }
        if (confirmPassword.isEmpty() || password != confirmPassword) {
            binding.editTextConfirmPassword.error = getString(R.string.error_password_mismatch)
            return false
        }
        return true
    }

    private fun toggleLoading(isLoading: Boolean) {
        binding.progressBarLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSignUp.isEnabled = !isLoading
        binding.btnGoogleSignIn.isEnabled = !isLoading
    }

    private fun navigateToSetUpProfile() {
        startActivity(Intent(this, SetupProfileActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}