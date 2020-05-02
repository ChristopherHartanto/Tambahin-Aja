package com.example.balapplat.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.balapplat.R
import com.example.balapplat.model.HighScore
import com.example.balapplat.model.User
import com.squareup.picasso.Picasso
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.sdk27.coroutines.onClick

class CustomGameRecyclerViewAdapter(private val context: Context,private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<CustomGameRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_choose_game, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(listener,position,context)
    }

    override fun getItemCount(): Int = 4

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val image = view.findViewById<ImageView>(R.id.ivCustomGame)

        fun bindItem(listener: (position: Int) -> Unit, position: Int, context: Context) {
            itemView.onClick {
                listener(position)
            }
        }

    }
}