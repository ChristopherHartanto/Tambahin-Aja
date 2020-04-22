package com.example.balapplat.leaderboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.balapplat.R
import com.example.balapplat.model.HighScore
import com.example.balapplat.model.User
import com.squareup.picasso.Picasso

class LeaderBoardRecyclerViewAdapter(private val context: Context, private val scoreItems: List<HighScore>
                                     , private val profileItems: List<User>)
    : RecyclerView.Adapter<LeaderBoardRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_leaderboard, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(scoreItems[position],profileItems[position], position)
    }

    override fun getItemCount(): Int = scoreItems.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val count = view.findViewById<TextView>(R.id.tvCountLeader)
        private val name = view.findViewById<TextView>(R.id.tvLeader)
        private val score = view.findViewById<TextView>(R.id.tvScoreLeader)
        private val image = view.findViewById<ImageView>(R.id.ivLeader)

        fun bindItem(highScore: HighScore, profileItems: User, position: Int) {
            var counter = position + 1
            count.text = "" + counter + ". "
            name.text = profileItems.name
            score.text = "" + highScore.score
            profileItems.facebookId?.let { Picasso.get().load(getFacebookProfilePicture(profileItems.facebookId!!)).fit().into(image) }
        }

        fun getFacebookProfilePicture(userID: String): String {
            return "https://graph.facebook.com/$userID/picture?type=large"
        }
    }
}