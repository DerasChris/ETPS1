package com.cad.proyectofinaletps1

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class PresupuestoAdapter(private val items: List<PresupuestoItem>, private val context: Context) : RecyclerView.Adapter<PresupuestoAdapter.ViewHolder>() {

    // Define the alternating colors
    private val colors = arrayOf(
        context.getColor(R.color.color1), // Color definido en colors.xml
        context.getColor(R.color.color2)  // Color definido en colors.xml
    )

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemTextView: TextView = itemView.findViewById(R.id.itemTextView)
        val verButton: Button = itemView.findViewById(R.id.btnPresupuestoVer)
        val cardView: CardView = itemView as CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.presupuesto_card_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemTextView.text = item.text

        // Set the background color based on the position
        holder.cardView.setCardBackgroundColor(colors[position % colors.size])

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
