package com.cad.proyectofinaletps1.ui

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cad.proyectofinaletps1.AdaptadorProductos
import com.cad.proyectofinaletps1.R
import com.cad.proyectofinaletps1.activity_perfil
import com.cad.proyectofinaletps1.models.Productos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class productosFragment : Fragment(), AdaptadorProductos.OnItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_productos, container, false)


        // Encontrar RecyclerView en el diseño del fragmento
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)

        // Configurar el GridLayoutManager para mostrar 2 tarjetas por fila
        val layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.layoutManager = layoutManager

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("productos")

        val dataList: MutableList<Productos> = mutableListOf()
        val productKeys: MutableList<String> = mutableListOf()

        val btnperf = view.findViewById<Button>(R.id.btnperfil)

        btnperf.setOnClickListener {
            val intent = Intent(context, activity_perfil::class.java)
            startActivity(intent)
        }

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataList.clear()
                productKeys.clear()

                for (snapshot in dataSnapshot.children) {
                    val productKey = snapshot.key
                    productKey?.let {productKeys.add(it)}

                    val nombre = snapshot.child("nombre").getValue(String::class.java)
                    val descripcion = snapshot.child("descripcion").getValue(String::class.java)
                    val precio = snapshot.child("precio").getValue(Double::class.java)
                    val img = snapshot.child("filepath").getValue(String::class.java)
                    val barcode = snapshot.child("barcode").getValue(String::class.java)
                    val categoria = snapshot.child("categoria").getValue(String::class.java)
                    val marca = snapshot.child("marca").getValue(String::class.java)


                    if (nombre != null && descripcion != null && precio != null) {
                        dataList.add(Productos(nombre, descripcion, precio, img,barcode,categoria,marca,productKey))
                    }
                }

                val adapter = AdaptadorProductos(dataList,productKeys)
                // Configurar el listener en el adaptador
                adapter.setOnItemClickListener(this@productosFragment)
                recyclerView.adapter = adapter


            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores de Firebase
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val bundle = requireActivity().intent.extras
        val email = bundle?.getString("Mail")
        val user = bundle?.getString("User")
        val profileURL = bundle?.getString("url")

        val txtMail = view.findViewById<TextView>(R.id.txtmails)
        val txtuser = view.findViewById<TextView>(R.id.tvCorreo)
        val imgProfile = view.findViewById<ImageView>(R.id.imgProfileP)



        txtMail.text = email
        txtuser.text = user
        Glide.with(this)
            .load(profileURL)
            .circleCrop()
            .apply(RequestOptions().override(150, 150)) // Opcional: ajustar el tamaño de la imagen
            .into(imgProfile)

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("monto")
        val txtMonto = view.findViewById<TextView>(R.id.txtMontoPres)
        var monto: Double = 0.0
        //  ID del usuario actual
        val usuarioId = FirebaseAuth.getInstance().currentUser?.uid


        // Verifica que el ID del usuario no sea nulo antes de realizar la consulta
        usuarioId?.let { uid ->
            // Realiza la consulta en la base de datos de Firebase para obtener el monto correspondiente al usuario actual
            databaseReference.child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Verifica si existe un monto para el usuario actual
                    if (dataSnapshot.exists()) {
                        // Obtén el monto desde el dataSnapshot
                        monto = dataSnapshot.child("montopresupuestal").getValue(Double::class.java)!!

                        // Muestra el monto en el TextView correspondiente

                        txtMonto.text = "$%.2f".format(monto)

                    } else {
                        // todos
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Maneja los errores de la base de datos
                    Log.e(TAG, "Error al obtener el monto del usuario: ${databaseError.message}")
                }
            })
        }

        // Obtener el total de todos los presupuestos del usuario actual
        val presupuestosReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("presupuestos")
        val txtTotalConsumido = view.findViewById<TextView>(R.id.txtTotalConsumidoP)
        val textaviso = view.findViewById<TextView>(R.id.txtAviso)



        usuarioId?.let { uid ->
            presupuestosReference.orderByChild("usuario_id").equalTo("usuario_"+uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(TAG,"El us: $uid")
                    var totalConsumido = 0.0
                    for (snapshot in dataSnapshot.children) {
                        val total = snapshot.child("total").getValue(Double::class.java) ?: 0.0
                        totalConsumido += total
                    }

                    if (isAdded) {
                        if (totalConsumido > monto) {
                            val red = ContextCompat.getColor(requireContext(), R.color.red)
                            txtTotalConsumido.setTextColor(red)
                            txtTotalConsumido.text = "$%.2f".format(totalConsumido)
                            val diferencia = kotlin.math.abs(totalConsumido - monto)
                            textaviso.text = "Has excedido tu presupuesto por: $%.2f".format(diferencia)
                            textaviso.visibility = View.VISIBLE
                        } else {
                            val black = ContextCompat.getColor(requireContext(), R.color.black)
                            txtTotalConsumido.setTextColor(black)
                            txtTotalConsumido.text = "$%.2f".format(totalConsumido)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Error al obtener los presupuestos del usuario: ${databaseError.message}")
                }
            })

    }

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            productosFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(producto: Productos, key: String) {
        val detallesFragment =
            producto.barcode?.let { DetallesProductoFragment.newInstance(it,producto.key) }

        // Reemplazar el contenido del contenedor de fragmentos con el fragmento de detalles del producto
        detallesFragment?.let {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, it)
                .addToBackStack(null)  // Permite volver al fragmento anterior con el botón de atrás
                .commit()
        }
    }

}