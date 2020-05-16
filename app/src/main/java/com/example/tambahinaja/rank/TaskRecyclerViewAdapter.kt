package com.example.tambahinaja.rank

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tambahinaja.R

class TaskRecyclerViewAdapter(private val context: Context, private val taskList: List<String>,
                              private val taskProgressList: List<String>)
    : RecyclerView.Adapter<TaskRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_task, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(taskList[position],taskProgressList[position],position,context)
    }

    override fun getItemCount(): Int = taskList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val desc = view.findViewById<TextView>(R.id.tvTaskDesc)
        private val progress = view.findViewById<TextView>(R.id.tvTaskProgress)

        fun bindItem(task: String, taskprogress: String,position: Int, context: Context) {
            val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)
            desc.typeface = typeface
            progress.typeface = typeface

            desc.text = task
            progress.text = taskprogress
        }

    }
}