package com.example.protectorsofastrax

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.protectorsofastrax.services.LocationService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startService(Intent(applicationContext, LocationService::class.java))

        main_leaderboard_btn.setOnClickListener{
            intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }

        main_profile_btn.setOnClickListener {
            intent= Intent(this,ProfileActivity::class.java)
            intent.putExtra("user_id", Firebase.auth.uid)
            startActivity(intent)
        }
        main_maps_btn.setOnClickListener {
            intent= Intent(this,MapActivity::class.java)
            startActivity(intent)
        }

        main_logout_btn.setOnClickListener {
            Firebase.auth.signOut()
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        main_myCards_btn.setOnClickListener {
            intent= Intent(this, MyCardsActivity::class.java)
            intent.putExtra("user_id", Firebase.auth.uid)
            startActivity(intent)
        }

//        intent = Intent(this, AddFriendsActivity::class.java)
//        startActivity(intent)

    }
}