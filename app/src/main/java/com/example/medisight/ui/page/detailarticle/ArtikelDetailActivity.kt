package com.example.medisight.ui.page.detailarticle

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.medisight.databinding.ArticleDetailBinding

class ArtikelDetailActivity : AppCompatActivity() {
    private lateinit var binding: ArticleDetailBinding
    private lateinit var viewModel: ArtikelDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ArtikelDetailViewModel::class.java]

        val artikelId = intent.getIntExtra(EXTRA_ARTIKEL_ID, -1)

        setupToolbar()
        observeData()

        if (artikelId != -1) {
            viewModel.fetchArtikelDetail(artikelId)
        } else {
            finish()
        }
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun observeData() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.nestedScrollView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.artikel.observe(this) { artikel ->
            with(binding) {
                articleTitle.text = artikel.title
                articleWriter.text = artikel.author
                articleDate.text = artikel.created_at
                articleDescription.text = artikel.content

                if (!artikel.foto_article.isNullOrEmpty()) {
                    Glide.with(this@ArtikelDetailActivity)
                        .load(artikel.foto_article)
                        .into(articleBanner)
                }
            }
        }

        viewModel.error.observe(this) { error ->
            binding.progressBar.visibility = View.GONE
            binding.nestedScrollView.visibility = View.VISIBLE
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_ARTIKEL_ID = "extra_artikel_id"
    }
}