package com.cad.proyectofinaletps1

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import com.google.firebase.database.*

class activity_perfil : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var buttonActualizar: Button
    private lateinit var editTextPpto: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_perfil)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val textView = findViewById<TextView>(R.id.textView_nombre)

        val textViewCorreo = findViewById<TextView>(R.id.textView2)
        editTextPpto = findViewById<EditText>(R.id.editTextPpto)
        buttonActualizar = findViewById<Button>(R.id.button2)

        val sharedPreferences: SharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val UsuarioActual = sharedPreferences.getString("userUUID", "valor predeterminado")


        UsuarioActual?.let { Log.d(TAG, it) }
        if (UsuarioActual != null) {
            databaseReference = FirebaseDatabase.getInstance().reference

            // Obtener datos del usuario logueado
            databaseReference.child("usuarios").child(UsuarioActual).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val nombre = dataSnapshot.child("nombre").getValue(String::class.java)
                    textView.text = nombre
                    val correo = dataSnapshot.child("correo").getValue(String::class.java)
                    textViewCorreo.text = correo
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar el error
                }
            })


        } else {
            // El usuario no está logueado
        }
    }
}
