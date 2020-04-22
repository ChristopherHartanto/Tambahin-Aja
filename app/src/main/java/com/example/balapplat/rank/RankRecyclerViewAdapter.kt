package com.example.balapplat.rank

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.balapplat.R
import com.example.balapplat.model.HighScore
import com.example.balapplat.model.User
import com.squareup.picasso.Picasso


class RankRecyclerViewAdapter(private val context: Context, private val items: List<String>,private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<RankRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_rank, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bindItem(items[position], listener, position, context)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val score = view.findViewById<TextView>(R.id.tvRankScore)
        private val name = view.findViewById<TextView>(R.id.tvRankName)
        private val image = view.findViewById<ImageView>(R.id.ivRank)

        fun bindItem(rankName: String, listener: (position: Int) -> Unit,position: Int, context: Context) {
                    val size = calculateSizeOfView(context)

            val layoutParams = GridLayout.LayoutParams(ViewGroup.LayoutParams(size - 40, size-20))

            name.text = rankName
            //score.text = "" + highScore.score
            itemView.setOnClickListener{
                listener(position)

            }
            itemView.layoutParams = layoutParams
        }

        fun calculateSizeOfView(context: Context): Int {


            val displayMetrics = context.resources.displayMetrics
            val dpWidth = displayMetrics.widthPixels
            return (dpWidth / 2) // COLUMN_COUNT would be 4 in your case
        }

    }
}