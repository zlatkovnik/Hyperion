package com.example.protectorsofastrax

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_profile.*


class ProfileActivity : AppCompatActivity() {

    private lateinit var user: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        user=Firebase.auth.currentUser as FirebaseUser
        if(user !=null)
        {
            //kod za sliku ovde
            val docRef=FirebaseFirestore.getInstance().collection("users").document(user.uid)
            docRef.get().addOnSuccessListener { documentSnapshot ->
                prof_username_edt.setText(documentSnapshot.getString("username"))
                prof_email_edt.setText(documentSnapshot.getString("email"))
                prof_battles_won_edt.setText(documentSnapshot.getString("battlesWon"))
                prof_cards_in_collection_edt.setText(documentSnapshot.getString("cardsInCollection"))

            }
        }
    }
}