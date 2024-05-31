package com.cad.proyectofinaletps1

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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

        val textView = findViewById<TextView>(R.id.tv_nombre)
        val img = findViewById<ImageView>(R.id.imgProfileP)
        val montoAntes = findViewById<TextView>(R.id.txtMontoAnterior)

        val textViewCorreo = findViewById<TextView>(R.id.tvCorreo)
        editTextPpto = findViewById<EditText>(R.id.editTextPpto)
        buttonActualizar = findViewById<Button>(R.id.button2)

        val sharedPreferences: SharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val UsuarioActual = sharedPreferences.getString("userUUID", "valor predeterminado")
        val nombre = sharedPreferences.getString("name", null) ?: ""
        val email = sharedPreferences.getString("email", null) ?: ""
        val imgf = sharedPreferences.getString("profile", null) ?: ""
        Glide.with(this)
            .load(imgf)
            .circleCrop()
            .into(img)


        UsuarioActual?.let { Log.d(TAG, it) }
        if (UsuarioActual != null) {
            textView.text = nombre
            textViewCorreo.text = email

            // Inicializar Firebase
            firebaseAuth = FirebaseAuth.getInstance()
            val currentUser: FirebaseUser? = firebaseAuth.currentUser
            val usuarioId = currentUser?.uid

            databaseReference = FirebaseDatabase.getInstance().reference

            // Obtener el monto actual del usuario
                    usuarioId?.let { uid ->
                        databaseReference.child("monto").child(uid).addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    val montoActual = dataSnapshot.child("montopresupuestal").getValue(Double::class.java)
                                    montoAntes.text = "Monto actual $: $montoActual" // Mostrar el monto actual antes de actualizarlo
                                } else {
                                    // No se encontró ningún monto para el usuario
                                    montoAntes.text = "Monto actual: No disponible"
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Manejar errores de Firebase
                                Log.e(TAG, "Error al obtener el monto del usuario: ${databaseError.message}")
                                montoAntes.text = "Monto actual: Error al obtener el monto"
                            }
                        })
                    }

            // Listener para el botón
            buttonActualizar.setOnClickListener {
                val monto = editTextPpto.text.toString().toDouble()

                // Crear objeto HashMap para almacenar el monto y el ID del usuario
                val montoUsuario = HashMap<String, Any>()
                montoUsuario["montopresupuestal"] = monto
                montoUsuario["usuario_id"] = UsuarioActual



                // Guardar el objeto en Firebase
                usuarioId?.let { it1 ->
                    databaseReference.child("monto").child(it1).setValue(montoUsuario)
                        .addOnSuccessListener {
                            Toast.makeText(this@activity_perfil, "Monto actualizado correctamente", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error al guardar el monto: ${e.message}")
                            Toast.makeText(this@activity_perfil, "Error al guardar el monto", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        } else {
            // El usuario no está logueado
        }
    }

}
