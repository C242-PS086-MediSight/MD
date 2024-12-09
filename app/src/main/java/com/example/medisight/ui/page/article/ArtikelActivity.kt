package com.example.medisight.ui.page.article

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medisight.databinding.ArticleBinding
import com.example.medisight.ui.page.detailarticle.ArtikelDetailActivity

class ArtikelActivity : AppCompatActivity() {
    private lateinit var binding: ArticleBinding
    private val viewModel: ArtikelViewModel by viewModels()
    private val adapter = ArtikelAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupSearchView()
        setupToolbar()

        viewModel.fetchArtikels()
    }

    private fun setupRecyclerView() {
        binding.recyclerView2.apply {
            layoutManager = LinearLayoutManager(this@ArtikelActivity)
            adapter = this@ArtikelActivity.adapter
        }

        adapter.setOnItemClickCallback { artikel ->
            val intent = Intent(this, ArtikelDetailActivity::class.java)
            intent.putExtra(ArtikelDetailActivity.EXTRA_ARTIKEL_ID, artikel.id)
            startActivity(intent)
        }
    }

    private fun setupObservers() {
        viewModel.artikelList.observe(this) { articles ->
            adapter.setArtikel(articles)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { adapter.filter(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { adapter.filter(it) }
                return true
            }
        })
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}