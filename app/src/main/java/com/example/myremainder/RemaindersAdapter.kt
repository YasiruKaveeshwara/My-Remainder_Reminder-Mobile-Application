package com.example.myremainder

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RemaindersAdapter (private var remainders: List<Remainder>, context: Context): RecyclerView.Adapter<RemaindersAdapter.RemainderViewHolder>() {

    private val db: RemainderDbHelper = RemainderDbHelper(context)
    private val scope = CoroutineScope(Dispatchers.Main)




    class RemainderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.remainderCardTitle)
        val time: TextView = itemView.findViewById(R.id.remainderCardTime)
        val date: TextView = itemView.findViewById(R.id.remainderCardDate)
        val meridian: TextView = itemView.findViewById(R.id.remainderCardMeridian)
        val active: Switch = itemView.findViewById(R.id.remainderCardStatusButton)
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
        holder.meridian.text = remainder.meridian

        // Remove the setOnCheckedChangeListener
        holder.active.setOnCheckedChangeListener(null)

        // Set the isChecked status
        holder.active.isChecked = remainder.active == "true"

        // Re-apply the setOnCheckedChangeListener
        holder.active.setOnCheckedChangeListener { _, isChecked ->
            val updatedRemainder = Remainder(remainder.id, remainder.title, remainder.content, remainder.time, remainder.date, remainder.meridian, remainder.repeat, if (isChecked) "true" else "false")

            scope.launch {
                db.updateRemainder(updatedRemainder)
                refreshData(db.getAllRemainders())
            }

            val message = if (isChecked) "Activated" else "Deactivated"
            Toast.makeText(holder.itemView.context, message, Toast.LENGTH_SHORT).show()
        }

        // Set click listener on the remainder_card layout
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, UpdateRemainderActivity::class.java).apply {
                putExtra("id", remainder.id)
            }

            holder.itemView.context.startActivity(intent)
        }


        // Set long click listener on the remainder_card layout
        holder.itemView.setOnLongClickListener {
            // Create an AlertDialog
            AlertDialog.Builder(it.context)
                .setTitle("Delete Remainder")
                .setMessage("Are you sure you want to delete this remainder?")
                .setPositiveButton("Yes") { _, _ ->
                    // Delete the remainder if the user confirms
                    scope.launch {
                        db.deleteRemainder(remainder.id)
                        refreshData(db.getAllRemainders())
                    }
                    Toast.makeText(holder.itemView.context, "Remainder deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No", null)
                .show()

            true // Return true to indicate that the long click was handled
        }
    }

    fun refreshData(newRemainders: List<Remainder>){
        remainders = newRemainders
        notifyDataSetChanged()
    }
}