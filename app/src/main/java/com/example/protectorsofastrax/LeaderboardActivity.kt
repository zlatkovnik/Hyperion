package com.example.protectorsofastrax

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.protectorsofastrax.data.LeaderboardUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User

class LeaderboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        FirebaseFirestore.getInstance().collection("users").get()
            .addOnSuccessListener { qs ->

                val users: ArrayList<LeaderboardUser> = ArrayList();
                qs.forEach { u ->
                    val name = u["name"] as String
                    val battlesWon = u["battlesWon"] as Long
                    users.add(LeaderboardUser(name, battlesWon))
                }
                users.sortByDescending { it.battlesWon }
//                users.forEachIndexed { index, leaderboardUser ->
//                    leaderboardUser.rank = index + 1
//                }
                val rvLeaderboard = findViewById<View>(R.id.leaderboard_rv) as RecyclerView

                val adapter = LeaderboardAdapter(users)
                // Attach the adapter to the recyclerview to populate items
                rvLeaderboard.adapter = adapter
                // Set layout manager to position the items
                rvLeaderboard.layoutManager = LinearLayoutManager(this@LeaderboardActivity)
            }
    }
}