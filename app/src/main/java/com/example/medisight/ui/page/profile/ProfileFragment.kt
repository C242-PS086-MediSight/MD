package com.example.medisight.ui.page.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.medisight.R
import com.example.medisight.data.repository.UserRepository
import com.example.medisight.data.preferences.UserPreferences
import com.example.medisight.domain.usecase.UserProfileState
import com.example.medisight.factory.ProfileViewModelFactory
import com.example.medisight.ui.page.onboarding.OnBoardingActivity
import com.example.medisight.ui.page.updateprofile.UpdateProfileActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    companion object {
        private const val TAG = "ProfileFragment"
    }

    private lateinit var userPreferences: UserPreferences
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var profileImageCard: MaterialCardView
    private lateinit var profileImage: ImageView
    private lateinit var fullNameText: TextView
    private lateinit var phoneText: TextView
    private lateinit var dobText: TextView
    private lateinit var addressText: TextView
    private lateinit var btnUpdate: MaterialButton
    private lateinit var btnLogout: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferences = UserPreferences(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        initializeViews(view)
        setupViewModel()
        setupButtonListeners()
        observeViewModel()

        return view
    }

    private fun initializeViews(view: View) {
        profileImageCard = view.findViewById(R.id.profileImageCard)
        profileImage = view.findViewById(R.id.profileImage)
        fullNameText = view.findViewById(R.id.fullNameText)
        phoneText = view.findViewById(R.id.phoneText)
        dobText = view.findViewById(R.id.dobText)
        addressText = view.findViewById(R.id.addressText)
        btnUpdate = view.findViewById(R.id.btnUpdate)
        btnLogout = view.findViewById(R.id.btnLogout)
    }

    private fun setupViewModel() {
        val userRepository = UserRepository(contentResolver = requireActivity().contentResolver)
        val factory = ProfileViewModelFactory(userRepository)
        profileViewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
    }

    private fun setupButtonListeners() {
        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        btnUpdate.setOnClickListener {
            val intent = Intent(requireContext(), UpdateProfileActivity::class.java).apply {
                putExtra("FULL_NAME", fullNameText.text.toString())
                putExtra("PHONE_NUMBER", phoneText.text.toString())
                putExtra("DATE_OF_BIRTH", dobText.text.toString())
                putExtra("ADDRESS", addressText.text.toString())
                putExtra("PROFILE_IMAGE_URL", profileViewModel.currentProfileImageUrl)
            }
            startActivity(intent)
        }
    }


    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.userProfile.collect { state ->
                when (state) {
                    is UserProfileState.Loading -> {
                        resetUIToDefault()
                        view?.findViewById<ProgressBar>(R.id.progressBarLoading)?.visibility =
                            View.VISIBLE
                    }

                    is UserProfileState.Success -> {
                        view?.findViewById<ProgressBar>(R.id.progressBarLoading)?.visibility =
                            View.GONE
                        displayUserProfile(state)
                    }

                    is UserProfileState.Error -> {
                        view?.findViewById<ProgressBar>(R.id.progressBarLoading)?.visibility =
                            View.GONE
                        handleError(state.message)
                    }
                }
            }
        }
    }


    private fun displayUserProfile(state: UserProfileState.Success) {
        val user = state.user

        fullNameText.text = user.fullName
        phoneText.text = user.phoneNumber
        dobText.text = user.dateOfBirth
        addressText.text = user.address
        loadProfileImage(user.profileImageUrl)
    }

    private fun loadProfileImage(imageUrl: String?) {
        imageUrl?.let { url ->
            Log.d(TAG, "Attempting to load image from URL: $url")

            Glide.with(requireContext())
                .load(url)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_blank_profile)
                        .error(R.drawable.ic_blank_profile)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                )
                .into(profileImage)
        } ?: run {
            Log.d(TAG, "No image URL provided. Setting default profile image.")
            profileImage.setImageResource(R.drawable.ic_blank_profile)
        }
    }

    private fun resetUIToDefault() {
        fullNameText.text = getString(R.string.profile_name)
        phoneText.text = getString(R.string.profile_phone)
        dobText.text = getString(R.string.date_of_birth_hint)
        addressText.text = getString(R.string.address_hint_example)
        profileImage.setImageResource(R.drawable.ic_blank_profile)
    }

    private fun handleError(message: String) {
        resetUIToDefault()
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        Log.e(TAG, "Profile Error: $message")
    }

    private fun showLogoutConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.custom_logout_dialog, null)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)

        val dialog = alertDialog.create()

        val btnCancel: MaterialButton = dialogView.findViewById(R.id.btnCancel)
        val btnLogout: MaterialButton = dialogView.findViewById(R.id.btnLogout)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnLogout.setOnClickListener {
            handleLogout()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun handleLogout() {
        userPreferences.clearSession()
        val intent = Intent(requireContext(), OnBoardingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}