package com.example.balapplat.friends

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.balapplat.R
import com.example.balapplat.model.User
import com.squareup.picasso.Picasso
import org.jetbrains.anko.textColor

class AddFriendRecyclerViewAdapter(private val context: Context, private val items: List<User>,
                                   private val statuses: List<Boolean>, private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<AddFriendRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_addfriend, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],statuses[position], listener,position, context)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val name = view.findViewById<TextView>(R.id.tvAddFriendName)
        private val status = view.findViewById<TextView>(R.id.tvAddFriendStatus)
        private val image = view.findViewById<ImageView>(R.id.ivAddFriendProfile)

        fun bindItem(items: User, statusFriend: Boolean ,listener: (position: Int) -> Unit, position: Int, context: Context) {

            val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)
            name.typeface = typeface
            status.typeface = typeface

            name.text = items.name
            items.facebookId?.let { Picasso.get().load(getFacebookProfilePicture(items.facebookId!!)).fit().into(image) }

            if (statusFriend){
                status.text = "Friend"
            }
//            else {
//                status.text = "Add Friend +"
//                status.textColor = R.color.colorPrimary
//            }


            status.setOnClickListener{
                listener(position)
            }
        }

        fun getFacebookProfilePicture(userID: String): String {
            return "https://graph.facebook.com/$userID/picture?type=large"
        }
    }
}