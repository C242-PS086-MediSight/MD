package com.example.medisight.ui.page.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.recyclerview.widget.GridLayoutManager
import com.example.medisight.R
import com.example.medisight.data.repository.UserRepository
import com.example.medisight.domain.usecase.UserProfileState
import com.example.medisight.factory.HomeViewModelFactory
import com.example.medisight.ui.page.article.ArtikelActivity
import com.example.medisight.ui.page.detailarticle.ArtikelDetailActivity
import com.example.medisight.ui.page.healthservices.FaskesActivity
import com.example.medisight.ui.page.medicalhistory.MedicalHistoryActivity
import com.example.medisight.ui.page.medicine.ObatActivity
import com.example.medisight.ui.page.setting.SettingActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var viewPagerBanner: ViewPager2
    private lateinit var tvUserName: TextView
    private lateinit var btnHealthServices: ImageButton
    private lateinit var btnMedicine: ImageButton
    private lateinit var btnArticle: ImageButton
    private lateinit var btnMedicalResume: ImageButton
    private lateinit var btnSetting: ImageButton
    private lateinit var rvHotArticles: RecyclerView
    private lateinit var rvMedicine: RecyclerView
    private val hotArticleAdapter = HotArticleAdapter()
    private val medicineAdapter = HomeMedicineAdapter()
    private val sliderHandler = Handler(Looper.getMainLooper())
    private val sliderRunnable = Runnable {
        viewPagerBanner.currentItem = if (viewPagerBanner.currentItem + 1 == bannerImages.size) 0
        else viewPagerBanner.currentItem + 1
    }

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            UserRepository(
                auth = FirebaseAuth.getInstance(),
                database = FirebaseDatabase.getInstance(),
                storage = FirebaseStorage.getInstance(),
                contentResolver = requireActivity().contentResolver
            )
        )
    }

    private val bannerImages = listOf(
        R.drawable.banner_image1,
        R.drawable.banner_image2,
        R.drawable.banner_image3
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        initializeViews(view)
        setupViewPager()
        setupButtons()
        setupHotArticles(view)
        setupMedicineRecyclerView(view)
        setupObservers()
        setupMedicineSeeAll(view)
        setupUserProfileObserver()

        return view
    }

    private fun initializeViews(view: View) {
        viewPagerBanner = view.findViewById(R.id.viewPagerBanner)
        tvUserName = view.findViewById(R.id.tvUserName)
        btnHealthServices = view.findViewById(R.id.btnHealthServices)
        btnMedicine = view.findViewById(R.id.btnMedicine)
        btnArticle = view.findViewById(R.id.btnArticle)
        btnMedicalResume = view.findViewById(R.id.btnMedicalResume)
        btnSetting = view.findViewById(R.id.btnSettings)
    }

    private fun setupUserProfileObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userProfile.collect { state ->
                when (state) {
                    is UserProfileState.Success -> {
                        tvUserName.text = state.user.fullName
                    }

                    is UserProfileState.Error -> {
                        tvUserName.text = "User"
                    }

                    is UserProfileState.Loading -> {
                        tvUserName.text = "Loading..."
                    }
                }
            }
        }
    }

    private fun setupButtons() {
        btnHealthServices.setOnClickListener {
            startActivity(Intent(requireContext(), FaskesActivity::class.java))
        }

        btnMedicine.setOnClickListener {
            startActivity(Intent(requireContext(), ObatActivity::class.java))
        }

        btnArticle.setOnClickListener {
            startActivity(Intent(requireContext(), ArtikelActivity::class.java))
        }

        btnMedicalResume.setOnClickListener {
            startActivity(Intent(requireContext(), MedicalHistoryActivity::class.java))
        }

        btnSetting.setOnClickListener {
            startActivity(Intent(requireContext(), SettingActivity::class.java))
        }
    }

    private fun setupHotArticles(view: View) {
        rvHotArticles = view.findViewById(R.id.rvHotArticles)
        rvHotArticles.adapter = hotArticleAdapter
        rvHotArticles.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        hotArticleAdapter.setOnItemClickCallback { artikel ->
            val intent = Intent(requireContext(), ArtikelDetailActivity::class.java)
            intent.putExtra(ArtikelDetailActivity.EXTRA_ARTIKEL_ID, artikel.id)
            startActivity(intent)
        }

        view.findViewById<TextView>(R.id.tvSeeAllArticles).setOnClickListener {
            startActivity(Intent(requireContext(), ArtikelActivity::class.java))
        }
    }

    private fun setupMedicineSeeAll(view: View) {
        view.findViewById<TextView>(R.id.tvSeeAllMedicine).setOnClickListener {
            startActivity(Intent(requireContext(), ObatActivity::class.java))
        }
    }

    private fun setupMedicineRecyclerView(view: View) {
        rvMedicine = view.findViewById(R.id.rvMedicine)
        rvMedicine.adapter = medicineAdapter
        rvMedicine.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun setupObservers() {
        viewModel.hotArticles.observe(viewLifecycleOwner) { articles ->
            hotArticleAdapter.setHotArticle(articles)
        }

        viewModel.medicines.observe(viewLifecycleOwner) { medicines ->
            medicineAdapter.setMedicine(medicines)
        }
    }

    private fun setupViewPager() {
        viewPagerBanner.apply {
            adapter = BannerAdapter(requireContext(), bannerImages)

            setPageTransformer { page, position ->
                when {
                    position < -1 -> page.alpha = 0f
                    position <= 1 -> {
                        page.alpha = 1f
                        val scaleFactor = Math.max(0.85f, 1 - Math.abs(position))
                        page.scaleX = scaleFactor
                        page.scaleY = scaleFactor
                        val vertMargin = page.height * (1 - scaleFactor) / 2
                        val horzMargin = page.width * (1 - scaleFactor) / 2
                        page.translationX = if (position < 0) {
                            horzMargin - vertMargin / 2
                        } else {
                            -horzMargin + vertMargin / 2
                        }
                    }

                    else -> page.alpha = 0f
                }
            }

            val animator = ViewPager2.PageTransformer { _, _ -> }
            setPageTransformer(animator)

            val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
            recyclerViewField.isAccessible = true
            val recyclerView = recyclerViewField.get(this) as RecyclerView

            val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
            touchSlopField.isAccessible = true
            val touchSlop = touchSlopField.get(recyclerView) as Int
            touchSlopField.set(recyclerView, touchSlop * 4)

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    sliderHandler.removeCallbacks(sliderRunnable)
                    sliderHandler.postDelayed(sliderRunnable, 5000)
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 5000)
    }
}