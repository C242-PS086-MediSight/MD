package com.example.medisight.ui.page.healthservices

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medisight.R
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import com.example.medisight.ui.page.maps.MapsActivity
import com.google.android.material.appbar.MaterialToolbar

class FaskesActivity : AppCompatActivity() {

    private lateinit var faskesViewModel: FaskesViewModel
    private lateinit var faskesAdapter: FaskesAdapter
    private lateinit var btnLocation: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var searchView: SearchView
    private lateinit var topAppBar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.health_service)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewFaskes)
        progressBar = findViewById(R.id.progressBar)
        searchView = findViewById(R.id.search_view)
        topAppBar = findViewById(R.id.topAppBar)
        btnLocation = findViewById(R.id.locationButton)

        topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }

        btnLocation.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        faskesAdapter = FaskesAdapter()
        recyclerView.adapter = faskesAdapter

        faskesViewModel = ViewModelProvider(this).get(FaskesViewModel::class.java)

        faskesViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        faskesViewModel.faskesList.observe(this) { faskesList ->
            faskesAdapter.submitList(faskesList)
        }

        faskesViewModel.getFaskesList()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { faskesViewModel.searchFaskes(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { faskesViewModel.searchFaskes(it) }
                return true
            }
        })
    }
}