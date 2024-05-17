package com.cad.proyectofinaletps1

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PresupuestoAdapter(private val items: List<PresupuestoItem>, private val context: Context) : RecyclerView.Adapter<PresupuestoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemTextView: TextView = itemView.findViewById(R.id.itemTextView)
        val verButton: Button = itemView.findViewById(R.id.btnPresupuestoVer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.presupuesto_card_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemTextView.text = item.text

        holder.verButton.setOnClickListener {
            val intent = Intent(context, ComparativaPresupuesto::class.java)
            intent.putExtra("nombre", item.text)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}