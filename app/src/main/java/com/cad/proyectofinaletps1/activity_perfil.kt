package com.cad.proyectofinaletps1

import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseApp
import android.app.Application
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference


class activity_perfil : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var buttonActualizar: Button
    private lateinit var databaseReference: DatabaseReference

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
        val textViewPpto = findViewById<TextView>(R.id.textView8)
        //textView.text = "Sofia"
        databaseReference = FirebaseDatabase.getInstance().reference.child("usuarios").child("usuario_1")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val nombre = dataSnapshot.child("nombre").getValue(String::class.java)
                textView.text = nombre
                val correo = dataSnapshot.child("correo").getValue(String::class.java)
                textViewCorreo.text = correo
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        databaseReference = FirebaseDatabase.getInstance().reference.child("presupuestos").child("presupuesto_1")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val total = dataSnapshot.child("total").getValue(Double::class.java)
                textViewPpto.text = total.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
        /* buttonActualizar = findViewById(R.id.button2)
        buttonActualizar.setOnClickListener {

            val nuevoTotal = 100.0
            databaseReference.child("total").setValue(nuevoTotal)
        } */
    // TODO CODIGO ANTES QUE ESTA LINEA
    }
}