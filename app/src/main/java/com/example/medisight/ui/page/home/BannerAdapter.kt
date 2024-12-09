package com.example.medisight.ui.page.home

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class BannerAdapter(private val context: Context, private val bannerImages: List<Int>) :
    RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val imageView = ImageView(context)
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        return BannerViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.imageView.setImageResource(bannerImages[position])
    }

    override fun getItemCount(): Int {
        return bannerImages.size
    }

    class BannerViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
}