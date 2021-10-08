package com.example.protectorsofastrax

import android.app.*
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.protectorsofastrax.data.BattleLocation
import com.example.protectorsofastrax.data.Card
import com.example.protectorsofastrax.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.okhttp.*
import kotlinx.android.synthetic.main.activity_battle.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.asDeferred


class BattleActivity : AppCompatActivity() {

    var ENEMIES_CHILD = "enemies/"
    lateinit var battleId: String
    var listener: ValueEventListener? = null
    var lis: ValueEventListener? = null

    var cachedOdds: Double = 0.0;
    var cachedBattleLocation: BattleLocation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.protectorsofastrax.R.layout.activity_battle)

        battle_back_btn.setOnClickListener {
            finish()
        }

        val enemyId = intent.getStringExtra("enemyID")!!
        battleId = intent.getStringExtra("battle_id")!!
//        val cardsInBattle: ArrayList<Card> = ArrayList<Card>()
//        var cardsIdInBattle:ArrayList<String> = ArrayList<String>()

        fun drawCards(cards: ArrayList<Card>) {
            val rvContacts =
                findViewById<View>(com.example.protectorsofastrax.R.id.battle_cards_rw) as RecyclerView

            val adapter = CardsAdapter(cards, false)
            // Attach the adapter to the recyclerview to populate items
            rvContacts.adapter = adapter
            // Set layout manager to position the items
//            rvContacts.layoutManager = LinearLayoutManager(this@BattleActivity,LinearLayoutManager.HORIZONTAL,false)
            rvContacts.setLayoutManager(object :
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) {
                override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                    // force height of viewHolder here, this will override layout_height from xml
                    lp.height = (height / 1).toInt()
                    lp.width = (width / 1).toInt()
                    return true
                }
            })
        }
//        battle_cards_rw.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        FirebaseFirestore.getInstance().collection("enemies").document(enemyId).get()
            .addOnSuccessListener { it ->
                battle_enemyname_txt.text = it.getString("name")
                var power = it.getLong("power")
                battle_power_txt.text = power.toString()
                var picture = it.getString("picture")
                FirebaseStorage.getInstance().reference
                    .child(ENEMIES_CHILD + picture)
                    .downloadUrl
                    .addOnSuccessListener {
                        Glide.with(this).load(it.toString()).into(battle_enemy_img)
                    }
            }
