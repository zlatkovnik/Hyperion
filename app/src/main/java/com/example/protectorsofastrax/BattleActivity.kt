package com.example.protectorsofastrax

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_battle.*

class BattleActivity : AppCompatActivity() {

    var ENEMIES_CHILD = "enemies/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle)

        val enemyId =intent.getStringExtra("enemyID")as String

        val docRef = FirebaseFirestore.getInstance().collection("enemies").document(enemyId)
        docRef.get().addOnSuccessListener { it ->
            battle_enemyname_txt.text=it.getString("name")
            var power=it.getLong("power")
            battle_power_txt.text= power.toString()
            var picture=it.getString("picture")
            FirebaseStorage.getInstance().reference
                .child(ENEMIES_CHILD+picture)
                .downloadUrl
                .addOnSuccessListener {
                    Glide.with(this).load(it.toString()).into(battle_enemy_img)
                }
        }
    }
}