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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetallesProductoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetallesProductoFragment : Fragment() {
    // Definir variables para los datos del producto
    private var nombre: String? = null
    private var descripcion: String? = null
    private var precio: Double = 0.0
    private var imgurl: String? = null
    private var barcode: Double = 0.0
    private var marca: String? = null
    private var categoria: String? = null
    private var key: String?=null
    private lateinit var imv: ImageView

    private var contProductos: Int = 1;

    private var userSelection = false

    private val presupuestosList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Obtener los datos del producto de los argumentos
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
        // Inflar el diseño para este fragmento y mostrar los detalles del producto
        val view = inflater.inflate(R.layout.fragment_detalles_producto, container, false)
        // Aquí puedes actualizar la interfaz de usuario con los datos del producto

        val txtNombreProducto = view.findViewById<TextView>(R.id.txtNombreProd)
        val txtDescripcionProducto = view.findViewById<TextView>(R.id.txtDescriptor)
        val txtPrecioProducto = view.findViewById<TextView>(R.id.txtPrecioProd)
        val txtBarcode = view.findViewById<TextView>(R.id.txtBarcode)
        val txtImg = view.findViewById<ImageView>(R.id.imvProduct)
        val btnAdd = view.findViewById<Button>(R.id.btnadd)
        val btndec = view.findViewById<Button>(R.id.btndec)
        val btnagregar = view.findViewById<Button>(R.id.btnAgregarapres)
        val imv = view.findViewById<ImageView>(R.id.imvss)


        // Obtener los datos del producto de los argumentos
        val nombre = arguments?.getString("nombre")
        val descripcion = arguments?.getString("descripcion")
        val precio = arguments?.getDouble("precio")
        val barcode = arguments?.getDouble("barcode")
        val img = arguments?.getString("imgurl")
        val key = arguments?.getString(ARG_KEY)

        Log.d(TAG, "Clave del producto: $key")

        // Establecer los valores de los TextViews con los detalles del producto
        txtNombreProducto.text = nombre
        txtDescripcionProducto.text = descripcion
        txtPrecioProducto.text = precio.toString()
        txtBarcode.text = barcode.toString()

        Glide.with(this)
            .load(img)
            .apply(RequestOptions().override(400, 400)) // Opcional: ajustar el tamaño de la imagen
            .into(txtImg)


        btnAdd.setOnClickListener {
            contProductos++
            btnAdd.text = "Agregar + ${contProductos.toString()}"
        }

        btndec.setOnClickListener {
            if (contProductos>1) {
                contProductos--
                btnAdd.text = "Agregar + ${contProductos.toString()}"
            }
        }

        btnagregar.setOnClickListener {
            ShowOerlay()
        }



        return view

    }
    private fun ShowOerlay() {
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


                val nombrePresupuestoPredeterminado = presupuestosList[0] // O el nombre del presupuesto predeterminado que desees


                val posicionPredeterminada = presupuestosList.indexOf(nombrePresupuestoPredeterminado)
                spinnerPresupuestos.setSelection(posicionPredeterminada)


                spinnerPresupuestos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        // Verifica si la selección es realizada por el usuario
                        if (userSelection) {
                            val nombrePresupuestoSeleccionado = presupuestosList[position]
                            val productoId = key     // ID del producto que deseas agregar
                            val cantidad = contProductos // Cantidad del producto que deseas agregar
                            Log.d(TAG,"Es $productoId")

                            // Llama a la función para agregar el producto al presupuesto

                            if (productoId != null) {
                                agregarProductoAlPresupuesto(nombrePresupuestoSeleccionado, productoId, cantidad)
                            }

                        } else {
                            // La selección es automática, no hagas nada
                        }

                        // Reinicia la bandera para la siguiente selección
                        userSelection = false
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Manejar la situación en la que no se ha seleccionado ningún elemento
                    }
                }

// Agrega un listener para detectar cuándo se muestra el overlay
                spinnerPresupuestos.setOnTouchListener { _, _ ->
                    // Marca la selección como realizada por el usuario cuando se toca el spinner
                    userSelection = true
                    false
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores de Firebase
            }
        })


        val editTextNombrePresupuesto = dialog.findViewById<EditText>(R.id.editTextText)
        val btnCrearPresupuesto = dialog.findViewById<Button>(R.id.btnCrearPresupuesto)


        btnCrearPresupuesto.setOnClickListener {

            val nombrePresupuesto = editTextNombrePresupuesto.text.toString().trim()


            if (nombrePresupuesto.isNotEmpty()) {

                val nuevoPresupuesto = Presupuesto(
                    nombrePresupuesto,
                    obtenerFechaActual(),
                    obtenerNombreMes(),
                    usuarioId
                )


                val nuevoPresupuestoRef = databaseReference.push()
                nuevoPresupuestoRef.setValue(nuevoPresupuesto)
                    .addOnSuccessListener {

                        Log.d(TAG, "Presupuesto creado exitosamente")

                    }
                    .addOnFailureListener { e ->

                        Log.e(TAG, "Error al crear el presupuesto", e)

                    }
            } else {

            }
        }





    }

    private fun agregarProductoAlPresupuesto(nombrePresupuesto: String, productoId: String, cantidad: Int) {
        // Obtén una referencia al presupuesto seleccionado en la base de datos
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference()

        // Consulta el presupuesto por su nombre para obtener su clave (key)
        databaseReference.child("presupuestos").orderByChild("nombre").equalTo(nombrePresupuesto)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Itera sobre los resultados ya que puede haber más de un presupuesto con el mismo nombre
                        for (snapshot in dataSnapshot.children) {
                            val presupuestoKey = snapshot.key
                            if (presupuestoKey != null) {
                                // Genera un nuevo nodo para el producto en el presupuesto
                                val nuevoProductoReference = databaseReference.child("presupuestos")
                                    .child(presupuestoKey).child("productos").child(productoId)

                                // Agrega la cantidad del producto al nodo del presupuesto
                                nuevoProductoReference.setValue(cantidad)
                                    .addOnSuccessListener {
                                        // Maneja el éxito de agregar el producto al presupuesto
                                        Toast.makeText(requireContext(), "Producto añadido a $nombrePresupuesto", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        // Maneja el fallo de agregar el producto al presupuesto
                                        Log.e(TAG, "Error al agregar el producto al presupuesto", e)
                                    }
                            }
                        }
                    } else {
                        // Maneja el caso en el que el presupuesto no existe
                        Log.e(TAG, "El presupuesto no existe")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Maneja los errores de Firebase
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
                // Pasar los datos del producto como argumentos
                arguments = Bundle().apply {
                    putString("nombre", producto.nombre)
                    putString("descripcion", producto.descripcion)
                    producto.precio?.let { putDouble("precio", it) }
                    putString("imgurl", producto.imgurl)
                    producto.barcode?.let { putDouble("barcode", it) }
                    putString("marca", producto.marca)
                    putString("categoria", producto.categoria)
                    putParcelable(ARG_PRODUCTO,producto)
                    putString(ARG_KEY,key)

                }
            }
    }
}