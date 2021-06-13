package com.example.protectorsofastrax

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        reg_submit_btn.setOnClickListener {
            if(TextUtils.isEmpty(reg_username_edt.text.toString().trim{it <= ' '}) )
            {
                Toast.makeText(this@RegisterActivity, "Enter username", Toast.LENGTH_LONG).show()
            }
            else if(TextUtils.isEmpty(reg_password_edt.text.toString().trim{it <= ' '}))
            {
                Toast.makeText(this@RegisterActivity, "Enter password", Toast.LENGTH_LONG).show()
            }
            else{
                val username: String=reg_username_edt.text.toString().trim{it -> it <=' '}
                val password:String=reg_password_edt.text.toString().trim{it -> it <=' '}
                val name = reg_name_edt.text.toString()
                val surname=reg_surname_edt.text.toString()
                val phone=reg_phone_edt.text.toString();

               
            }

        }
    }
}
