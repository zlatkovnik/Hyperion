package com.example.protectorsofastrax

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.protectorsofastrax.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : AppCompatActivity() {

    val GET_FROM_GALLERY = 3;

    val AVATAR_CHILD = "avatars/"

    var avatarUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        reg_edit_avatar_btn.setOnClickListener {
            startActivityForResult(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI
                ),
                GET_FROM_GALLERY
            )
        }

        reg_submit_btn.setOnClickListener {
            when {
                TextUtils.isEmpty(reg_username_edt.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Enter a valid username",
                        Toast.LENGTH_LONG
                    ).show()
                }
                TextUtils.isEmpty(reg_email_edt.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Enter a valid email address",
                        Toast.LENGTH_LONG
                    ).show()
                }
                TextUtils.isEmpty(reg_password_edt.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Enter a valid password",
                        Toast.LENGTH_LONG
                    ).show()
                }
                avatarUri == null -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Pick a profile image",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    val email: String = reg_email_edt.text.toString().trim { it -> it <= ' ' }
                    val username: String = reg_username_edt.text.toString().trim { it -> it <= ' ' }
                    val password: String = reg_password_edt.text.toString().trim { it -> it <= ' ' }
                    val name = reg_name_edt.text.toString()
                    val surname = reg_surname_edt.text.toString()
                    val phone = reg_phone_edt.text.toString();

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser: FirebaseUser = task.result?.user as FirebaseUser;
                                Toast.makeText(this, "Successful registration", Toast.LENGTH_SHORT)
                                    .show();

                                FirebaseStorage.getInstance().reference
                                    .child(AVATAR_CHILD + firebaseUser.uid)
                                    .putFile(avatarUri as Uri)

                                val user =
                                    User(firebaseUser.uid, email, username, name, surname, phone, 100.0f);

                                FirebaseFirestore.getInstance().collection("users")
                                    .document(user.id).set(user).addOnSuccessListener {
                                        val intent = Intent(this, MainActivity::class.java)
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        intent.putExtra("user_id", firebaseUser.uid)
                                        intent.putExtra("email", email)
                                        startActivity(intent)
                                        finish()
                                    }.addOnFailureListener {
                                        Toast.makeText(
                                            this,
                                            it.message.toString(),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            } else {
                                Toast.makeText(
                                    this,
                                    task.exception!!.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            GET_FROM_GALLERY -> {
                reg_avatar_img.setImageURI(data?.data)
                avatarUri = data?.data
            }
        }
    }
}
