package com.example.protectorsofastrax

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MyCardsActivity : AppCompatActivity() {

//    lateinit var contacts: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_cards)

        val rvContacts = findViewById<View>(R.id.rcview) as RecyclerView
        // Initialize contacts

        val num = arrayOf<String>("mrkela", "bosko", "smrk")  //explicit type declaration
        val heros = arrayOf<String>("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3D9t6WXUSM-xQ&psig=AOvVaw2ewA4kJ1nSkQ5SnHmV26mv&ust=1624122396975000&source=images&cd=vfe&ved=0CAoQjRxqFwoTCOCbi8jVofECFQAAAAAdAAAAABAE"
            , "https://www.google.com/imgres?imgurl=https%3A%2F%2Fwww.elfak.ni.ac.rs%2Fimages%2Fkatedre%2Fracunarstvo%2Femina-milovanovic.jpg&imgrefurl=https%3A%2F%2Fwww.elfak.ni.ac.rs%2Fcv%2Femina-milovanovic&tbnid=4kSLkYyYyuMurM&vet=12ahUKEwibjM7b1aHxAhUNrhoKHQHDB0IQMygAegQIARA5..i&docid=ZxrXsxSG0rJ3nM&w=500&h=500&q=Emina%20Milovanovic&ved=2ahUKEwibjM7b1aHxAhUNrhoKHQHDB0IQMygAegQIARA5"
            , "https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.facebook.com%2Fidespodmacbato%2F&psig=AOvVaw0dN-yK7QwKv7jbrzV5O7Tq&ust=1624122485904000&source=images&cd=vfe&ved=0CAoQjRxqFwoTCKCXzfHVofECFQAAAAAdAAAAABAD")



        // Create adapter passing in the sample user data
        val adapter = CardsAdapter(heros)
        // Attach the adapter to the recyclerview to populate items
        rvContacts.adapter = adapter
        // Set layout manager to position the items
        rvContacts.layoutManager = LinearLayoutManager(this)

    }
}
