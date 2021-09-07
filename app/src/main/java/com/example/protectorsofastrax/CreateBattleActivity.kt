package com.example.protectorsofastrax

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.protectorsofastrax.data.Card
import com.example.protectorsofastrax.data.Enemy
import com.google.firebase.firestore.FirebaseFirestore

class CreateBattleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_battle)

        FirebaseFirestore.getInstance().collection("enemies").get()
            .addOnSuccessListener {
                val enemies = ArrayList<Enemy>()
                for (document in it) {
                    enemies.add(Enemy(document.id, document.data["name"] as String, document.data["power"] as Long, document.data["picture"] as String))
                }
                drawEnemies(enemies)
            }
    }

    private fun drawEnemies(enemies: ArrayList<Enemy>) {
        val rvContacts = findViewById<View>(R.id.enemies_rcv) as RecyclerView

        val adapter = EnemiesAdapter(enemies)
        // Attach the adapter to the recyclerview to populate items
        rvContacts.adapter = adapter
        // Set layout manager to position the items
        rvContacts.layoutManager = LinearLayoutManager(this@CreateBattleActivity)
    }
}