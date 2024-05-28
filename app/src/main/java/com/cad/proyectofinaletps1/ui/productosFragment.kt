package com.cad.proyectofinaletps1.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cad.proyectofinaletps1.AdaptadorProductos
import com.cad.proyectofinaletps1.R
import com.cad.proyectofinaletps1.activity_perfil
import com.cad.proyectofinaletps1.models.Productos
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [productosFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
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
        val txtuser = view.findViewById<TextView>(R.id.txtNombreUser)
        val imgProfile = view.findViewById<ImageView>(R.id.imgProfile)
        txtMail.text = email
        txtuser.text = user
        Glide.with(this)
            .load(profileURL)
            .circleCrop()
            .apply(RequestOptions().override(150, 150)) // Opcional: ajustar el tamaño de la imagen
            .into(imgProfile)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment productosFragment.
         */
        // TODO: Rename and change types and number of parameters
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



