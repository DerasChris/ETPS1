package com.cad.proyectofinaletps1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductosHistorialAdapter(private val items: List<ProductoHistorialItem>) : RecyclerView.Adapter<ProductosHistorialAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombreProductoHistorial)
        val txtCantidad: TextView = itemView.findViewById(R.id.txtCantidadProductoHistorial)
        val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecioProductoHistorial)
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
    }

    override fun getItemCount(): Int {
        return items.size
    }
}