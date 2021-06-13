package com.example.protectorsofastrax

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.protectorsofastrax.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

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

                                val user: User =
                                    User(firebaseUser.uid, email, username, name, surname, phone);

                                FirebaseFirestore.getInstance().collection("users")
                                    .document(firebaseUser.uid).set(user).addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "you were registered successfully.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }.addOnFailureListener {
                                        Toast.makeText(
                                            this,
                                            it.message.toString(),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("user_id", firebaseUser.uid)
                                intent.putExtra("email", email)
                                startActivity(intent)
                                finish()
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
}
