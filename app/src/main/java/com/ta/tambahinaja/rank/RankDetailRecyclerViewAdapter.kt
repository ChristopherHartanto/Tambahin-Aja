package com.ta.tambahinaja.rank

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.ta.tambahinaja.R

class RankDetailRecyclerViewAdapter(private val context: Context, private val rankDetailItems: List<String>)
    : RecyclerView.Adapter<RankDetailRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_rank_detail, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(rankDetailItems[position],context)
    }

    override fun getItemCount(): Int = rankDetailItems.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val info = view.findViewById<TextView>(R.id.tvRankDetailInfo)

        fun bindItem(rankDetailItems: String, context: Context) {
            val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)
            info.typeface = typeface

            info.text = rankDetailItems
        }

    }
}