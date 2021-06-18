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
        val heroes = arrayOf<String>("https://www.elfak.ni.ac.rs/images/katedre/racunarstvo/emina-milovanovic.jpg",
        "https://scontent.fbeg5-1.fna.fbcdn.net/v/t1.18169-9/558876_407540449318561_1163836385_n.jpg?_nc_cat=110&ccb=1-3&_nc_sid=973b4a&_nc_ohc=71uIPqvIMAUAX_rfqEW&_nc_ht=scontent.fbeg5-1.fna&oh=c27d1ec38842f0b21da6ca9e1b3fdfb1&oe=60D1DFC7"
            ,"https://scontent.fbeg5-1.fna.fbcdn.net/v/t1.18169-9/16998016_1244279075620076_6412031504229812139_n.png?_nc_cat=108&ccb=1-3&_nc_sid=973b4a&_nc_ohc=n7xlkZf-PuQAX-BW0Jj&_nc_ht=scontent.fbeg5-1.fna&oh=12f560b587edd45575e3cebe391dead2&oe=60D0DB46"
        ,"https://i.ytimg.com/vi/9t6WXUSM-xQ/maxresdefault.jpg")



        // Create adapter passing in the sample user data
        val adapter = CardsAdapter(heroes)
        // Attach the adapter to the recyclerview to populate items
        rvContacts.adapter = adapter
        // Set layout manager to position the items
        rvContacts.layoutManager = LinearLayoutManager(this)

    }
}
