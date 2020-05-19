package com.example.tambahinaja.rank

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tambahinaja.R


class RankRecyclerViewAdapter(private val context: Context, private val items: List<ChooseGame>,
                              private val availableGameList: List<Boolean>,private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<RankRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_rank, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bindItem(availableGameList[position],items[position], listener, position, context)
    }

    override fun getItemCount(): Int {
        return if (availableGameList.size == items.size)
            availableGameList.size
        else
            0
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val background = view.findViewById<FrameLayout>(R.id.layout_rank_background)
        private val score = view.findViewById<TextView>(R.id.tvRankScore)
        private val name = view.findViewById<TextView>(R.id.tvRankName)
        private val image = view.findViewById<ImageView>(R.id.ivRank)
        private val energy = view.findViewById<TextView>(R.id.tvGameEnergy)
        private val payGame = view.findViewById<TextView>(R.id.tvPayGame)
        private val llPayGame = view.findViewById<LinearLayout>(R.id.layout_pay_game)

        fun bindItem(availableGame: Boolean,chooseGame: ChooseGame, listener: (position: Int) -> Unit,position: Int, context: Context) {
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
            when(position){
                0 -> {
                    background.setBackgroundResource(R.color.colorNormal)
                    image.setImageResource(R.drawable.normal_game)
                }
                1 -> {
                    background.setBackgroundResource(R.color.colorOddEven)
                    image.setImageResource(R.drawable.odd_even_game)
                }
                2 -> {
                    background.setBackgroundResource(R.color.colorRush)
                    image.setImageResource(R.drawable.rush_game)
                }
                3 -> {
                    background.setBackgroundResource(R.color.colorAlphaNum)
                    image.setImageResource(R.drawable.alpha_num_game)
                }
            }
            if (position > 0){
                if (!availableGame) {
                    payGame.text = "${chooseGame.priceGame}"
                    llPayGame.visibility = View.VISIBLE
                }else
                    llPayGame.visibility = View.GONE
            }


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