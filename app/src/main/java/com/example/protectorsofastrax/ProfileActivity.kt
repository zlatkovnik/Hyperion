package com.example.protectorsofastrax

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*


class ProfileActivity : AppCompatActivity() {

    var AVATARS_CHILD = "avatars/"
    val GET_FROM_GALLERY = 3;
    var avatarUri: Uri? = null

    private lateinit var user: FirebaseUser
//    var storage=Firebase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        user=Firebase.auth.currentUser as FirebaseUser
        if(user !=null)
        {
            FirebaseStorage.getInstance().reference
                .child(AVATARS_CHILD+Firebase.auth.uid)
                .downloadUrl
                .addOnSuccessListener {
                    Glide.with(this).load(it.toString()).into(prof_avatar_img)
                }
            val docRef=FirebaseFirestore.getInstance().collection("users").document(user.uid)
            docRef.get().addOnSuccessListener { documentSnapshot ->
                prof_username_edt.text = documentSnapshot.getString("username")
                prof_email_edt.text = documentSnapshot.getString("email")
                prof_battles_won_edt.setText(documentSnapshot.getString("battlesWon"))
                prof_cards_in_collection_edt.setText(documentSnapshot.getString("cardsInCollection"))


            }
          prof_edit_btn.setOnClickListener {
                intent= Intent(this,EditActivity::class.java)
                 intent.putExtra("username",prof_username_edt.text.toString())
                intent.putExtra("email",prof_email_edt.text.toString())
                startActivityForResult(intent,1000)
            }
        }
    prof_chandge_picture_btn.setOnClickListener {
        startActivityForResult(
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            ),
            GET_FROM_GALLERY
        )
    }


    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            GET_FROM_GALLERY->{
                prof_avatar_img.setImageURI(data?.data)
                avatarUri=data?.data

                FirebaseStorage.getInstance().reference
                    .child(AVATARS_CHILD+Firebase.auth.uid)
                    .putFile(avatarUri as Uri)


            }
        }
        if(resultCode==1000)
        {
            prof_username_edt.setText(data?.getStringExtra("username").toString())
            prof_email_edt.setText(data?.getStringExtra("email").toString())
        }
    }
}