package com.example.balapplat.rank

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

class TaskRecyclerViewAdapter(private val context: Context)
    : RecyclerView.Adapter<TaskRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_task, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(position,context)
    }

    override fun getItemCount(): Int = 4

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val desc = view.findViewById<TextView>(R.id.tvTaskDesc)
        private val progress = view.findViewById<TextView>(R.id.tvTaskProgress)

        fun bindItem(position: Int, context: Context) {
            val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)
            desc.typeface = typeface
            progress.typeface = typeface

            desc.text = "Get Total Point 500"
            progress.text = "150/500"
        }

    }
}