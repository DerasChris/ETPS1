package com.cad.proyectofinaletps1.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cad.proyectofinaletps1.AdaptadorProductos
import com.cad.proyectofinaletps1.R
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
class productosFragment : Fragment() {
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

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_productos, container, false)

        // Encontrar RecyclerView en el diseño del fragmento
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)

        // Configurar el GridLayoutManager para mostrar 2 tarjetas por fila
        val layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.layoutManager = layoutManager

        // Referencia a la base de datos de Firebase
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("productos")

        // Lista para almacenar los datos recuperados de Firebase
        val dataList: MutableList<Productos> = mutableListOf()

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Limpiar la lista de datos antes de agregar nuevos datos
                dataList.clear()

                for (snapshot in dataSnapshot.children) {
                    // Obtener nombre, descripción y precio de cada producto
                    val nombre = snapshot.child("nombre").getValue(String::class.java)
                    val descripcion = snapshot.child("descripcion").getValue(String::class.java)
                    val precio = snapshot.child("precio").getValue(Double::class.java)
                    val img = snapshot.child("filepath").getValue(String::class.java)

                    // Verificar si los valores no son nulos
                    if (nombre != null && descripcion != null && precio != null) {
                        // Agregar el producto a la lista de datos
                        dataList.add(Productos(nombre, descripcion, precio,img))
                    }
                }

                // Crear y configurar el adaptador del RecyclerView con la lista de datos
                val adapter = AdaptadorProductos(dataList)
                recyclerView.adapter = adapter
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = requireActivity().intent.extras
        val email = bundle?.getString("Mail")

        val txtMail = view.findViewById<TextView>(R.id.txtmails)
        txtMail.text = "Email: $email"
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
}