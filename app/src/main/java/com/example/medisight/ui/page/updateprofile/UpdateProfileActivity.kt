package com.example.medisight.ui.page.updateprofile

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.medisight.R
import com.example.medisight.data.repository.UserRepository
import com.example.medisight.databinding.ActivityUpdateProfileBinding
import com.example.medisight.domain.usecase.UpdateProfileState
import com.example.medisight.factory.UpdateProfileViewModelFactory
import kotlinx.coroutines.launch
import java.util.Calendar

class UpdateProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateProfileBinding
    private lateinit var viewModel: UpdateProfileViewModel
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            binding.profileImage.setImageURI(it)
            viewModel.updateProfileImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userRepository = UserRepository(contentResolver = contentResolver)
        val factory = UpdateProfileViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory)[UpdateProfileViewModel::class.java]

        setupUI()
        observeViewModel()
        setupListeners()
    }

    private fun setupUI() {
        intent.getStringExtra("FULL_NAME")?.let {
            binding.edFullName.setText(it)
        }
        intent.getStringExtra("PHONE_NUMBER")?.let {
            binding.edPhoneNumber.setText(it)
        }
        intent.getStringExtra("DATE_OF_BIRTH")?.let {
            binding.edDateOfBirth.setText(it)
        }
        intent.getStringExtra("ADDRESS")?.let {
            binding.edAddress.setText(it)
        }

        intent.getStringExtra("PROFILE_IMAGE_URL")?.let { imageUrl ->
            Glide.with(this)
                .load(imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_blank_profile)
                        .error(R.drawable.ic_blank_profile)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                )
                .into(binding.profileImage)
        }
    }

    private fun setupListeners() {
        binding.editImageButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnUpdate.setOnClickListener {
            val fullName = binding.edFullName.text.toString()
            val phoneNumber = binding.edPhoneNumber.text.toString()
            val dateOfBirth = binding.edDateOfBirth.text.toString()
            val address = binding.edAddress.text.toString()

            viewModel.saveProfile(fullName, phoneNumber, dateOfBirth, address)
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.edDateOfBirth.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            binding.edDateOfBirth.setText(selectedDate)
        }, year, month, day).show()
    }


    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.updateState.collect { state ->
                when (state) {
                    is UpdateProfileState.Loading -> {
                        binding.progressBarLoading.visibility = View.VISIBLE
                        binding.btnUpdate.isEnabled = false
                    }

                    is UpdateProfileState.Success -> {
                        binding.progressBarLoading.visibility = View.GONE
                        Toast.makeText(
                            this@UpdateProfileActivity,
                            "Profile Updated Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }

                    is UpdateProfileState.Error -> {
                        binding.progressBarLoading.visibility = View.GONE
                        binding.btnUpdate.isEnabled = true
                        Toast.makeText(
                            this@UpdateProfileActivity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is UpdateProfileState.Idle -> {
                        binding.progressBarLoading.visibility = View.GONE
                        binding.btnUpdate.isEnabled = true
                    }
                }
            }
        }
    }
}