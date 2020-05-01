package com.example.balapplat.friends

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.balapplat.R
import com.example.balapplat.model.User
import com.squareup.picasso.Picasso
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource

class FriendsRecyclerViewAdapter(private val context: Context, private val items: List<User>, private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<FriendsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_friend, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],listener,position)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val active = view.findViewById<TextView>(R.id.tvActive)
        private val name = view.findViewById<TextView>(R.id.tvFriendName)
        private val image = view.findViewById<ImageView>(R.id.ivFriendProfile)

        fun bindItem(items: User,listener: (position: Int) -> Unit, position: Int) {
//            if (items.active == false)
//                active.backgroundResource = R.color.colorGrey
//            else
//                active.backgroundColorResource = R.color.colorPrimary

            name.text = items.name
            items.facebookId?.let { Picasso.get().load(getFacebookProfilePicture(items.facebookId!!)).fit().into(image) }

            itemView.setOnClickListener{
                listener(position)
            }
        }
        fun getFacebookProfilePicture(userID: String): String {
            return "https://graph.facebook.com/$userID/picture?type=large"
        }

    }
}