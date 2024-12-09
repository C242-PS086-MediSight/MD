package com.example.medisight.ui.page.medicine

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medisight.R
import com.example.medisight.data.model.Obat
import com.example.medisight.ui.page.detailmedicine.ObatDetailActivity

class ObatAdapter(private var obatList: List<Obat>) :
    RecyclerView.Adapter<ObatAdapter.ObatViewHolder>() {

    inner class ObatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.medicine_title)
        private val imageView: ImageView = itemView.findViewById(R.id.medicine_image)

        fun bind(obat: Obat) {
            nameTextView.text = obat.nama_obat
            Glide.with(itemView.context)
                .load(obat.foto_obat)
                .placeholder(R.drawable.ic_place_holder)
                .into(imageView)

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ObatDetailActivity::class.java).apply {
                    putExtra(ObatDetailActivity.EXTRA_OBAT_ID, obat.id)
                }
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine, parent, false)
        return ObatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ObatViewHolder, position: Int) {
        holder.bind(obatList[position])
    }

    override fun getItemCount(): Int = obatList.size

    fun updateList(newList: List<Obat>) {
        obatList = newList
        notifyDataSetChanged()
    }
}