package com.example.medisight.ui.page.article

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medisight.data.model.Artikel
import com.example.medisight.databinding.ItemArticleBinding

class ArtikelAdapter : RecyclerView.Adapter<ArtikelAdapter.ArtikelViewHolder>() {
    private var onItemClickCallback: ((Artikel) -> Unit)? = null
    private var artikelList = ArrayList<Artikel>()
    private var filteredList = ArrayList<Artikel>()

    fun setOnItemClickCallback(callback: (Artikel) -> Unit) {
        onItemClickCallback = callback
    }

    fun setArtikel(articles: List<Artikel>) {
        artikelList.clear()
        artikelList.addAll(articles)
        filteredList.clear()
        filteredList.addAll(articles)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtikelViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtikelViewHolder(binding)
    }

    override fun getItemCount() = filteredList.size

    override fun onBindViewHolder(holder: ArtikelViewHolder, position: Int) {
        holder.bind(filteredList[position])
        holder.itemView.setOnClickListener {
            onItemClickCallback?.invoke(filteredList[position])
        }
    }

    fun filter(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(artikelList)
        } else {
            val filtered = artikelList.filter {
                it.title.lowercase().contains(query.lowercase())
            }
            filteredList.addAll(filtered)
        }
        notifyDataSetChanged()
    }

    inner class ArtikelViewHolder(private val binding: ItemArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(artikel: Artikel) {
            with(binding) {
                articleTitle.text = artikel.title
                Glide.with(itemView.context)
                    .load(artikel.foto_article)
                    .into(articleImage)

                val contentPreview = artikel.content.split("\r\n")
                    .take(2)
                    .joinToString("\n")
                articleDescription.text = "$contentPreview..."
            }
        }
    }
}