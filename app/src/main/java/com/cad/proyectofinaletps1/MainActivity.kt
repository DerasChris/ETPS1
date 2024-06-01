package com.cad.proyectofinaletps1

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bundle = intent.extras
        val email = bundle?.getString("Mail")
        val provider = bundle?.getString("provider")
        val txtMail = findViewById<TextView>(R.id.txtmail)
        txtMail.setText(email.toString())
    }
}