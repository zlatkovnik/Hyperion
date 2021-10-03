package com.example.protectorsofastrax

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.protectorsofastrax.data.Card
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_battle.*


class BattleActivity : AppCompatActivity() {

    var ENEMIES_CHILD = "enemies/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.protectorsofastrax.R.layout.activity_battle)

        val enemyId =intent.getStringExtra("enemyID")as String
        val enemies: ArrayList<Card> = ArrayList<Card>()

        fun drawCards(cards: ArrayList<Card>){
            val rvContacts = findViewById<View>(com.example.protectorsofastrax.R.id.battle_cards_rw) as RecyclerView

            val adapter = CardsAdapter(cards,false)
            // Attach the adapter to the recyclerview to populate items
            rvContacts.adapter = adapter
            // Set layout manager to position the items
//            rvContacts.layoutManager = LinearLayoutManager(this@BattleActivity,LinearLayoutManager.HORIZONTAL,false)
            rvContacts.setLayoutManager(object : LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false) {
                override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                    // force height of viewHolder here, this will override layout_height from xml
                    lp.height = (height / 1).toInt()
                    lp.width=(width/1).toInt()
                    return true
                }
            })
        }
//        battle_cards_rw.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
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
        FirebaseFirestore.getInstance().collection("cards").get()
            .addOnSuccessListener { it->
                it.forEach {
                    var name = it["name"] as String
                    var picture = it["picture"] as String
                    var race = it["race"] as String
                    var clas = it["clas"] as String
                    var power = it["power"] as Long
                    enemies.add(Card(it.id, picture, name, clas, power, race))
                }
                this@BattleActivity.runOnUiThread(Runnable {
                    drawCards(enemies)})
            }


        battle_addcard_btn.setOnClickListener {
            val i= Intent(this@BattleActivity,MyCardsActivity::class.java)
            i.putExtra("user_id",Firebase.auth.uid)
            i.putExtra("select",true)
            startActivityForResult(i,200)
        }
    }
}