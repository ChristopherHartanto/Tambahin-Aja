package com.example.balapplat.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.balapplat.R
import com.example.balapplat.model.HighScore
import com.example.balapplat.model.User
import com.squareup.picasso.Picasso
import org.jetbrains.anko.backgroundResource

class SettingRecyclerViewAdapter(private val context: Context, private val settingItems: List<Setting>)
    : RecyclerView.Adapter<SettingRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_setting, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(settingItems[position],context)
    }

    override fun getItemCount(): Int = settingItems.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val desc = view.findViewById<TextView>(R.id.tvSettingDesc)
        private val title = view.findViewById<TextView>(R.id.tvSettingTitle)

        fun bindItem(setting: Setting, context: Context) {
            val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)
            desc.typeface = typeface
            title.typeface = typeface

            if (setting.desc == "")
                desc.visibility = View.GONE
            else
                desc.text = setting.desc

            title.text = setting.title
        }

    }
}