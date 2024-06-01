package com.cad.proyectofinaletps1.ui

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
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
    private var barcode: String? = null
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
            barcode = it.getString(ARG_BARCODE).toString()
            marca = it.getString("marca")
            categoria = it.getString("categoria")
            key = it.getString(ARG_KEY).toString()
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
        val btnAgregarapres = view.findViewById<Button>(R.id.btnAgregarapres)
        val btnAnadirATablero = view.findViewById<Button>(R.id.btnAnadirATablero)
        val imv = view.findViewById<ImageView>(R.id.imvss)



        btnAnadirATablero.setOnClickListener {
            val cantidad = cantidadLlevar.text.toString().toIntOrNull() ?: 0
            if (cantidad > 0) {
                ShowOerlay(cantidad)
            } else {
                Toast.makeText(requireContext(), "Por favor, ingrese una cantidad válida", Toast.LENGTH_SHORT).show()
            }
        }

        btnAgregarapres.setOnClickListener {
            val cantidadActual = cantidadLlevar.text.toString().toIntOrNull() ?: 0
            cantidadLlevar.setText((cantidadActual + 1).toString())
        }

        btndec.setOnClickListener {
            val cantidadActual = cantidadLlevar.text.toString().toIntOrNull() ?: 0
            if (cantidadActual >= 2) {
                cantidadLlevar.setText((cantidadActual - 1).toString())
            }
        }

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("productos").child(key ?: "")
        databaseReference.child("precio").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val precioNuevo = dataSnapshot.getValue(Double::class.java) ?: 0.0
                Log.d(TAG,"$precioNuevo")
                if (precio != precioNuevo) {
                    actualizarTextoPrecio(precio, precioNuevo)
                    precio = precioNuevo
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error de Firebase: ${databaseError.message}")
            }
        })

        val databaseReference2: DatabaseReference = FirebaseDatabase.getInstance().getReference("productos")
        databaseReference2.orderByChild("barcode").equalTo(barcode)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val producto = snapshot.getValue(Productos::class.java)
                            Log.d(TAG, "Producto: $producto")
                            producto?.let {
                                txtNombreProducto.text = it.nombre
                                txtDescripcionProducto.text = it.descripcion
                                txtPrecioProducto.text = "$${it.precio}"
                                txtBarcode.text = it.barcode.toString()
                                Log.d(TAG, "Cargando imagen desde URL: ${it.imgurl}")
                                Glide.with(this@DetallesProductoFragment)
                                    .load(snapshot.child("filepath").getValue(String::class.java))
                                    .apply(RequestOptions().override(400, 450))
                                    .into(txtImg)
                            }
                        }
                    } else {
                        Log.e(TAG, "Producto no encontrado")
                        Log.e(TAG, "Producto no encontrado ${barcode}")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Error de Firebase: ${databaseError.message}")
                }
            })


        return view
    }

    // Función para actualizar el texto de txtPrecioProd
    private fun actualizarTextoPrecio(precioAnterior: Double, precioNuevo: Double) {
        val textoPrecio = view?.findViewById<TextView>(R.id.txtPrecioProd)
        if (precioAnterior != 0.0 && precioAnterior != precioNuevo) {
            textoPrecio?.text = "Antes: $$precioAnterior | Ahora: $$precioNuevo"
        } else {
            textoPrecio?.text = "$$precioNuevo"
        }
    }

    private fun ShowOerlay(cantidad: Int) {
        val dialog = Dialog(requireActivity())
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_presupuesto)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val spinnerPresupuestos = dialog.findViewById<Spinner>(R.id.spnPresupuestos)
        val txtNoTableros = dialog.findViewById<TextView>(R.id.txtNoTableros)
        val txtSeleccionarTablero = dialog.findViewById<TextView>(R.id.textView2)

        val sharedPreferences = requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val usuarioId = sharedPreferences.getString("userUUID", null) ?: "UUID no encontrado"

        Log.d(TAG,usuarioId)

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("presupuestos")

        val query = databaseReference.orderByChild("usuario_id").equalTo(usuarioId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val presupuestosList = mutableListOf<String>()
                    presupuestosList.add("Seleccione el tablero")

                    for (snapshot in dataSnapshot.children) {
                        val nombrePresupuesto = snapshot.child("nombre").getValue(String::class.java)
                        nombrePresupuesto?.let { presupuestosList.add(it) }
                    }

                    val adapter = object : ArrayAdapter<String>(requireContext(), R.layout.spinner_item, R.id.txtTablero, presupuestosList) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = super.getView(position, convertView, parent)
                            val btnDelete = view.findViewById<Button>(R.id.btnDelete)
                            btnDelete.setOnClickListener {
                                // Handle delete action
                                if (position > 0) {
                                    val nombrePresupuestoSeleccionado = presupuestosList[position]
                                    eliminarPresupuesto(nombrePresupuestoSeleccionado)
                                }
                            }
                            return view
                        }

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = super.getDropDownView(position, convertView, parent)
                            val btnDelete = view.findViewById<Button>(R.id.btnDelete)
                            btnDelete.setOnClickListener {
                                // Handle delete action
                                if (position > 0) {
                                    val nombrePresupuestoSeleccionado = presupuestosList[position]
                                    eliminarPresupuesto(nombrePresupuestoSeleccionado)
                                }
                            }
                            return view
                        }
                    }

                    spinnerPresupuestos.adapter = adapter

                    val nombrePresupuestoPredeterminado = presupuestosList[0]
                    val posicionPredeterminada = presupuestosList.indexOf(nombrePresupuestoPredeterminado)
                    spinnerPresupuestos.setSelection(posicionPredeterminada)

                    spinnerPresupuestos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            if (position > 0) { // Evita la primera opción "Seleccione el tablero"
                                val nombrePresupuestoSeleccionado = presupuestosList[position]
                                val productoId = key // Asegúrate de que 'key' esté definido y accesible en este contexto
                                Log.d(TAG,"La key ess: $key")
                                if (productoId != null) {
                                    agregarProductoAlPresupuesto(nombrePresupuestoSeleccionado, productoId, cantidad, dialog)
                                }
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

                    spinnerPresupuestos.setOnTouchListener { _, _ ->
                        userSelection = true
                        false
                    }

                    txtSeleccionarTablero.visibility = View.VISIBLE
                    txtNoTableros.visibility = View.GONE
                    spinnerPresupuestos.visibility = View.VISIBLE
                } else {
                    txtSeleccionarTablero.visibility = View.GONE
                    txtNoTableros.visibility = View.VISIBLE
                    spinnerPresupuestos.visibility = View.GONE
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

    private fun eliminarPresupuesto(nombrePresupuesto: String) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("presupuestos")
        databaseReference.orderByChild("nombre").equalTo(nombrePresupuesto)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Tablero eliminado", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error al eliminar el tablero", e)
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Error de Firebase: ${databaseError.message}")
                }
            })
    }


    private fun agregarProductoAlPresupuesto(nombrePresupuesto: String, productoId: String, cantidad: Int, dialog: Dialog) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference()
        val productoReference = databaseReference.child("productos").child(productoId)

        productoReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(productoSnapshot: DataSnapshot) {
                if (productoSnapshot.exists()) {
                    val precio = productoSnapshot.child("precio").getValue(Double::class.java) ?: 0.0
                    val totalNuevoProducto = precio * cantidad

                    databaseReference.child("presupuestos").orderByChild("nombre").equalTo(nombrePresupuesto)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (snapshot in dataSnapshot.children) {
                                        val presupuestoKey = snapshot.key
                                        if (presupuestoKey != null) {
                                            val productosReference = databaseReference.child("presupuestos").child(presupuestoKey).child("productos").child(productoId)
                                            val totalReference = databaseReference.child("presupuestos").child(presupuestoKey).child("total")

                                            productosReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(productSnapshot: DataSnapshot) {
                                                    if (productSnapshot.exists()) {
                                                        Toast.makeText(requireContext(), "El producto ya existe en este tablero", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        productosReference.setValue(cantidad)
                                                            .addOnSuccessListener {
                                                                // Actualizar el total del presupuesto
                                                                totalReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                                                    override fun onDataChange(totalSnapshot: DataSnapshot) {
                                                                        var totalActual = totalSnapshot.getValue(Double::class.java) ?: 0.0
                                                                        totalActual += totalNuevoProducto
                                                                        val totalFormateado = String.format("%.2f", totalActual).toDouble()
                                                                        totalReference.setValue(totalFormateado)
                                                                            .addOnSuccessListener {
                                                                                Toast.makeText(requireContext(), "Producto añadido a $nombrePresupuesto", Toast.LENGTH_SHORT).show()
                                                                                dialog.dismiss()  // Close the dialog here
                                                                            }
                                                                            .addOnFailureListener { e ->
                                                                                Log.e(TAG, "Error al actualizar el total del tablero", e)
                                                                            }
                                                                    }

                                                                    override fun onCancelled(databaseError: DatabaseError) {
                                                                        Log.e(TAG, "Error de Firebase: ${databaseError.message}")
                                                                    }
                                                                })
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Log.e(TAG, "Error al agregar el producto al tablero", e)
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
                                    Log.e(TAG, "El tablero no existe")
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e(TAG, "Error de Firebase: ${databaseError.message}")
                            }
                        })
                } else {
                    Log.e(TAG, "El producto no existe")
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
        private const val ARG_BARCODE = "barcode"
        private const val ARG_KEY = "key"
        @JvmStatic
        fun newInstance(barcode: String, key: String?) =
            DetallesProductoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_BARCODE, barcode)
                    putString(ARG_KEY, key)
                }
            }
    }

}