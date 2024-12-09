package com.example.medisight.ui.page.bottomnavigation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.medisight.R
import com.example.medisight.ui.page.chatbot.ChatBotActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigation()
        setupFabClickListener()
    }

    private fun setupNavigation() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)
    }

    private fun setupFabClickListener() {
        val fabChatBot: FloatingActionButton = findViewById(R.id.fabChatBot)
        fabChatBot.drawable?.setTint(ContextCompat.getColor(this, android.R.color.white))
        fabChatBot.setOnClickListener {
            val intent = Intent(this, ChatBotActivity::class.java)
            startActivity(intent)
        }
    }
}