package com.example.medisight.ui.page.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medisight.data.model.Artikel
import com.example.medisight.databinding.ItemHotArticleBinding

class HotArticleAdapter : RecyclerView.Adapter<HotArticleAdapter.HotArticleViewHolder>() {
    private var hotArticleList = ArrayList<Artikel>()
    private var onItemClickCallback: ((Artikel) -> Unit)? = null

    fun setOnItemClickCallback(callback: (Artikel) -> Unit) {
        onItemClickCallback = callback
    }

    fun setHotArticle(articles: List<Artikel>) {
        hotArticleList.clear()
        hotArticleList.addAll(articles.filter { it.id in listOf(1, 4, 5) })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotArticleViewHolder {
        val binding =
            ItemHotArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HotArticleViewHolder(binding)
    }

    override fun getItemCount() = hotArticleList.size

    override fun onBindViewHolder(holder: HotArticleViewHolder, position: Int) {
        holder.bind(hotArticleList[position])
        holder.itemView.setOnClickListener {
            onItemClickCallback?.invoke(hotArticleList[position])
        }
    }

    class HotArticleViewHolder(private val binding: ItemHotArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(artikel: Artikel) {
            with(binding) {
                articleTitle.text = artikel.title
                Glide.with(itemView.context)
                    .load(artikel.foto_article)
                    .into(articleImage)
            }
        }
    }
}