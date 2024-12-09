package com.example.medisight.ui.page.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medisight.data.model.Obat
import com.example.medisight.databinding.ItemMedicineBinding
import com.example.medisight.ui.page.detailmedicine.ObatDetailActivity

class HomeMedicineAdapter : RecyclerView.Adapter<HomeMedicineAdapter.HomeMedicineViewHolder>() {
    private var medicineList = ArrayList<Obat>()

    fun setMedicine(medicines: List<Obat>) {
        medicineList.clear()
        val filteredList = medicines.filter { it.id in listOf(2, 4, 6, 10) }
        medicineList.addAll(filteredList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeMedicineViewHolder {
        val binding =
            ItemMedicineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeMedicineViewHolder(binding)
    }

    override fun getItemCount() = medicineList.size

    override fun onBindViewHolder(holder: HomeMedicineViewHolder, position: Int) {
        val obat = medicineList[position]
        holder.bind(obat)
    }

    inner class HomeMedicineViewHolder(private val binding: ItemMedicineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(obat: Obat) {
            binding.apply {
                medicineTitle.text = obat.nama_obat
                Glide.with(itemView.context)
                    .load(obat.foto_obat)
                    .into(medicineImage)

                root.setOnClickListener {
                    val intent = Intent(itemView.context, ObatDetailActivity::class.java)
                    intent.putExtra(ObatDetailActivity.EXTRA_OBAT_ID, obat.id)
                    itemView.context.startActivity(intent)
                }
            }
        }
    }
}