//

        lis = FirebaseDatabase.getInstance().reference.child("battles").child(battleId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val battle = snapshot.value as HashMap<String, Any>
                    var userCard = battle["userCardMap"] as HashMap<String, String>?
                    if (userCard == null) {
                        battle_win_chance_txt.text = ""
                        cachedBattleLocation = BattleLocation(
                            battleId,
                            enemyId,
                            battle["latitude"] as Double,
                            battle["longitude"] as Double,
                            null,
                            userCard
                        )
                    } else {
                        cachedBattleLocation = BattleLocation(
                            battleId,
                            enemyId,
                            battle["latitude"] as Double,
                            battle["longitude"] as Double,
                            null,
                            userCard
                        )

                        val cardsIdInBattle = ArrayList<String>()
                        val cardsInBattle = ArrayList<Card>()
                        if (userCard != null) {
                            userCard.forEach { it ->
                                cardsIdInBattle.add(it.value)
                            }
                            var requests = cardsIdInBattle.map { cid ->
                                FirebaseFirestore.getInstance().collection("cards").document(cid)
                                    .get()
                                    .asDeferred()
                            }
                            GlobalScope.launch {
                                val results: ArrayList<DocumentSnapshot> =
                                    requests.awaitAll() as ArrayList<DocumentSnapshot>
                                results.forEach {
                                    var name = it["name"] as String
                                    var picture = it["picture"] as String
                                    var race = it["race"] as String
                                    var clas = it["clas"] as String
                                    var power = it["power"] as Long
                                    cardsInBattle.add(Card(it.id, picture, name, clas, power, race))
                                }
                                FirebaseFirestore.getInstance().collection("enemies").document(enemyId)
                                    .get()
                                    .addOnSuccessListener { enemySnapshot ->
                                        val basePower =
                                            if (cardsInBattle.size > 0) cardsInBattle.map { card -> card.power }
                                                .reduce { acc, cardPower -> acc + cardPower }
                                            else 0
                                        var totalPower: Long = basePower
                                        val enemyPower = enemySnapshot.data?.get("power") as Long
                                        cardsInBattle.forEach { card ->
                                            val filteredCards =
                                                cardsInBattle.filter { c -> c.name == card.name }
                                            if (card.race == "Human") {
                                                // Orci smanjuju ljudima
                                                val foundOrc =
                                                    filteredCards.find { c -> c.race == "Orc" }
                                                if (foundOrc != null) {
                                                    totalPower -= (card.power * 0.2).toLong()
                                                }
                                                // Ljudi povecavaju druge ljude
                                                val foundHuman =
                                                    filteredCards.find { c -> c.race == card.race }
                                                if (foundHuman != null) {
                                                    totalPower += (card.power * 0.15).toLong()
                                                }
                                            }
                                            // Zmajevi ne vole druge zmajeve
                                            if (card.race == "Dragon") {
                                                val foundDragon =
                                                    filteredCards.find { c -> c.race == card.race }
                                                if (foundDragon != null) {
                                                    totalPower -= (card.power * 0.3).toLong()
                                                }
                                            }
                                            // Ako warrior ima healeri
                                            if (card.clas == "Warrior") {
                                                val foundPriest =
                                                    filteredCards.find { c -> c.clas == "Priest" || c.clas == "Paladin" }
                                                if (foundPriest != null) {
                                                    totalPower += (card.power * 0.275).toLong()
                                                }
                                            }
                                            // Ako mage ima warloci
                                            if (card.clas == "Mage") {
                                                val foundWarlock =
                                                    filteredCards.find { c -> c.clas == "Warlock" }
                                                if (foundWarlock != null) {
                                                    totalPower -= (card.power * 0.2).toLong()
                                                }
                                            }
                                            // Ako warlock ima healeri
                                            if (card.clas == "Warlock") {
                                                val foundPriest =
                                                    filteredCards.find { c -> c.clas == "Priest" }
                                                if (foundPriest != null) {
                                                    totalPower += (card.power * 0.15).toLong()
                                                }
                                            }
                                        }
                                        totalPower -= (basePower * cardsInBattle.size * cardsInBattle.size * 0.014).toLong()
                                        cachedOdds = totalPower.toDouble() / enemyPower.toDouble()
                                        if (cachedOdds > 90.0) cachedOdds = 90.0
                                        this@BattleActivity.runOnUiThread(Runnable {
                                            battle_win_chance_txt.text =
                                                (cachedOdds * 100).toInt().toString() + "%"
                                        })
                                    }
                                val thread = Thread {
                                    try {
                                        this@BattleActivity.runOnUiThread(Runnable {
                                            drawCards(cardsInBattle)
                                        })
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                thread.start()

                            }
                        }
                    }

                }


                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


        val userId = Firebase.auth.uid as String
        if (battleId != userId) {
            battle_battle_btn.visibility = View.INVISIBLE
            battle_battle_btn.isEnabled = false
        } else {
            battle_addcard_btn.visibility = View.INVISIBLE
            battle_addcard_btn.isEnabled = false
            battle_battle_btn.setOnClickListener {
                startBattle()
            }
        }

        battle_back_btn.setOnClickListener {
            finish()
        }

        battle_addcard_btn.setOnClickListener {
            val i = Intent(this@BattleActivity, MyCardsActivity::class.java)
            i.putExtra("user_id", Firebase.auth.uid)
            i.putExtra("select", true)
            startActivityForResult(i, 200)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200) {
            if (resultCode == Activity.RESULT_OK) {
                val cardId = data!!.getStringExtra("selected") as String
                listener = FirebaseDatabase.getInstance().reference.child("battles").child(battleId)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val battle = snapshot.value as HashMap<String, Any>
                            val latitude = battle["latitude"] as Double
                            val longitude = battle["longitude"] as Double
                            val enemyId = battle["enemyId"] as String
                            var userCard = battle["userCardMap"] as HashMap<String, String>?

                            if (userCard == null) {
                                userCard = HashMap()
                            }
                            if (userCard.get(Firebase.auth.uid.toString()) == null) {
                                userCard.put(Firebase.auth.uid.toString(), cardId)
                            } else {
                                Toast.makeText(
                                    this@BattleActivity,
                                    "You can't add more than one card!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            val battleLocation = BattleLocation(
                                battleId,
                                enemyId,
                                latitude,
                                longitude,
                                null,
                                userCard
                            )
                            FirebaseDatabase.getInstance().reference.child("battles")
                                .child(battleId).setValue(battleLocation)
                            FirebaseDatabase.getInstance().reference.removeEventListener(listener!!)
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
            if (resultCode == RESULT_CANCELED) {
            }
        }
    }

    private fun startBattle() {
        val rnd = Math.random()
        if (rnd < cachedOdds) {
            cachedBattleLocation?.userCardMap?.forEach { b ->
                FirebaseFirestore.getInstance().collection("users").document(b.key).get()
                    .addOnSuccessListener { snapshot ->
                        var user = snapshot.toObject(User::class.java)
                        user!!.experience += (50.0 * Math.random()).toLong()
                        FirebaseFirestore.getInstance().collection("cards").get()
                            .addOnSuccessListener { cardsSnapshot ->
                                val allCardIds = cardsSnapshot.map { ss -> ss.id }
                                val randomCardId = allCardIds.random()
                                val newCards = arrayListOf<String>()
                                newCards.addAll(user!!.cards)
                                newCards.add(randomCardId)
                                user!!.cards = newCards
                                FirebaseFirestore.getInstance().collection("users")
                                    .document(snapshot.id).set(user)
                                    .addOnSuccessListener {
                                        FirebaseFirestore.getInstance().collection("fcm")
                                            .document(snapshot.id).get()
                                            .addOnSuccessListener { fcmSnapshot ->
                                                val cardSnapshot =
                                                    cardsSnapshot.documents.find { s -> s.id.toString() == randomCardId }
                                                val card = cardSnapshot?.toObject(Card::class.java)
                                                val message = object {
                                                    val title = "Battle Won!"
                                                    val text = card?.name + " is now available for battle."
                                                }
                                                FirebaseDatabase.getInstance().reference.child("notification").child(snapshot.id)
                                                    .setValue(message)
                                                FirebaseDatabase.getInstance().reference.child("battles").child(battleId).removeValue()
                                                finish()
                                            }
                                    }
                            }
                    }
            }
        } else {
            val message = object {
                val title = "You lost"
                val text = "Better luck next time."
            }
            cachedBattleLocation?.userCardMap?.forEach { b ->
                FirebaseDatabase.getInstance().reference.child("notification").child(b.key)
                    .setValue(message)
                FirebaseDatabase.getInstance().reference.child("battles").child(battleId).removeValue()
                finish()
            }

        }
    }
}