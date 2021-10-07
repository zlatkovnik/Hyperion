package com.example.protectorsofastrax

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.protectorsofastrax.services.LocationService
import com.example.protectorsofastrax.services.NotificationService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startService(Intent(applicationContext, LocationService::class.java))
        startService(Intent(applicationContext, NotificationService::class.java))

        main_leaderboard_btn.setOnClickListener {
            intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }

        main_profile_btn.setOnClickListener {
            intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user_id", Firebase.auth.uid)
            startActivity(intent)
        }
        main_maps_btn.setOnClickListener {
            intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        main_logout_btn.setOnClickListener {
            stopService(Intent(applicationContext, LocationService::class.java))
            Firebase.auth.signOut()
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        main_myCards_btn.setOnClickListener {
            intent = Intent(this, MyCardsActivity::class.java)
            intent.putExtra("user_id", Firebase.auth.uid)
            startActivity(intent)
        }

        main_add_friends_btn.setOnClickListener {
            intent = Intent(this, AddFriendsActivity::class.java)
            startActivity(intent)
        }

    }
}