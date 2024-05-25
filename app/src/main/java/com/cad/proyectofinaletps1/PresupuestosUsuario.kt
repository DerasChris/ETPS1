package com.cad.proyectofinaletps1

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.*

class PresupuestosUsuario : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var database: FirebaseDatabase
    private var mesSelected: String = "Todos"
    // To-Do: Enviar id del usuario autenticado


    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        mesSelected = parent.getItemAtPosition(pos).toString()
        Log.d("Selected option: ", mesSelected)

        val recyclerView: RecyclerView = findViewById(R.id.rvPresupuestos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val presupuestosList = mutableListOf<PresupuestoItem>()
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val usuarioId = sharedPreferences.getString("userUUID", null) ?: "UUID no encontrado"

        Log.d(TAG,"$usuarioId")

        getPresupuestosByUser(usuarioId) { presupuestos ->
            for (presupuesto in presupuestos) {
                presupuestosList.add(PresupuestoItem(presupuesto.nombre.toString()))
            }

            val adapterUpdated = PresupuestoAdapter(presupuestosList, this)
            recyclerView.adapter = adapterUpdated
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback.
    }
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_presupuestos_usuario)

        val spinnerMeses: Spinner = findViewById(R.id.spnMeses)
        spinnerMeses.onItemSelectedListener = this

        val recyclerView: RecyclerView = findViewById(R.id.rvPresupuestos)
        recyclerView.layoutManager = LinearLayoutManager(this)



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FirebaseDatabase.getInstance()

        val presupuestosList = mutableListOf<PresupuestoItem>()

        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val usuarioId = sharedPreferences.getString("userUUID", null) ?: "UUID no encontrado"

        getPresupuestosByUser(usuarioId) { presupuestos ->
            for (presupuesto in presupuestos) {
                presupuestosList.add(PresupuestoItem(presupuesto.nombre.toString()))
            }

            val adapter = PresupuestoAdapter(presupuestosList, this)
            recyclerView.adapter = adapter
        }
    }

    fun getPresupuestosByUser(userBuscado: String, callback: (List<Presupuesto>) -> Unit) {
        val presupuestosRef = database.getReference("presupuestos")

        var query = presupuestosRef.orderByChild("usuario_id").equalTo(userBuscado)

        Log.d("mes selected onCreate", mesSelected)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val presupuestos = mutableListOf<Presupuesto>()
                for (usuarioSnapshot in snapshot.children) {
                    val presupuesto = usuarioSnapshot.getValue(Presupuesto::class.java)

                    if (mesSelected != "Todos") {
                        if (presupuesto?.mes.toString().lowercase() == mesSelected.toString().lowercase()) {
                            presupuesto?.let {
                                presupuestos.add(it)
                            }
                        }
                    } else {
                        presupuesto?.let {
                            presupuestos.add(it)
                        }
                    }
                }
                callback(presupuestos)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Error al buscar presupuestos: $error")
            }
        })
    }
}