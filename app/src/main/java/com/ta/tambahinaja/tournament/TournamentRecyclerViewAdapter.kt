package com.ta.tambahinaja.tournament

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.ta.tambahinaja.R
import com.squareup.picasso.Picasso
import com.ta.tambahinaja.utils.getFacebookProfilePicture
import org.jetbrains.anko.backgroundResource

class TournamentRecyclerViewAdapter(private val context: Context, private val tournamentParticipants: List<TournamentParticipant>)
    : RecyclerView.Adapter<TournamentRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_leaderboard, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(tournamentParticipants[position],holder.layoutPosition, tournamentParticipants.size, context)
    }

    override fun getItemCount(): Int = tournamentParticipants.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val count = view.findViewById<TextView>(R.id.tvCountLeader)
        private val name = view.findViewById<TextView>(R.id.tvLeader)
        private val score = view.findViewById<TextView>(R.id.tvScoreLeader)
        private val image = view.findViewById<ImageView>(R.id.ivLeader)

        fun bindItem(tournamentParticipants: TournamentParticipant,position: Int, size: Int, context: Context) {
            val counter = size - position
            val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)
            name.typeface = typeface
            score.typeface = typeface
            count.typeface = typeface

            when (counter) {
                1-> {
                    count.backgroundResource = R.drawable.first_medal
                    count.text = ""
                }
                2 -> {
                    count.backgroundResource = R.drawable.second_medal
                    count.text = ""
                }
                3 -> {
                    count.backgroundResource = R.drawable.third_medal
                    count.text = ""
                }
                else -> {
                    count.backgroundResource = R.color.colorTransparent
                    count.text = "" + counter + ". "
                }
            }

            name.text = tournamentParticipants.name
            score.text = "" + tournamentParticipants.point
            if (tournamentParticipants.facebookId != "")
                tournamentParticipants.facebookId?.let { Picasso.get().load(getFacebookProfilePicture(tournamentParticipants.facebookId!!)).fit().into(image) }
        }
    }
}