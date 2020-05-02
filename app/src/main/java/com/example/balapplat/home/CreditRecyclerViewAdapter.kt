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

class CreditRecyclerViewAdapter(private val context: Context)
    : RecyclerView.Adapter<CreditRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_credit, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(position,context)
    }

    override fun getItemCount(): Int = 3

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val desc = view.findViewById<TextView>(R.id.tvCreditDesc)
        private val title = view.findViewById<TextView>(R.id.tvCreditTitle)

        fun bindItem(position: Int, context: Context) {

            val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)
            desc.typeface = typeface
            title.typeface = typeface

            when (position) {
                0 -> {
                    desc.text = "7000"
                    title.text = "Isi Pulsa 5000 ke Semua Operator"
                }
                1 -> {
                    desc.text = "11000"
                    title.text = "Isi Pulsa 10000 ke Semua Operator"
                }
                2 ->{
                    desc.text = "19000"
                    title.text = "Isi Pulsa 20000 ke Semua Operator"
                }
            }
        }

    }
}