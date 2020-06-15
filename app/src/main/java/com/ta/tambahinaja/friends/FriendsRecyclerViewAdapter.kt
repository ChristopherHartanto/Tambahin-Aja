package com.ta.tambahinaja.friends

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.thunder413.datetimeutils.DateTimeStyle
import com.ta.tambahinaja.R
import com.ta.tambahinaja.model.User
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import com.github.thunder413.datetimeutils.DateTimeUtils
import com.ta.tambahinaja.utils.getFacebookProfilePicture

class FriendsRecyclerViewAdapter(private val context: Context, private val items: List<User>, private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<FriendsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_friend, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],listener,holder.layoutPosition, context)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val online = view.findViewById<TextView>(R.id.tvOnline)
        private val name = view.findViewById<TextView>(R.id.tvFriendName)
        private val lastOnline = view.findViewById<TextView>(R.id.tvFriendLastOnline)
        private val image = view.findViewById<ImageView>(R.id.ivFriendProfile)

        fun bindItem(items: User,listener: (position: Int) -> Unit, position: Int, context: Context) {

            if (items.online != null){
                online.text = "Online"
                lastOnline.text = ""
            }
            else if(items.lastOnline?.toInt() ?: 0 != 0){
                online.text = ""
                val a = DateTimeUtils.formatDate(items.lastOnline!! + 7 * 3600 * 1000)
                lastOnline.text = "last online : ${DateTimeUtils.formatWithPattern(a,"dd MMM hh:mm:ss a")}"
                //lastOnline.text = "last online : ${items.lastOnline?.let { getDate(it) }}"
            }

            if (items.online == null)
                online.text = ""
            if (items.lastOnline!!.toInt() == 0)
                lastOnline.text = ""

            val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)
            name.typeface = typeface
            online.typeface = typeface
            lastOnline.typeface = typeface

            name.text = items.name

            if (items.facebookId != "")
                items.facebookId?.let { Picasso.get().load(getFacebookProfilePicture(items.facebookId!!)).fit().into(image) }

            itemView.setOnClickListener{
                listener(position)
            }
        }


        fun getDate(time: Long) : String{
            val sdf = SimpleDateFormat("dd MMM hh:mm:ss a")
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(Date(time).time * 1000L)
        }

    }
}