package com.example.training.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.training.R
import kotlinx.android.synthetic.main.activity_home.view.*

class RecyclerViewAdapter (val context: Context, val items: List<String>, val itemImages: List<Int>)
    : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolderA>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolderA{
        return ViewHolderA(LayoutInflater.from(context).inflate(R.layout.item_rows, parent, false))
    }


    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolderA, position: Int) {
        holder.bindItem(items[position], itemImages[position])
    }

    class ViewHolderA(view: View) : RecyclerView.ViewHolder(view){

        val ivItem = view.findViewById<ImageView>(R.id.ivItem)
        val tvitem = view.findViewById<TextView>(R.id.tvItem)

        fun bindItem(nama: String, image: Int){
            tvitem.text = nama
            ivItem.setBackgroundResource(image)
        }
    }
}