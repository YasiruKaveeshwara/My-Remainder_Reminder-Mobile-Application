package com.example.myremainder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RemaindersAdapter (private var remainders: List<Remainder>, context: Context): RecyclerView.Adapter<RemaindersAdapter.RemainderViewHolder>() {

    class RemainderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.remainderCardTitle)
        val time: TextView = itemView.findViewById(R.id.remainderCardTime)
        val date: TextView = itemView.findViewById(R.id.remainderCardDate)
        val active: TextView = itemView.findViewById(R.id.remainderCardActiveButton)
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
    }

    fun refreshData(newRemainders: List<Remainder>){
        remainders = newRemainders
        notifyDataSetChanged()
    }
}