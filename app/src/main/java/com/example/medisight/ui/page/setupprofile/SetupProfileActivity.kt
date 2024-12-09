package com.example.medisight.ui.page.setupprofile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.medisight.R
import com.example.medisight.data.repository.UserRepository
import com.example.medisight.data.response.UserResponse
import com.example.medisight.factory.SetupProfileViewModelFactory
import com.example.medisight.ui.page.bottomnavigation.MainActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class SetupProfileActivity : AppCompatActivity() {
    private lateinit var viewModel: SetupProfileViewModel
    private var selectedImageUri: Uri? = null

    private lateinit var profileImage: ImageView
    private lateinit var editImageButton: ImageButton
    private lateinit var edFullName: EditText
    private lateinit var edPhoneNumber: EditText
    private lateinit var edDateOfBirth: EditText
    private lateinit var edAddress: EditText
    private lateinit var btnSave: Button
    private lateinit var progressBarLoading: ProgressBar

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    Glide.with(this).load(uri).into(profileImage)
                    lifecycleScope.launch { viewModel.uploadProfileImage(uri) }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile_setup)

        val storageOptions = FirebaseOptions.Builder()
            .setApplicationId("eleven-barbershop")
            .setApiKey("AIzaSyBWv2biy73oZJuSkBbYXFx2A971m3NWJu0")
            .setDatabaseUrl("https://eleven-barbershop-default-rtdb.firebaseio.com")
            .setProjectId("eleven-barbershop")
            .setStorageBucket("eleven-barbershop.appspot.com")
            .build()

        FirebaseApp.initializeApp(this, storageOptions, "storageApp")
        val storage = FirebaseStorage.getInstance(FirebaseApp.getInstance("storageApp"))

        initializeViews()
        initializeViewModel()
        setupListeners()

        savedInstanceState?.let { bundle ->
            edFullName.setText(bundle.getString("fullName"))
            edPhoneNumber.setText(bundle.getString("phoneNumber"))
            edDateOfBirth.setText(bundle.getString("dateOfBirth"))
            edAddress.setText(bundle.getString("address"))
            selectedImageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable("imageUri", Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable("imageUri")
            }
            selectedImageUri?.let { uri ->
                Glide.with(this).load(uri).into(profileImage)
            }
        }
    }

    private fun initializeViews() {
        profileImage = findViewById(R.id.profileImage)
        editImageButton = findViewById(R.id.editImageButton)
        edFullName = findViewById(R.id.edFullName)
        edPhoneNumber = findViewById(R.id.edPhoneNumber)
        edDateOfBirth = findViewById(R.id.edDateOfBirth)
        edAddress = findViewById(R.id.edAddress)
        btnSave = findViewById(R.id.btnSave)
        progressBarLoading = findViewById(R.id.progressBarLoading)
    }

    private fun initializeViewModel() {
        val userRepository = UserRepository(contentResolver = contentResolver)
        viewModel = ViewModelProvider(
            this,
            SetupProfileViewModelFactory(userRepository)
        )[SetupProfileViewModel::class.java]
        observeViewModel()
    }

    private fun setupListeners() {
        editImageButton.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(galleryIntent)
        }

        edDateOfBirth.setOnClickListener {
            showDatePicker()
        }
        edDateOfBirth.isFocusable = false

        btnSave.setOnClickListener { saveUserProfile() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            edDateOfBirth.setText(selectedDate)
        }, year, month, day).show()
    }

    private fun saveUserProfile() {
        val fullName = edFullName.text.toString().trim()
        val phoneNumber = edPhoneNumber.text.toString().trim()
        val dateOfBirth = edDateOfBirth.text.toString().trim()
        val address = edAddress.text.toString().trim()

        if (fullName.isBlank() || phoneNumber.isBlank() || dateOfBirth.isBlank() || address.isBlank()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        } else {
            progressBarLoading.visibility = View.VISIBLE
            btnSave.isEnabled = false

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            userId?.let { uid ->
                val userProfileRef = FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(uid)

                val userProfile = hashMapOf(
                    "fullName" to fullName,
                    "phoneNumber" to phoneNumber,
                    "dateOfBirth" to dateOfBirth,
                    "address" to address,
                    "profileImageUrl" to viewModel.profileImageUrl
                )

                userProfileRef.setValue(userProfile)
                    .addOnSuccessListener {
                        progressBarLoading.visibility = View.GONE
                        btnSave.isEnabled = true
                        Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT)
                            .show()
                        navigateToMainActivity()
                    }
                    .addOnFailureListener { e ->
                        progressBarLoading.visibility = View.GONE
                        btnSave.isEnabled = true
                        Toast.makeText(
                            this,
                            "Failed to save profile: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }


    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.userResponse.collectLatest { response ->
                when (response) {
                    is UserResponse.Loading -> {
                        if (response.isLoading) {
                            progressBarLoading.visibility = View.VISIBLE
                            btnSave.isEnabled = false
                        } else {
                            progressBarLoading.visibility = View.GONE
                            btnSave.isEnabled = true
                        }
                    }

                    is UserResponse.Success -> {
                        progressBarLoading.visibility = View.GONE
                        btnSave.isEnabled = true
                        Toast.makeText(
                            this@SetupProfileActivity,
                            "Operation successful",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is UserResponse.Error -> {
                        progressBarLoading.visibility = View.GONE
                        btnSave.isEnabled = true
                        Toast.makeText(
                            this@SetupProfileActivity,
                            "Error: ${response.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putString("fullName", edFullName.text.toString())
            putString("phoneNumber", edPhoneNumber.text.toString())
            putString("dateOfBirth", edDateOfBirth.text.toString())
            putString("address", edAddress.text.toString())
            putParcelable("imageUri", selectedImageUri)
        }
    }
}