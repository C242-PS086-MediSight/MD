package com.example.medisight.ui.page.detailmedicine

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.medisight.R
import com.example.medisight.databinding.MedicineDetailBinding
import java.text.NumberFormat
import java.util.Locale

class ObatDetailActivity : AppCompatActivity() {
    private lateinit var binding: MedicineDetailBinding
    private val viewModel: ObatDetailViewModel by viewModels()

    companion object {
        const val EXTRA_OBAT_ID = "extra_obat_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MedicineDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val obatId = intent.getIntExtra(EXTRA_OBAT_ID, -1)
        if (obatId == -1) {
            finish()
            return
        }

        setupToolbar()
        setupObservers()
        viewModel.getObatDetail(obatId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        window.statusBarColor = getColor(R.color.white)
    }

    private fun setupObservers() {
        viewModel.obat.observe(this) { obat ->
            binding.apply {
                medicineBanner.let {
                    Glide.with(this@ObatDetailActivity)
                        .load(obat.foto_obat)
                        .placeholder(R.drawable.ic_place_holder)
                        .error(R.drawable.ic_place_holder)
                        .into(it)
                }
                medicineTitle.text = obat.nama_obat
                medicinePrice.text = formatRupiah(obat.harga)
                medicineDosage.text = obat.dosis
                medicineDescription.text = obat.deskripsi
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.apply {
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                medicineBanner.visibility = if (isLoading) View.GONE else View.VISIBLE
                medicineTitle.visibility = if (isLoading) View.GONE else View.VISIBLE
                medicinePrice.visibility = if (isLoading) View.GONE else View.VISIBLE
                medicineDosage.visibility = if (isLoading) View.GONE else View.VISIBLE
                medicineDescription.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }

        viewModel.error.observe(this) { error ->
            Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
        }
    }

    private fun formatRupiah(amount: Int): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(amount.toLong()).replace("IDR", "Rp")
    }
}