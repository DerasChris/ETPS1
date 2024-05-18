package com.cad.proyectofinaletps1.ui

import android.app.Dialog
import android.content.ContentValues.TAG
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cad.proyectofinaletps1.R
import com.cad.proyectofinaletps1.models.Productos
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DetallesProductoFragment : Fragment() {
    private var nombre: String? = null
    private var descripcion: String? = null
    private var precio: Double = 0.0
    private var imgurl: String? = null
    private var barcode: Double = 0.0
    private var marca: String? = null
    private var categoria: String? = null
    private var key: String?=null
    private lateinit var imv: ImageView

    private var userSelection = false

    private val presupuestosList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            nombre = it.getString("nombre")
            descripcion = it.getString("descripcion")
            precio = it.getDouble("precio")
            imgurl = it.getString("imgurl")
            barcode = it.getDouble("barcode")
            marca = it.getString("marca")
            categoria = it.getString("categoria")
            key = it.getString("key")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalles_producto, container, false)

        val txtNombreProducto = view.findViewById<TextView>(R.id.txtNombreProd)
        val txtDescripcionProducto = view.findViewById<TextView>(R.id.txtDescriptor)
        val txtPrecioProducto = view.findViewById<TextView>(R.id.txtPrecioProd)
        val txtBarcode = view.findViewById<TextView>(R.id.txtBarcode)
        val txtImg = view.findViewById<ImageView>(R.id.imvProduct)
        val cantidadLlevar = view.findViewById<EditText>(R.id.cantidadLlevar)
        val btndec = view.findViewById<Button>(R.id.btndec)
        val btnAgregar = view.findViewById<Button>(R.id.btnAgregarapres)
        val imv = view.findViewById<ImageView>(R.id.imvss)

        val nombre = arguments?.getString("nombre")
        val descripcion = arguments?.getString("descripcion")
        val precio = arguments?.getDouble("precio")
        val barcode = arguments?.getDouble("barcode")
        val img = arguments?.getString("imgurl")
        val key = arguments?.getString(ARG_KEY)

        Log.d(TAG, "Clave del producto: $key")

        txtNombreProducto.text = nombre
        txtDescripcionProducto.text = descripcion
        txtPrecioProducto.text = "$" + precio.toString()
        txtBarcode.text = barcode.toString()

        Glide.with(this)
            .load(img)
            .apply(RequestOptions().override(400, 450))
            .into(txtImg)

        btnAgregar.setOnClickListener {
            val cantidad = cantidadLlevar.text.toString().toIntOrNull() ?: 0
            if (cantidad > 0) {
                ShowOerlay(cantidad)
            } else {
                Toast.makeText(requireContext(), "Por favor, ingrese una cantidad válida", Toast.LENGTH_SHORT).show()
            }
        }

        btndec.setOnClickListener {
            val cantidadActual = cantidadLlevar.text.toString().toIntOrNull() ?: 0
            if (cantidadActual >= 1) {
                cantidadLlevar.setText((cantidadActual - 1).toString())
            }
        }

        return view
    }

    private fun ShowOerlay(cantidad: Int) {
        val dialog = Dialog(requireActivity())
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_presupuesto)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val spinnerPresupuestos = dialog.findViewById<Spinner>(R.id.spnPresupuestos)

        val usuarioId = "usuario_1"
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("presupuestos")

        val query = databaseReference.orderByChild("usuario_id").equalTo(usuarioId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val presupuestosList = mutableListOf<String>()

                for (snapshot in dataSnapshot.children) {
                    val nombrePresupuesto = snapshot.child("nombre").getValue(String::class.java)
                    nombrePresupuesto?.let { presupuestosList.add(it) }
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, presupuestosList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerPresupuestos.adapter = adapter

                val nombrePresupuestoPredeterminado = presupuestosList[0]
                val posicionPredeterminada = presupuestosList.indexOf(nombrePresupuestoPredeterminado)
                spinnerPresupuestos.setSelection(posicionPredeterminada)

                spinnerPresupuestos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (userSelection) {
                            val nombrePresupuestoSeleccionado = presupuestosList[position]
                            val productoId = key

                            if (productoId != null) {
                                agregarProductoAlPresupuesto(nombrePresupuestoSeleccionado, productoId, cantidad, dialog)
                            }
                        } else {
                            userSelection = false
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}

                }

                spinnerPresupuestos.setOnTouchListener { _, _ ->
                    userSelection = true
                    false
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        val editTextNombrePresupuesto = dialog.findViewById<EditText>(R.id.editTextText)
        val btnCrearPresupuesto = dialog.findViewById<Button>(R.id.btnCrearPresupuesto)

        btnCrearPresupuesto.setOnClickListener {
            val nombrePresupuesto = editTextNombrePresupuesto.text.toString().trim()
            if (nombrePresupuesto.isNotEmpty()) {
                // Verifica si el usuario ya tiene un tablero con el mismo nombre
                databaseReference.orderByChild("nombre").equalTo(nombrePresupuesto)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            var nombreDuplicado = false
                            for (snapshot in dataSnapshot.children) {
                                val usuarioIdExistente = snapshot.child("usuario_id").getValue(String::class.java)
                                if (usuarioIdExistente == usuarioId) {
                                    nombreDuplicado = true
                                    break
                                }
                            }
                            if (nombreDuplicado) {
                                Toast.makeText(requireContext(), "Ya existe un tablero con este nombre", Toast.LENGTH_SHORT).show()
                            } else {
                                val nuevoPresupuesto = Presupuesto(
                                    nombrePresupuesto,
                                    obtenerFechaActual(),
                                    obtenerNombreMes(),
                                    usuarioId
                                )
                                val nuevoPresupuestoRef = databaseReference.push()
                                nuevoPresupuestoRef.setValue(nuevoPresupuesto)
                                    .addOnSuccessListener {
                                        Toast.makeText(requireContext(), "Tablero creado", Toast.LENGTH_SHORT).show()
                                        Log.d(TAG, "Tablero creado exitosamente")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "Error al crear el presupuesto", e)
                                    }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e(TAG, "Error de Firebase: ${databaseError.message}")
                        }
                    })
            }
        }
    }

    private fun agregarProductoAlPresupuesto(nombrePresupuesto: String, productoId: String, cantidad: Int, dialog: Dialog) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference()
        databaseReference.child("presupuestos").orderByChild("nombre").equalTo(nombrePresupuesto)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val presupuestoKey = snapshot.key
                            if (presupuestoKey != null) {
                                val productosReference = databaseReference.child("presupuestos").child(presupuestoKey).child("productos").child(productoId)

                                productosReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(productSnapshot: DataSnapshot) {
                                        if (productSnapshot.exists()) {
                                            Toast.makeText(requireContext(), "El producto ya existe en este tablero", Toast.LENGTH_SHORT).show()
                                        } else {
                                            productosReference.setValue(cantidad)
                                                .addOnSuccessListener {
                                                    Toast.makeText(requireContext(), "Producto añadido a $nombrePresupuesto", Toast.LENGTH_SHORT).show()
                                                    dialog.dismiss()  // Close the dialog here
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(TAG, "Error al agregar el producto al presupuesto", e)
                                                }
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        Log.e(TAG, "Error de Firebase: ${databaseError.message}")
                                    }
                                })
                            }
                        }
                    } else {
                        Log.e(TAG, "El presupuesto no existe")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Error de Firebase: ${databaseError.message}")
                }
            })
    }

    private fun obtenerFechaActual(): String {
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(currentDate)
    }

    private fun obtenerNombreMes(): String {
        val calendar = Calendar.getInstance()
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        return month.toLowerCase(Locale.getDefault())
    }

    data class Presupuesto(
        val nombre: String,
        val fecha_creacion: String,
        val mes: String,
        val usuario_id: String
    )

    companion object {
        private const val ARG_PRODUCTO = "producto"
        private const val ARG_KEY = "key"

        @JvmStatic
        fun newInstance(producto: Productos, key: String) =
            DetallesProductoFragment().apply {
                arguments = Bundle().apply {
                    putString("nombre", producto.nombre)
                    putString("descripcion", producto.descripcion)
                    producto.precio?.let { putDouble("precio", it) }
                    putString("imgurl", producto.imgurl)
                    producto.barcode?.let { putDouble("barcode", it) }
                    putString("marca", producto.marca)
                    putString("categoria", producto.categoria)
                    putParcelable(ARG_PRODUCTO, producto)
                    putString(ARG_KEY, key)
                }
            }
    }
}
