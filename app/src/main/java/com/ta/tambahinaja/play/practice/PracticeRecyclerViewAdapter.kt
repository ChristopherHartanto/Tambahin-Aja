package com.ta.tambahinaja.play.practice

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.ta.tambahinaja.R
import org.jetbrains.anko.backgroundResource


class PracticeRecyclerViewAdapter(private val context: Context, private val items: List<Practice>, private val currentLevel: Int,
                                  private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<PracticeRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_practice, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bindItem(items[position],currentLevel, listener, holder.layoutPosition, context)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val clickAnimation = AlphaAnimation(1.2F,0.6F)
        private val title = view.findViewById<TextView>(R.id.tvPracticeTitle)
        private val level = view.findViewById<TextView>(R.id.tvPracticeLevelNumber)
        private val priceContent = view.findViewById<TextView>(R.id.tvPracticePriceContent)
        private val price = view.findViewById<TextView>(R.id.tvPracticePrice)
        private val reward = view.findViewById<TextView>(R.id.tvPracticeReward)
        private val rewardContent = view.findViewById<TextView>(R.id.tvPracticeRewardContent)
        private val cvPractice = view.findViewById<CardView>(R.id.cvPractice)

        fun bindItem(practice: Practice,currentLevel: Int, listener: (position: Int) -> Unit,position: Int, context: Context) {
            val size = calculateSizeOfView(context)

            val layoutParams = GridLayout.LayoutParams(ViewGroup.LayoutParams(size - 40, size-20))
            val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)

            title.typeface = typeface
            level.typeface = typeface
            priceContent.typeface = typeface
            price.typeface = typeface
            reward.typeface = typeface
            rewardContent.typeface = typeface

            level.text = practice.level.toString()
            priceContent.text = practice.price.toString()
            rewardContent.text = practice.reward.toString()

            itemView.setOnClickListener{
                itemView.startAnimation(clickAnimation)
                listener(position)
            }
//
            if (practice.level!! % 6 == 0)
                cvPractice.backgroundResource = R.color.colorGrey

            if (currentLevel > practice.level!!)
                cvPractice.backgroundResource = R.color.colorWhite

            if (currentLevel > practice.level!!)
                cvPractice.backgroundResource = R.color.colorWhite
            //itemView.layoutParams = layoutParams
        }

        private fun calculateSizeOfView(context: Context): Int {


            val displayMetrics = context.resources.displayMetrics
            val dpWidth = displayMetrics.widthPixels
            return (dpWidth / 2)
        }

    }
}