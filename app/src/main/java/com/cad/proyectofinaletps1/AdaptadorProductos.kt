package com.cad.proyectofinaletps1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cad.proyectofinaletps1.models.Productos

class AdaptadorProductos(private val dataList: List<Productos>) : RecyclerView.Adapter<AdaptadorProductos.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.txtNombre)
        val descriptionTextView: TextView = itemView.findViewById(R.id.txtDesc)
        val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecio)
        val txtImg: ImageView = itemView.findViewById(R.id.imv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardproducts, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataList[position]
        holder.titleTextView.text = currentItem.nombre
        holder.descriptionTextView.text = currentItem.descripcion
        holder.txtPrecio.text ="$ "+ currentItem.precio.toString()

        Glide.with(holder.itemView.context)
            .load(currentItem.imgurl)
            .apply(RequestOptions().override(300, 300)) // Suponiendo que tienes una propiedad llamada "urlImagen" en tu modelo Productos
            .into(holder.txtImg)

    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
