package com.example.protectorsofastrax

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onStart() {
        super.onStart()

        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (auth != null) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_redirect_btn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        login_submit_btn.setOnClickListener {
            when {
                TextUtils.isEmpty(login_email_edt.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(this, "Enter email.", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(login_password_edt.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(this, "Enter password.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val email: String = login_email_edt.text.toString().trim { it <= ' ' }
                    val password: String = login_password_edt.text.toString().trim { it <= ' ' }

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser: FirebaseUser = task.result?.user as FirebaseUser

                                FirebaseFirestore.getInstance().collection("users")
                                    .document(firebaseUser.uid).get()
                                    .addOnSuccessListener { document ->
                                        if (document != null) {
                                            Toast.makeText(
                                                this@LoginActivity, "Welcome ${document["username"]}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@LoginActivity, "User not found!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(
                                            this@LoginActivity, "An error occurred:&#10;${exception.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }


                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("user_id", firebaseUser.uid)
                                intent.putExtra("email", email)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this@LoginActivity, task.exception!!.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }

        }
    }
}