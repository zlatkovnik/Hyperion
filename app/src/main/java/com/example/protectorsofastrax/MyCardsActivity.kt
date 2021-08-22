package com.example.protectorsofastrax

import android.os.Bundle
import android.os.StrictMode
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MyCardsActivity : AppCompatActivity() {

//    lateinit var contacts: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_cards)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val rvContacts = findViewById<View>(R.id.rcview) as RecyclerView
        // Initialize contacts

        val num = arrayOf<String>("mrkela", "bosko", "smrk")  //explicit type declaration
        val heroes = arrayOf<String>("https://www.elfak.ni.ac.rs/images/katedre/racunarstvo/emina-milovanovic.jpg"
        ,"https://i.ytimg.com/vi/9t6WXUSM-xQ/maxresdefault.jpg")



        // Create adapter passing in the sample user data
        val adapter = CardsAdapter(heroes, num)
        // Attach the adapter to the recyclerview to populate items
        rvContacts.adapter = adapter
        // Set layout manager to position the items
        rvContacts.layoutManager = LinearLayoutManager(this)

    }
}
