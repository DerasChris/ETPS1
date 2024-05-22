package com.cad.proyectofinaletps1

import android.os.Bundle
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


        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            databaseReference = FirebaseDatabase.getInstance().reference

            // Obtener datos del usuario logueado
            databaseReference.child("usuarios").child(userId).addValueEventListener(object : ValueEventListener {
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

            // Obtener presupuesto del usuario logueado
            databaseReference.child("presupuestos").child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val total = dataSnapshot.child("total").getValue(Double::class.java)
                    editTextPpto.setText(total.toString())
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar el error
                }
            })

            // Guardar el nuevo presupuesto
            buttonActualizar.setOnClickListener {
                val nuevoTotal = editTextPpto.text.toString().toDoubleOrNull()
                if (nuevoTotal != null) {
                    databaseReference.child("presupuestos").child(userId).child("total").setValue(nuevoTotal)
                } else {
                    // Manejar el caso en que la entrada no sea un número válido
                }
            }
        } else {
            // El usuario no está logueado
        }
    }
}
