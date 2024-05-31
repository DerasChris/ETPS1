package com.cad.proyectofinaletps1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView

class ProductosHistorialAdapter(private val items: List<ProductoHistorialItem>, private val eliminarClickListener: (String) -> Unit) : RecyclerView.Adapter<ProductosHistorialAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombreProductoHistorial)
        val txtCantidad: TextView = itemView.findViewById(R.id.txtCantidadProductoHistorial)
        val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecioProductoHistorial)
        val eliminarButton: AppCompatImageButton = itemView.findViewById(R.id.btn_eliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.producto_historial_card_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.txtNombre.text = item.nombre
        holder.txtCantidad.text = item.cantidad.toString()
        holder.txtPrecio.text = item.precio.toString()

        holder.eliminarButton.setOnClickListener {
            // Obtener la clave del producto en la posición actual
            val claveProducto = item.productoKey
            // Llamar al listener pasando la clave del producto
            eliminarClickListener(claveProducto)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

