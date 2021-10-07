package com.example.protectorsofastrax

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.protectorsofastrax.services.LocationService
import com.example.protectorsofastrax.services.NotificationService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.Constants
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startService(Intent(applicationContext, LocationService::class.java))

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(Constants.TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            if(Firebase.auth.uid != null){
                data class UserToken(val uid: String, val token: String)
                FirebaseFirestore.getInstance().collection("fcm").document(Firebase.auth.uid!!).set(UserToken(Firebase.auth.uid!!, token!!))
            }
        })

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