package com.example.balapplat.rank

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.balapplat.R
import com.example.balapplat.model.HighScore
import com.example.balapplat.model.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_rank.*
import org.jetbrains.anko.support.v4.ctx


class RankRecyclerViewAdapter(private val context: Context, private val items: List<ChooseGame>,private val listener: (position: Int) -> Unit)
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
        private val energy = view.findViewById<TextView>(R.id.tvGameEnergy)
        private val payGame = view.findViewById<TextView>(R.id.tvPayGame)
        private val llPayGame = view.findViewById<LinearLayout>(R.id.layout_pay_game)

        fun bindItem(chooseGame: ChooseGame, listener: (position: Int) -> Unit,position: Int, context: Context) {
                    val size = calculateSizeOfView(context)

            val layoutParams = GridLayout.LayoutParams(ViewGroup.LayoutParams(size - 40, size-20))
            val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)

            name.typeface = typeface
            score.typeface = typeface
            energy.typeface = typeface
            payGame.typeface = typeface
            score.text = ""+chooseGame.score
            name.text = chooseGame.title
            energy.text = ""+chooseGame.energy
            //score.text = "" + highScore.score

            if (position == 2)
                llPayGame.visibility = View.VISIBLE

            itemView.setOnClickListener{
                listener(position)

            }
            //itemView.layoutParams = layoutParams
        }

        fun calculateSizeOfView(context: Context): Int {


            val displayMetrics = context.resources.displayMetrics
            val dpWidth = displayMetrics.widthPixels
            return (dpWidth / 2)
        }

    }
}