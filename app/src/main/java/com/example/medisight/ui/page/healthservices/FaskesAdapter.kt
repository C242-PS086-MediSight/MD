package com.example.medisight.ui.page.healthservices

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medisight.R
import com.example.medisight.data.model.Faskes
import com.example.medisight.databinding.ItemHealthServiceBinding

class FaskesAdapter : ListAdapter<Faskes, FaskesAdapter.FaskesViewHolder>(FaskesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaskesViewHolder {
        val binding =
            ItemHealthServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FaskesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FaskesViewHolder, position: Int) {
        val faskes = getItem(position)
        holder.bind(faskes)
    }

    class FaskesViewHolder(private val binding: ItemHealthServiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(faskes: Faskes) {
            binding.healthServiceTitle.text = faskes.nama_faskes
            binding.healthServiceAddress.text = faskes.link_maps

            Glide.with(binding.root)
                .load(faskes.foto_url)
                .placeholder(R.drawable.ic_place_holder)
                .into(binding.healthServiceImage)

            binding.healthServiceAddress.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(faskes.link_maps))
                itemView.context.startActivity(intent)
            }
        }
    }

    class FaskesDiffCallback : DiffUtil.ItemCallback<Faskes>() {
        override fun areItemsTheSame(oldItem: Faskes, newItem: Faskes): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Faskes, newItem: Faskes): Boolean {
            return oldItem == newItem
        }
    }
}