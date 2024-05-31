package com.cad.proyectofinaletps1

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.*

class ComparativaPresupuesto : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private var montox: Double = 0.0;
    private var presupuestoEncontrado: Presupuesto? = null
    private lateinit var adapter: ProductosHistorialAdapter
    private var nombreppres: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val nombrePresupuesto = intent.getStringExtra("nombre")
        nombreppres = nombrePresupuesto
        setContentView(R.layout.activity_comparativa_presupuesto)

        val recyclerView: RecyclerView = findViewById(R.id.rvProductosHistorial)
        recyclerView.layoutManager = LinearLayoutManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }




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
                                            val productoHistorialItem =
                                                ProductoHistorialItem(
                                                    producto.nombre.toString(),
                                                    productoQty.value,
                                                    producto.precio as Double,
                                                    producto.productoKeyy.toString()
                                                )


                                            if (productoHistorialItem != null) {
                                                productosHistorialList.add(productoHistorialItem)
                                            }

                                            totalProductosPresupuesto += producto.precio?.times(
                                                productoQty.value
                                            ) ?: 0.0

                                            val disponible = montox?.minus(totalProductosPresupuesto) ?: 0.0
                                            val porcentajeUsado = if (montox > 0) {
                                                calcularPorcentajeDisponible(totalProductosPresupuesto, montox)
                                            } else {
                                                100.0
                                            }

                                            txtPorcentajeUsado.text = String.format("%.2f", porcentajeUsado)
                                            txtConsumido.text = "$" + String.format("%.2f", totalProductosPresupuesto)
                                            txtDisponible.text = "$" + String.format("%.2f", disponible)


                                            if (index == productosList.size - 1) {
                                                // Construir el adaptador con la lista de elementos
                                                adapter = ProductosHistorialAdapter(productosHistorialList) { producto ->
                                                    // Utilizar la clave del producto asociada con el elemento de la lista
                                                    val presupuestoKey = nombrePresupuesto
                                                    val claveProducto = producto.toString()
                                                    if (presupuestoKey != null) {
                                                        if (claveProducto != null) {
                                                            eliminarProductoDePresupuesto(presupuestoKey, claveProducto)
                                                        }
                                                    }
                                                }
// Asignar el adaptador al RecyclerView
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

    private fun eliminarProductoDePresupuesto(claveProducto: String, presupuestoName: String) {
        val presupuestoId = nombreppres

        // Verificar si se obtuvo correctamente el ID del presupuesto
        if (presupuestoId != null) {
            val presupuestosRef = FirebaseDatabase.getInstance().getReference("presupuestos").child(presupuestoId)
            Log.d(TAG,"El productos es: $claveProducto")
            Log.d(TAG,"El key es: $presupuestoName")
            // Eliminar el producto del presupuesto utilizando la clave del producto
            presupuestosRef.child("productos").child(claveProducto).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Producto eliminado del presupuesto", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al eliminar el producto del presupuesto", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Error: no se pudo obtener el ID del presupuesto", Toast.LENGTH_SHORT).show()
        }
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
    val precio: Double? = 0.0,
    val productoKeyy: String? = ""
)
