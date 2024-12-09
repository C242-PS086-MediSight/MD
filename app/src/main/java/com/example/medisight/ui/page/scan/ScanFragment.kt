package com.example.medisight.ui.page.scan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.medisight.R
import com.example.medisight.databinding.FragmentScanBinding
import com.example.medisight.helper.WoundsModelHelper
import com.example.medisight.ui.page.result.ResultActivity
import java.io.File

class ScanFragment : Fragment() {
    private lateinit var binding: FragmentScanBinding
    private lateinit var woundsModelHelper: WoundsModelHelper
    private val viewModel: ScanViewModel by viewModels()

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            if (selectedImageUri == null) {
                Toast.makeText(
                    requireContext(),
                    R.string.image_selection_failed,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                updateSelectedImage(selectedImageUri)
            }
        } else {
            Toast.makeText(requireContext(), R.string.image_selection_cancelled, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            viewModel.currentPhotoUri.value?.let { uri ->
                updateSelectedImage(uri)
            }
        } else {
            Toast.makeText(requireContext(), R.string.camera_capture_failed, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), R.string.camera_permission_denied, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBarLoading.visibility = View.GONE

        woundsModelHelper = WoundsModelHelper(requireContext())

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.currentPhotoUri.observe(viewLifecycleOwner, Observer { uri ->
            uri?.let {
                binding.previewImageView.setImageURI(it)
            }
        })

        viewModel.isAnalyzeButtonEnabled.observe(viewLifecycleOwner, Observer { isEnabled ->
            binding.analyzeButton.isEnabled = isEnabled
        })
    }

    private fun setupListeners() {
        binding.galleryButton.setOnClickListener {
            openGallery()
        }

        binding.cameraButton.setOnClickListener {
            checkCameraPermission()
        }

        binding.analyzeButton.setOnClickListener {
            viewModel.currentPhotoUri.value?.let { uri ->
                binding.progressBarLoading.visibility = View.VISIBLE
                analyzeImage(uri)
            }
        }
    }

    private fun updateSelectedImage(uri: Uri) {
        viewModel.updateCurrentPhotoUri(uri)
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(
                    requireContext(),
                    R.string.camera_permission_rationale,
                    Toast.LENGTH_SHORT
                ).show()
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            val photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            viewModel.updateCurrentPhotoUri(photoUri)
            cameraLauncher.launch(photoUri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.camera_open_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        galleryLauncher.launch(galleryIntent)
    }

    private fun createImageFile(): File {
        val storageDir: File? = requireContext().getExternalFilesDir(null)
        if (storageDir == null || (!storageDir.exists() && !storageDir.mkdirs())) {
            throw IllegalStateException(getString(R.string.image_directory_error))
        }
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
    }

    private fun analyzeImage(uri: Uri) {
        woundsModelHelper.classifyImageAsync(uri) { result ->
            val (label, confidence) = result
            binding.progressBarLoading.visibility = View.GONE
            navigateToResultActivity(uri.toString(), label, confidence)
        }
    }

    private fun navigateToResultActivity(imageUri: String, topCategory: String, confidence: Float) {
        val resultsArray = FloatArray(5) { 0f }.apply {
            val index = when (topCategory) {
                "Abrasions" -> 0
                "Bruises" -> 1
                "Burns" -> 2
                "Cut" -> 3
                "Normal" -> 4
                else -> -1
            }
            if (index != -1) {
                this[index] = confidence
            }
        }

        val intent = Intent(requireContext(), ResultActivity::class.java).apply {
            putExtra("imageUri", imageUri)
            putExtra("results", resultsArray)
        }
        startActivity(intent)
    }
}