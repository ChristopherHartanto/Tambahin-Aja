package com.ta.tambahinaja

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProfileRecyclerViewAdapter(private val items: List<String>)
    : RecyclerView.Adapter<ProfileRecyclerViewAdapter.ViewHolder>() {
    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        private val tvRowHistory = view.findViewById<TextView>(R.id.tvRowHistory)
        fun bindItem(item: String) {
            tvRowHistory.text = item
        }
    }


    override fun getItemCount(): Int =
        items.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_history_profile, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position])
    }
}