package com.example.medisight.ui.page.medicine

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medisight.R
import com.google.android.material.appbar.MaterialToolbar

class ObatActivity : AppCompatActivity() {

    private lateinit var obatViewModel: ObatViewModel
    private lateinit var obatAdapter: ObatAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.medicine)

        recyclerView = findViewById(R.id.obatRecyclerView)
        searchView = findViewById(R.id.search_view)
        progressBar = findViewById(R.id.progressBar)

        recyclerView.layoutManager = GridLayoutManager(this, 2)

        obatViewModel = ViewModelProvider(this).get(ObatViewModel::class.java)
        obatAdapter = ObatAdapter(emptyList())
        recyclerView.adapter = obatAdapter

        findViewById<MaterialToolbar>(R.id.topAppBar).setNavigationOnClickListener {
            onBackPressed()
        }

        obatViewModel.obatList.observe(this) { obatList ->
            obatAdapter.updateList(obatList)
        }

        obatViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { obatViewModel.searchObat(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { obatViewModel.searchObat(it) }
                return true
            }
        })

        obatViewModel.fetchObatList()
    }
}