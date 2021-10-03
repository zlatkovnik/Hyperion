package com.example.protectorsofastrax

import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.protectorsofastrax.data.Card
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import kotlinx.android.synthetic.main.activity_my_cards.*
import kotlinx.android.synthetic.main.card.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await


class MyCardsActivity : AppCompatActivity() {

//    lateinit var contacts: ArrayList<String>

    lateinit var firestore: FirebaseFirestore;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_cards)

        firestore = FirebaseFirestore.getInstance()

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)


        // Initialize contacts


        val user_id = intent.getStringExtra("user_id")
        val cards : ArrayList<Card> = ArrayList<Card>()
        firestore.collection("users").document(user_id!!).get()
            .addOnSuccessListener { ds ->
                var cardIds = ds["cards"] as ArrayList<String>
                var requests = cardIds.map {  cid ->
                    firestore.collection("cards").document(cid).get().asDeferred()
                }
                GlobalScope.launch {
                    val results: ArrayList<DocumentSnapshot> = requests.awaitAll() as ArrayList<DocumentSnapshot>
                    results.forEach {
                        var name = it["name"] as String
                        var picture = it["picture"] as String
                        var race = it["race"] as String
                        var clas = it["clas"] as String
                        var power = it["power"] as Long
                        cards.add(Card(it.id, picture, name, clas, power, race))
                    }
                    this@MyCardsActivity.runOnUiThread(Runnable {
                        drawCards(cards)
                    })
//                    drawCards(cards)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
            val select=intent.getBooleanExtra("select", false)
            var  nesto = "0"
            if(select)
            {
                card_hero_img.onBindViewHolder{

                }
            }

    }

    fun drawCards(cards: ArrayList<Card>){
        val rvContacts = findViewById<View>(R.id.rcview) as RecyclerView

        val adapter = CardsAdapter(cards)
        // Attach the adapter to the recyclerview to populate items
        rvContacts.adapter = adapter
        // Set layout manager to position the items
        rvContacts.layoutManager = LinearLayoutManager(this@MyCardsActivity)
    }
}
