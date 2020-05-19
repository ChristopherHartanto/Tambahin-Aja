package com.example.tambahinaja.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tambahinaja.R
import org.jetbrains.anko.sdk27.coroutines.onClick

class SettingRecyclerViewAdapter(private val context: Context, private val settingItems: List<Setting>,
                                 private val listener: (position : Int) -> Unit)
    : RecyclerView.Adapter<SettingRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_setting, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(settingItems[position],context, listener, position)
    }

    override fun getItemCount(): Int = settingItems.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val clickAnimation = AlphaAnimation(1.2F,0.6F)
        private val desc = view.findViewById<TextView>(R.id.tvSettingDesc)
        private val title = view.findViewById<TextView>(R.id.tvSettingTitle)

        fun bindItem(setting: Setting, context: Context, listener: (position: Int) -> Unit, position: Int) {
            val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)
            desc.typeface = typeface
            title.typeface = typeface

            if (setting.desc == "")
                desc.visibility = View.GONE
            else
                desc.text = setting.desc

            title.text = setting.title

            itemView.onClick {
                itemView.startAnimation(clickAnimation)
                listener(position)
            }
        }

    }
}