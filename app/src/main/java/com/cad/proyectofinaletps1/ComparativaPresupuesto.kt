package com.cad.proyectofinaletps1

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.*
import kotlin.time.times

class ComparativaPresupuesto : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private var montox: Double = 0.0;
    private var presupuestoEncontrado: Presupuesto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comparativa_presupuesto)

        val recyclerView: RecyclerView = findViewById(R.id.rvProductosHistorial)
        recyclerView.layoutManager = LinearLayoutManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Log.d(TAG, "acá truena")

        val nombrePresupuesto = intent.getStringExtra("nombre")

        val txtNombrePresupuesto = findViewById<TextView>(R.id.txtNombrePresupuesto)
        val txtTotalPresupuesto = findViewById<TextView>(R.id.txtTotalPresupuesto)
        val txtConsumido = findViewById<TextView>(R.id.txtConsumido)
        val txtDisponible = findViewById<TextView>(R.id.txtDisponible)
        val txtPorcentajeUsado = findViewById<TextView>(R.id.txtPorcentajeUsado)

        // Obtén la referencia a la base de datos de Firebase
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("monto")

        // Obtén el ID del usuario actual
        val usuarioId = FirebaseAuth.getInstance().currentUser?.uid

        // Verifica que el ID del usuario no sea nulo antes de realizar la consulta
        usuarioId?.let { uid ->
            // Realiza la consulta en la base de datos de Firebase para obtener el monto correspondiente al usuario actual
            databaseReference.child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Verifica si existe un monto para el usuario actual
                    if (dataSnapshot.exists()) {
                        // Obtén el monto desde el dataSnapshot
                        val monto = dataSnapshot.child("montopresupuestal").getValue(Double::class.java)
                        if (monto != null) {
                            montox = monto
                        }
                    } else {
                        // Si no existe un monto para el usuario actual, muestra un mensaje alternativo o realiza alguna acción
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Maneja los errores de la base de datos
                    Log.e(TAG, "Error al obtener el monto del usuario: ${databaseError.message}")
                }
            })
        }

        var totalProductosPresupuesto = 0.0

        database = FirebaseDatabase.getInstance()

        val presupuestosRef = database.getReference("presupuestos")
        val query = presupuestosRef.orderByChild("nombre").equalTo(nombrePresupuesto)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val presupuesto = childSnapshot.getValue(Presupuesto::class.java)
                    if (presupuesto != null) {
                        txtNombrePresupuesto.text = presupuesto.nombre
                        txtTotalPresupuesto.text = montox.toString()

                        if (presupuesto.productos != null) {
                            val productosHistorialList = mutableListOf<ProductoHistorialItem>()
                            var totalProductosPresupuesto = 0.0

                            val productosList = presupuesto.productos.entries.toList()
                            for ((index, productoQty) in productosList.withIndex()) {
                                val productosRef = database.getReference("productos").child(productoQty.key)

                                productosRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val producto = snapshot.getValue(Producto::class.java)
                                        if (producto != null) {
                                            productosHistorialList.add(
                                                ProductoHistorialItem(
                                                    producto.nombre.toString(),
                                                    productoQty.value,
                                                    producto.precio as Double
                                                )
                                            )

                                            totalProductosPresupuesto += producto.precio * productoQty.value

                                            val disponible = montox?.minus(totalProductosPresupuesto) ?: 0.0
                                            val porcentajeUsado = if (montox > 0) {
                                                calcularPorcentajeDisponible(totalProductosPresupuesto, montox)
                                            } else {
                                                100.0
                                            }

                                            txtPorcentajeUsado.text = String.format("%.2f", porcentajeUsado)
                                            txtConsumido.text = "$" + String.format("%.2f", totalProductosPresupuesto)
                                            txtDisponible.text = "$" + String.format("%.2f", disponible)

                                            // Only set the adapter once all products have been processed
                                            if (index == productosList.size - 1) {
                                                val adapter = ProductosHistorialAdapter(productosHistorialList)
                                                recyclerView.adapter = adapter
                                            }
                                        } else {
                                            Log.d("Not found", "Producto no encontrado")
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("MainActivity", "Error al buscar producto: $error")
                                    }
                                })
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Error al buscar presupuesto: $error")
            }
        })
    }

    fun calcularPorcentajeDisponible(cantidadConsumida: Double, cantidadTotal: Double): Double {
        val porcentajeDisponible = (cantidadConsumida / cantidadTotal) * 100
        return porcentajeDisponible
    }
}

data class Presupuesto(
    val fecha_creacion: String? = "",
    val mes: String? = "",
    val nombre: String? = "",
    val productos: Map<String, Int>? = emptyMap(),
    val total: Double? = 0.0,
    val usuario_id: String? = ""
)

data class Producto(
    val barcode: String? = "",
    val categoria: String? = "",
    val descripcion: String? = "",
    val filepath: String? = "",
    val marca: String? = "",
    val nombre: String? = "",
    val precio: Double? = 0.0
)
