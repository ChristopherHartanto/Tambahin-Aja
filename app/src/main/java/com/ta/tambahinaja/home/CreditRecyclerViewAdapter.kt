package com.ta.tambahinaja.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.ta.tambahinaja.R
import org.jetbrains.anko.sdk27.coroutines.onClick

class CreditRecyclerViewAdapter(
        private val context: Context,
        private val creditShopItems: List<CreditShop>,
        private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<CreditRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_credit, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(listener,position,creditShopItems[position],context)
    }

    override fun getItemCount(): Int = creditShopItems.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val desc = view.findViewById<TextView>(R.id.tvCreditDesc)
        private val title = view.findViewById<TextView>(R.id.tvCreditTitle)
        private val buy = view.findViewById<Button>(R.id.btnBuyCredit)

        fun bindItem(listener: (position: Int) -> Unit,position: Int,creditShop: CreditShop, context: Context) {

            val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)
            desc.typeface = typeface
            title.typeface = typeface

            desc.text = creditShop.price.toString()
            title.text = creditShop.title

            itemView.onClick {
                listener(position)
            }
            buy.onClick {
                listener(position)
            }
        }

    }
}