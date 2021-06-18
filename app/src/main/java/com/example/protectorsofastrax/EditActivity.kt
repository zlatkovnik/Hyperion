package com.example.protectorsofastrax

import android.app.Dialog
import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.protectorsofastrax.data.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_register.*


class EditActivity : AppCompatActivity() {

    var xp: Double = 1.0

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
                xp = documentSnapshot.getDouble("experience")!!
            }


            edit_save_btn.setOnClickListener {
                val intent = Intent()
                updateProfileInfo()
                intent.putExtra("username", edit_username_edt.text.toString())
                intent.putExtra("email", edit_email_edt.text.toString())
                setResult(1000, intent)
                finish()


            }
            edit_cancel_btn.setOnClickListener {
                finish()
            }
        }
//        edit_save_btn.setOnClickListener {
//            val profileUpdates= userProfileChangeRequest {
////                val email: String = edit_email_edt.text.toString().trim { it -> it <= ' ' }
////                val username: String = edit_username_edt.text.toString().trim { it -> it <= ' ' }
//////                val password: String = edit_password_edt.text.toString().trim { it -> it <= ' ' }
////                val name = edit_name_edt.text.toString()
////                val surname = edit_surname_edt.text.toString()
////                val phone = edit_phone_edt.text.toString();
//
//            }
//            user!!.updateProfile(profileUpdates)
//                .addOnCompleteListener{
//                        task->
//                    if(task.isSuccessful)
//                    {
//                        Log.d("EditActivity:","User profile updated")
//                        Toast.makeText(this, "Successful update", Toast.LENGTH_SHORT)
//                            .show();
//                        finish()
//                    }
//                }
//        }


    }

    private fun updateProfileInfo() {
        var map: MutableMap<String, String> = HashMap<String, String>()
        map["username"] = edit_username_edt.text.toString()
        map["email"] = edit_email_edt.text.toString()
        map["name"] = edit_name_edt.text.toString()
        map["surname"] = edit_surname_edt.text.toString()
        map["phone"] = edit_phone_edt.text.toString()
        FirebaseFirestore.getInstance().collection("users").document(user.uid).update(map.toMap())
    }
}