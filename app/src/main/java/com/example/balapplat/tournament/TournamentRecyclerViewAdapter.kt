package com.example.balapplat.tournament

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

class TournamentRecyclerViewAdapter(private val context: Context, private val tournamentParticipants: List<TournamentParticipant>)
    : RecyclerView.Adapter<TournamentRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_leaderboard, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(tournamentParticipants[position],position, context)
    }

    override fun getItemCount(): Int = tournamentParticipants.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val count = view.findViewById<TextView>(R.id.tvCountLeader)
        private val name = view.findViewById<TextView>(R.id.tvLeader)
        private val score = view.findViewById<TextView>(R.id.tvScoreLeader)
        private val image = view.findViewById<ImageView>(R.id.ivLeader)

        fun bindItem(tournamentParticipants: TournamentParticipant,position: Int, context: Context) {
            val counter = position + 1
            val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)
            name.typeface = typeface
            score.typeface = typeface

            when (position) {
                0 -> count.backgroundResource = R.drawable.first_medal
                1 -> count.backgroundResource = R.drawable.second_medal
                2 -> count.backgroundResource = R.drawable.third_medal
                else -> count.text = "" + counter + ". "
            }

            name.text = tournamentParticipants.name
            score.text = "" + tournamentParticipants.point
            tournamentParticipants.facebookId?.let { Picasso.get().load(getFacebookProfilePicture(tournamentParticipants.facebookId!!)).fit().into(image) }
        }

        fun getFacebookProfilePicture(userID: String): String {
            return "https://graph.facebook.com/$userID/picture?type=large"
        }
    }
}