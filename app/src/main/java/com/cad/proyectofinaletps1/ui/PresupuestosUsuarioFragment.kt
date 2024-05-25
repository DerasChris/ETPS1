package com.cad.proyectofinaletps1

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class PresupuestosUsuarioFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var database: FirebaseDatabase
    private var mesSelected: String = "Todos"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_presupuestos_usuario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinnerMeses: Spinner = view.findViewById(R.id.spnMeses)
        spinnerMeses.onItemSelectedListener = this

        val recyclerView: RecyclerView = view.findViewById(R.id.rvPresupuestos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FirebaseDatabase.getInstance()

        val presupuestosList = mutableListOf<PresupuestoItem>()

        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val usuarioId = sharedPreferences.getString("userUUID", null) ?: "UUID no encontrado"

        getPresupuestosByUser(usuarioId) { presupuestos ->
            for (presupuesto in presupuestos) {
                presupuestosList.add(PresupuestoItem(presupuesto.nombre.toString()))
            }

            val adapter = PresupuestoAdapter(presupuestosList, requireContext())
            recyclerView.adapter = adapter
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        mesSelected = parent.getItemAtPosition(pos).toString()
        Log.d("Selected option: ", mesSelected)

        val recyclerView: RecyclerView = view?.findViewById(R.id.rvPresupuestos) ?: return
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val presupuestosList = mutableListOf<PresupuestoItem>()
        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val usuarioId = sharedPreferences.getString("userUUID", null) ?: "UUID no encontrado"

        Log.d(TAG, usuarioId)

        getPresupuestosByUser(usuarioId) { presupuestos ->
            for (presupuesto in presupuestos) {
                presupuestosList.add(PresupuestoItem(presupuesto.nombre.toString()))
            }

            val adapterUpdated = PresupuestoAdapter(presupuestosList, requireContext())
            recyclerView.adapter = adapterUpdated
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback.
    }

    private fun getPresupuestosByUser(userBuscado: String, callback: (List<Presupuesto>) -> Unit) {
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
                Log.e("PresupuestosUsuarioFragment", "Error al buscar presupuestos: $error")
            }
        })
    }
}
