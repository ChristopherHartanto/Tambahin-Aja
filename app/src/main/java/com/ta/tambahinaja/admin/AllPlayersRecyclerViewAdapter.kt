package com.ta.tambahinaja.admin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.ta.tambahinaja.R
import com.ta.tambahinaja.model.HighScore
import com.ta.tambahinaja.model.User
import com.ta.tambahinaja.utils.getFacebookProfilePicture
import com.squareup.picasso.Picasso
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.sdk27.coroutines.onClick

class AllPlayersRecyclerViewAdapter(private val context: Context, private val items: List<AllPlayer>,
                                    private val listener: (id: String) -> Unit)
    : RecyclerView.Adapter<AllPlayersRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_all_players, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position], position, listener)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val credit = view.findViewById<TextView>(R.id.tvPlayerCredit)
        private val name = view.findViewById<TextView>(R.id.tvPlayerName)
        private val image = view.findViewById<ImageView>(R.id.ivPlayer)

        fun bindItem(allPlayer: AllPlayer, position: Int, listener: (id: String) -> Unit) {

            credit.text = "Rp ${allPlayer.credit.toString()}"
            name.text = allPlayer.name.toString()

            Picasso.get().load(getFacebookProfilePicture(allPlayer.facebookId.toString())).into(image)

            itemView.onClick {
                listener(allPlayer.uid.toString())
            }
        }
    }
}