package com.example.medisight.ui.page.setting

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.*
import com.example.medisight.databinding.ActivitySettingBinding
import com.example.medisight.helper.PreferencesHelper
import com.example.medisight.worker.WoundCareWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private lateinit var preferencesHelper: PreferencesHelper

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setupNotificationWorker()
        } else {
            binding.switchNotification.isChecked = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesHelper = PreferencesHelper(this)

        setupViews()
        restoreSettings()
    }

    private fun setupViews() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            preferencesHelper.setNotificationEnabled(isChecked)
            if (isChecked) {
                checkNotificationPermission()
            } else {
                cancelNotificationWorker()
            }
        }

        binding.languageText.setOnClickListener {
            openLanguageSettings()
        }

        binding.icNextLanguage.setOnClickListener {
            openLanguageSettings()
        }
    }

    private fun restoreSettings() {
        binding.switchNotification.isChecked = preferencesHelper.isNotificationEnabled()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    setupNotificationWorker()
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            setupNotificationWorker()
        }
    }

    private fun setupNotificationWorker() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val workData = workDataOf(
                WoundCareWorker.KEY_WOUND_TYPE to "general"
            )

            val workRequest = PeriodicWorkRequestBuilder<WoundCareWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setInputData(workData)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(applicationContext)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )

            Log.d(TAG, "Successfully scheduled notification worker")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up notification worker", e)
            binding.switchNotification.isChecked = false
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

    private fun cancelNotificationWorker() {
        WorkManager.getInstance(applicationContext)
            .cancelUniqueWork(WORK_NAME)
        Log.d(TAG, "Cancelled notification worker")
    }

    private fun openLanguageSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_LOCALE_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open settings", e)
            }
        }
    }

    companion object {
        private const val TAG = "SettingActivity"
        private const val WORK_NAME = "woundCareWork"
    }
}