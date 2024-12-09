package com.example.medisight.ui.page.medicalhistory

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medisight.R
import kotlinx.coroutines.launch
import android.app.AlertDialog
import android.view.View
import com.example.medisight.data.model.ScanHistory
import com.example.medisight.databinding.ActivityHistoryBinding
import com.google.android.material.button.MaterialButton

class MedicalHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private val viewModel: MedicalHistoryViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupButtonListeners()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            onDeleteClick = { history ->
                showDeleteConfirmationDialog(history)
            }
        )

        binding.rvItemHistory.apply {
            layoutManager = LinearLayoutManager(this@MedicalHistoryActivity)
            adapter = this@MedicalHistoryActivity.adapter
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.histories.collect { histories ->
                adapter.submitList(histories)
                updateEmptyState(histories.isEmpty())
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.rvItemHistory.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvItemHistory.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        }
    }

    private fun setupButtonListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showDeleteConfirmationDialog(scanHistory: ScanHistory) {
        val dialogView = layoutInflater.inflate(R.layout.custom_delete_history_dialog, null)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)
        val btnDelete = dialogView.findViewById<MaterialButton>(R.id.btnDelete)

        val alertDialog = dialogBuilder.create()

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnDelete.setOnClickListener {
            viewModel.deleteHistory(scanHistory.id)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}