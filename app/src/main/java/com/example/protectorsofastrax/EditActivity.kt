package com.example.protectorsofastrax

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.activity_profile.*

class EditActivity : AppCompatActivity() {

    private lateinit var user: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        user = Firebase.auth.currentUser as FirebaseUser
        if (user != null) {
            val docRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
            docRef.get().addOnSuccessListener { documentSnapshot ->
                edit_username_edt.setText(documentSnapshot.getString("username"))
                edit_email_edt.setText(documentSnapshot.getString("email"))
                edit_phone_edt.setText(documentSnapshot.getString("phone"))
                edit_name_edt.setText(documentSnapshot.getString("name"))
                edit_surname_edt.setText(documentSnapshot.getString("surname"))
            }
            edit_save_btn.setOnClickListener {
                val intent = Intent()

                intent.putExtra("username", edit_username_edt.text.toString())
                intent.putExtra("email", edit_email_edt.text.toString())
                intent.putExtra("phone", edit_phone_edt.text.toString())
                intent.putExtra("name", edit_name_edt.text.toString())
                intent.putExtra("surname", edit_surname_edt.text.toString())


            }
        }


    }
}