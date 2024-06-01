package com.cad.proyectofinaletps1.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cad.proyectofinaletps1.R
import com.cad.proyectofinaletps1.databinding.ActivityMainBinding
import com.cad.proyectofinaletps1.databinding.FragmentBarcodeBinding
import com.cad.proyectofinaletps1.models.Productos
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.zxing.integration.android.IntentIntegrator

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [barcodeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class barcodeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentBarcodeBinding? = null
    private val binding get() = _binding!!

    private var scannedBarcode: String? = null
    private var productKey: String? = null


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
        _binding = FragmentBarcodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnScan.setOnClickListener { initScanner() }

        binding.btnRed.setOnClickListener {
            if (scannedBarcode != null && productKey != null) {
                val detallesFragment = DetallesProductoFragment.newInstance(scannedBarcode!!, productKey!!)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, detallesFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), "No se ha escaneado ningún producto", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initScanner() {
        val intentIntegrator = IntentIntegrator.forSupportFragment(this)
        intentIntegrator
            .setOrientationLocked(false)
            .setPrompt("Escanee un producto: ")
            .initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val txtscan = view?.findViewById<TextView>(R.id.txtBarcode)
        val linearlayo = view?.findViewById<LinearLayout>(R.id.linearProd)
        val txtNom = view?.findViewById<TextView>(R.id.txtNprod)
        val txtDesc = view?.findViewById<TextView>(R.id.txtDescrip)
        val txtprice = view?.findViewById<TextView>(R.id.txtPreciop)
        val imvProd = view?.findViewById<ImageView>(R.id.imvSProd)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Cancelado", Toast.LENGTH_LONG).show()
            } else {
                scannedBarcode = result.contents
                val database = FirebaseDatabase.getInstance()
                val ref = database.getReference("productos")
                ref.orderByChild("barcode").equalTo(scannedBarcode)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                linearlayo?.visibility = View.VISIBLE
                                for (productSnapshot in dataSnapshot.children) {
                                    if (productSnapshot.child("barcode").getValue(String::class.java) == scannedBarcode) {
                                        val producto = productSnapshot.getValue(Productos::class.java)
                                        productKey = productSnapshot.key
                                        // Actualiza la interfaz de usuario con los datos del producto encontrado
                                        txtNom?.text = "Producto: " + (producto?.nombre ?: "Producto no encontrado")
                                        txtprice?.text = "Precio: $" + (producto?.precio?.toString() ?: "-")
                                        txtscan?.text = "Código de Barras: $scannedBarcode"
                                        imvProd?.let {
                                            Glide.with(requireContext())
                                                .load(productSnapshot.child("filepath").getValue(String::class.java))
                                                .into(it)
                                        }

                                        return  // Termina la ejecución después de encontrar el producto
                                    }
                                }
                            } else {
                                Toast.makeText(requireContext(), "Producto no encontrado", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Manejar errores si la consulta es cancelada
                        }
                    })
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment barcodeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            barcodeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}