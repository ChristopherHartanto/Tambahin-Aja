package com.example.balapplat.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.balapplat.R
import com.example.balapplat.model.HighScore
import com.example.balapplat.model.User
import com.squareup.picasso.Picasso
import org.jetbrains.anko.backgroundResource

class CreditHistoryRecyclerViewAdapter(private val context: Context, private val creditHistoryItems: List<CreditHistory>)
    : RecyclerView.Adapter<CreditHistoryRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_credit_history, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(creditHistoryItems[position],context)
    }

    override fun getItemCount(): Int = creditHistoryItems.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val info = view.findViewById<TextView>(R.id.tvCreditHistoryInfo)
        private val date = view.findViewById<TextView>(R.id.tvCreditHistoryDate)

        fun bindItem(creditHistory: CreditHistory, context: Context) {

            val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)
            info.typeface = typeface
            date.typeface = typeface

            info.text = creditHistory.info
            date.text = creditHistory.date

        }

    }
}