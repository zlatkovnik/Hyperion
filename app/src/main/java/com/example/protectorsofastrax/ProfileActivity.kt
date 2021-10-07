package com.example.protectorsofastrax

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.protectorsofastrax.data.Card
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.asDeferred


class ProfileActivity : AppCompatActivity() {

    var AVATARS_CHILD = "avatars/"
    val GET_FROM_GALLERY = 3;
    var avatarUri: Uri? = null

    private lateinit var user: FirebaseUser

    private lateinit var userId: String

    private var cardsArrayAdapter: ArrayAdapter<String>? = null

    //    var storage=Firebase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        user = Firebase.auth.currentUser as FirebaseUser
        userId = intent.getStringExtra("user_id") as String
        if (userId != null) {
            FirebaseStorage.getInstance().reference
                .child(AVATARS_CHILD + userId)
                .downloadUrl
                .addOnSuccessListener {
                    Glide.with(this).load(it.toString()).into(prof_avatar_img)
                }
            val docRef = FirebaseFirestore.getInstance().collection("users").document(userId)
            docRef.get().addOnSuccessListener { documentSnapshot ->
                var cardIds: ArrayList<String> = documentSnapshot.get("cards") as ArrayList<String>
                var experience: Long = documentSnapshot.getLong("experience") as Long
                prof_username_edt.text = documentSnapshot.getString("username")
                prof_email_edt.text = documentSnapshot.getString("email")
                prof_level_txtV.text = "${experience.div(100)} LVL"
                prof_experience_prB.progress = experience.mod(100)
                prof_email_edt.text = documentSnapshot.getString("email")
                prof_battles_won_edt.setText(documentSnapshot.get("battlesWon").toString())
                prof_cards_in_collection_edt.setText(cardIds.size.toString())

            }
            if (Firebase.auth.uid != userId) {
                prof_edit_btn.visibility = View.INVISIBLE
                prof_edit_btn.isEnabled = false
                prof_chandge_picture_btn.visibility = View.INVISIBLE
                prof_chandge_picture_btn.isEnabled = false
            }
            prof_edit_btn.setOnClickListener {
                intent = Intent(this, EditActivity::class.java)
                intent.putExtra("username", prof_username_edt.text.toString())
                intent.putExtra("email", prof_email_edt.text.toString())
                startActivityForResult(intent, 1000)
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
        prof_back_btn.setOnClickListener {
            if (isTaskRoot) {
                val intent = Intent(this@ProfileActivity, MainActivity::class.java)
                startActivity(intent)
            } else {
                finish()
            }
        }

        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener {

                GlobalScope.launch {
                    cardsArrayAdapter = ArrayAdapter(this@ProfileActivity, android.R.layout.simple_list_item_1)
                    var cardIds = it["cards"] as ArrayList<String>
                    var requests = cardIds.map { cid ->
                        FirebaseFirestore.getInstance().collection("cards").document(cid).get()
                            .asDeferred()
                    }
                    val results: ArrayList<DocumentSnapshot> =
                        requests.awaitAll() as ArrayList<DocumentSnapshot>
                    results.forEach {
                        var name = it["name"] as String
                        var race = it["race"] as String
                        var clas = it["clas"] as String
                        var power = it["power"] as Long
                        var txt= "$name  power:$power"
                        cardsArrayAdapter!!.add(txt)
                    }
                    this@ProfileActivity.runOnUiThread(Runnable {
                        prof_rw.adapter = cardsArrayAdapter
                    })


                }
            }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        if(isTaskRoot){
            val intent = Intent(this@ProfileActivity, MainActivity::class.java)
            startActivity(intent)
        } else {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            GET_FROM_GALLERY -> {
                prof_avatar_img.setImageURI(data?.data)
                avatarUri = data?.data

                FirebaseStorage.getInstance().reference
                    .child(AVATARS_CHILD + Firebase.auth.uid)
                    .putFile(avatarUri as Uri)


            }
        }
        if (resultCode == 1000) {
            prof_username_edt.setText(data?.getStringExtra("username").toString())
            prof_email_edt.setText(data?.getStringExtra("email").toString())
        }
    }
}