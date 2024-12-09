package com.example.medisight.ui.page.medicalhistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medisight.R
import com.example.medisight.data.model.ScanHistory
import com.example.medisight.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onDeleteClick: (ScanHistory) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private var histories = listOf<ScanHistory>()

    fun submitList(newHistories: List<ScanHistory>) {
        histories = newHistories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(histories[position])
    }


    override fun getItemCount(): Int = histories.size

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(history: ScanHistory) {
            binding.apply {
                historyTitle.text = history.scanResult
                historySaved.text = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                    .format(Date(history.timestamp))

                Glide.with(historyImage)
                    .load(history.imageUrl)
                    .placeholder(R.drawable.ic_place_holder)
                    .into(historyImage)

                trash.setOnClickListener {
                    onDeleteClick(history)
                }
            }
        }
    }
}