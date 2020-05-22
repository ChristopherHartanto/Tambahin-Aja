package com.ta.tambahinaja.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.ta.tambahinaja.R
import com.ta.tambahinaja.rank.AvailableGame
import org.jetbrains.anko.sdk27.coroutines.onClick

class CustomGameRecyclerViewAdapter(private val context: Context,
                                    private val availableGame: List<Boolean>,
                                    private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<CustomGameRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_choose_game, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(listener,position,availableGame[position])
    }

    override fun getItemCount(): Int = availableGame.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val image = view.findViewById<ImageView>(R.id.ivCustomGame)
        private val not_available = view.findViewById<LinearLayout>(R.id.layout_custom_game_not_available)

        fun bindItem(listener: (position: Int) -> Unit, position: Int, availableGame: Boolean) {

            when(position){
                0 -> image.setImageResource(R.drawable.normal_game)
                1 -> image.setImageResource(R.drawable.odd_even_game)
                2 -> image.setImageResource(R.drawable.rush_game)
                3 -> image.setImageResource(R.drawable.alpha_num_game)
                4 -> image.setImageResource(R.drawable.mix_game)
                5 -> image.setImageResource(R.drawable.double_attack_game)
            }

            if (!availableGame)
                not_available.visibility = View.VISIBLE
            else
                not_available.visibility = View.GONE

            itemView.onClick {
                listener(position)
            }
        }

    }
}