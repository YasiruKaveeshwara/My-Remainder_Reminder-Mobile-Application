package com.example.myremainder

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class RemaindersAdapter (private var remainders: List<Remainder>, context: Context): RecyclerView.Adapter<RemaindersAdapter.RemainderViewHolder>() {

    private val db: RemainderDbHelper = RemainderDbHelper(context)
    class RemainderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.remainderCardTitle)
        val time: TextView = itemView.findViewById(R.id.remainderCardTime)
        val date: TextView = itemView.findViewById(R.id.remainderCardDate)
        val active: TextView = itemView.findViewById(R.id.remainderCardStatusButton)
        val updateButton: ImageView = itemView.findViewById(R.id.editButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemainderViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.remainder_item, parent, false)
    return RemainderViewHolder(view)
}

    override fun getItemCount(): Int = remainders.size

    override fun onBindViewHolder(holder: RemainderViewHolder, position: Int) {
        val remainder = remainders[position]
        holder.title.text = remainder.title
        holder.time.text = remainder.time
        holder.date.text = remainder.date
        holder.active.text = remainder.active

        holder.updateButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, UpdateRemainderActivity::class.java).apply {
                putExtra("id", remainder.id)
            }

            holder.itemView.context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            db.deleteRemainder(remainder.id)
            refreshData(db.getAllRemainders())
            Toast.makeText(holder.itemView.context, "Remainder deleted", Toast.LENGTH_SHORT).show()
        }
    }

    fun refreshData(newRemainders: List<Remainder>){
        remainders = newRemainders
        notifyDataSetChanged()
    }
}