package com.example.protectorsofastrax

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.protectorsofastrax.data.BattleLocation
import com.example.protectorsofastrax.data.Card
import com.example.protectorsofastrax.data.Enemy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_create_battle.*
import org.osmdroid.util.GeoPoint

class CreateBattleActivity : AppCompatActivity(), OnEnemyItemClickListner {
    lateinit var userId: String
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

        userId = Firebase.auth.uid!!

        boss_select_back_btn.setOnClickListener {
            finish()
        }
    }

    private fun drawEnemies(enemies: ArrayList<Enemy>) {
        val rvContacts = findViewById<View>(R.id.enemies_rcv) as RecyclerView

        val adapter = EnemiesAdapter(enemies, this)
        // Attach the adapter to the recyclerview to populate items
        rvContacts.adapter = adapter
        // Set layout manager to position the items
        rvContacts.layoutManager = LinearLayoutManager(this@CreateBattleActivity)
    }

    override fun onItemClick(item: Enemy, position: Int) {
        val id=FirebaseAuth.getInstance().uid as String
        val latitude = intent.extras!!["latitude"] as Double
        val longitude = intent.extras!!["longitude"] as Double
        var userCardMap : HashMap<String, String> =HashMap<String, String>()
         val battle=BattleLocation( id,item.id!!, latitude, longitude,null,userCardMap)

        FirebaseDatabase.getInstance().reference.child("battles").child(userId)
            .setValue(battle)
        finish()
    }
}