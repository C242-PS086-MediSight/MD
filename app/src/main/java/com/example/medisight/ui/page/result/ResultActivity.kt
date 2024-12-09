package com.example.medisight.ui.page.result

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.medisight.R
import com.example.medisight.data.model.ClassificationLabel
import com.example.medisight.data.model.Wound
import com.example.medisight.data.repository.MedicalHistoryRepository
import com.example.medisight.databinding.ActivityResultBinding
import com.example.medisight.helper.NotificationHelper
import com.example.medisight.helper.PreferencesHelper
import com.example.medisight.ui.page.medicine.ObatAdapter
import com.example.medisight.ui.page.medicalhistory.MedicalHistoryActivity
import com.example.medisight.worker.WoundCareWorker
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val viewModel: ResultViewModel by viewModels()
    private lateinit var obatAdapter: ObatAdapter

    companion object {
        private const val TAG = "ResultActivity"
    }

    private val labels = listOf(
        ClassificationLabel(
            "Abrasions",
            "Abrasions are wounds that affect only the top layer of the skin."
        ),
        ClassificationLabel(
            "Bruises",
            "Bruises are caused by blunt force trauma leading to blood vessels breaking under the skin."
        ),
        ClassificationLabel(
            "Burns",
            "Burns are injuries to the skin caused by heat, chemicals, or radiation."
        ),
        ClassificationLabel("Cut", "Cuts are open wounds that are caused by sharp objects."),
        ClassificationLabel("Normal", "No injury detected, skin appears healthy.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSystemBarInsets()
        setupRecyclerView()
        setupObservers()
        setupButtonListeners()
        handleIntent()
    }

    private fun setupRecyclerView() {
        obatAdapter = ObatAdapter(emptyList())
        binding.medicineRecyclerView.apply {
            adapter = obatAdapter
            layoutManager = GridLayoutManager(this@ResultActivity, 2)
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
                    outRect.left = spacing
                    outRect.right = spacing
                    outRect.bottom = spacing
                    if (parent.getChildAdapterPosition(view) < 2) {
                        outRect.top = spacing
                    }
                }
            })
        }
    }

    private fun setupObservers() {
        viewModel.apply {
            imageUri.observe(this@ResultActivity) { uri ->
                try {
                    binding.resultImageView.setImageURI(uri)
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting image", e)
                    binding.resultImageView.setImageResource(R.drawable.ic_place_holder)
                }
            }

            classificationResult.observe(this@ResultActivity) { result ->
                binding.resultText.text = String.format(
                    Locale.getDefault(),
                    getString(R.string.classification_result),
                    result.label.label,
                    result.confidence * 100f
                )
            }

            medicineList.observe(this@ResultActivity) { medicines ->
                Log.d(TAG, "Received medicines: ${medicines.map { it.id }}")
                obatAdapter.updateList(medicines)
                binding.apply {
                    medicineTitle.visibility = if (medicines.isEmpty()) View.GONE else View.VISIBLE
                    medicineRecyclerView.visibility =
                        if (medicines.isEmpty()) View.GONE else View.VISIBLE
                }
            }

            woundHandlingInfo.observe(this@ResultActivity) { woundInfo ->
                woundInfo?.let {
                    updateWoundInformation(it)
                } ?: showNormalSkinInformation()
            }

            isLoading.observe(this@ResultActivity) { isLoading ->
                binding.progressBarLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupSystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupButtonListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.saveButton.setOnClickListener {
            saveImage()
        }
    }

    private fun handleIntent() {
        intent.getStringExtra("imageUri")?.let {
            viewModel.setImageUri(Uri.parse(it))
        }
        viewModel.setClassificationResult(intent.getFloatArrayExtra("results"), labels)
    }

    private fun updateWoundInformation(woundInfo: Wound) {
        binding.apply {
            handlingTipsDescription.text = woundInfo.langkah_penanganan
            moreTipsDescription.text = woundInfo.tips_tambahan
        }
    }

    private fun showNormalSkinInformation() {
        binding.apply {
            handlingTipsDescription.text = getString(R.string.no_handling_required)
            moreTipsDescription.text = getString(R.string.normal_skin_tips)
            medicineTitle.visibility = View.GONE
            medicineRecyclerView.visibility = View.GONE
        }
    }

    private fun saveImage() {
        viewModel.imageUri.value?.let { uri ->
            try {
                binding.progressBarLoading.visibility = View.VISIBLE
                binding.saveButton.isEnabled = false

                val repository = MedicalHistoryRepository()
                val woundType = viewModel.classificationResult.value?.label?.label ?: "Unknown"

                repository.saveHistory(
                    imageUri = uri,
                    scanResult = woundType,
                    confidence = viewModel.classificationResult.value?.confidence ?: 0f
                ) { success ->
                    runOnUiThread {
                        binding.progressBarLoading.visibility = View.GONE
                        binding.saveButton.isEnabled = true

                        if (success) {
                            Toast.makeText(
                                this,
                                getString(R.string.save_success),
                                Toast.LENGTH_SHORT
                            ).show()

                            if (woundType.lowercase() != "normal") {
                                setupNotifications(woundType)
                            }

                            startActivity(Intent(this, MedicalHistoryActivity::class.java))
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.save_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving to history", e)
                binding.progressBarLoading.visibility = View.GONE
                binding.saveButton.isEnabled = true
                Toast.makeText(this, getString(R.string.save_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupNotifications(woundType: String) {
        try {
            val preferencesHelper = PreferencesHelper(this)
            if (!preferencesHelper.isNotificationEnabled()) {
                return
            }

            NotificationHelper(this).showImmediateNotification(woundType)

            val workData = workDataOf(
                WoundCareWorker.KEY_WOUND_TYPE to woundType
            )

            val workRequest = PeriodicWorkRequestBuilder<WoundCareWorker>(
                1, TimeUnit.DAYS
            )
                .setInputData(workData)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(applicationContext)
                .enqueueUniquePeriodicWork(
                    "woundCareWork_${System.currentTimeMillis()}",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )

            Log.d(TAG, "Successfully setup notifications for wound type: $woundType")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up notifications", e)
        }
    }

    private fun calculateInitialDelay(): Long {
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= currentTime) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return calendar.timeInMillis - currentTime
    }
}