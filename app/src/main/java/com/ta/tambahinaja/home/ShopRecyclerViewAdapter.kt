package com.ta.tambahinaja.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.ta.tambahinaja.R
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast

class ShopRecyclerViewAdapter(
        private val context: Context,
        private val items: List<SkuDetails>,
        private val itemClicked: (SkuDetails) -> Unit
)
    : RecyclerView.Adapter<ShopRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_market, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(position,items[position],itemClicked,context)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val buy = view.findViewById<TextView>(R.id.btnShopItemBuy)
        private val desc = view.findViewById<TextView>(R.id.tvShopItemDesc)
        private val image = view.findViewById<ImageView>(R.id.ivShopItem)

        fun bindItem(position: Int,item: SkuDetails, itemClicked: (SkuDetails) -> Unit, context: Context) {
            val counter = position + 1
            val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)
            desc.typeface = typeface
            buy.typeface = typeface

            desc.text = item.title
            buy.text = item.price

            when (item.title) {
                "500 Coin" -> image.backgroundResource = R.drawable.coin
                "1000 Coin" -> image.backgroundResource = R.drawable.money_bag
                "2000 Coin" -> image.backgroundResource = R.drawable.treasure_box
                "Energy Limit 300" -> image.backgroundResource = R.drawable.lightning2
                "Energy Limit 200" -> image.backgroundResource = R.drawable.lightning1
                "Fulling Energy to Limit" -> image.backgroundResource = R.drawable.lightning
            }
//            when (position) {
//                0 -> {
//                    desc.text = "Fulling Energy to Limit"
//                    image.backgroundResource = R.drawable.lightning
//                    buy.text = "Rp 5.900"
//                }
//                1 -> {
//                    desc.text = "Energy Limit to 200"
//                    image.backgroundResource = R.drawable.lightning1
//                    buy.text = "Rp 19.900"
//                }
//                2 ->{
//                    desc.text = "Energy Limit to 300"
//                    image.backgroundResource = R.drawable.lightning2
//                    buy.text = "Rp 32.900"
//                }
//                3 -> {
//                    desc.text = "500 Coin"
//                    image.backgroundResource = R.drawable.coin
//                    buy.text = "Rp 24.900"
//                }
//                4 -> {
//                    desc.text = "1000 Coin"
//                    image.backgroundResource = R.drawable.money_bag
//                    buy.text = "Rp 35.900"
//                }
//                5 ->{
//                    desc.text = "2000 Coin"
//                    image.backgroundResource = R.drawable.treasure_box
//                    buy.text = "Rp 49.900"
//                }
//            }

            buy.onClick {
                itemClicked(item)
            }

            itemView.onClick {
                itemClicked(item)
            }

        }

    }
}