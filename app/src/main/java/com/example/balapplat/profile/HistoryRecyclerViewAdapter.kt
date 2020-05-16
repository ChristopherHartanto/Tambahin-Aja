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

class HistoryRecyclerViewAdapter(private val context: Context, private val historyItems: List<History>
                                     , private val user: User)
    : RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_history, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(historyItems[position],user, position, context)
    }

    override fun getItemCount(): Int = historyItems.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val playerName = view.findViewById<TextView>(R.id.tvPlayerName)
        private val playerPoint = view.findViewById<TextView>(R.id.tvPlayerPoint)
        private val playerImage = view.findViewById<ImageView>(R.id.ivPlayerImage)
        private val opponentPoint = view.findViewById<TextView>(R.id.tvOpponentPoint)
        private val opponentName = view.findViewById<TextView>(R.id.tvOpponentName)
        private val opponentImage = view.findViewById<ImageView>(R.id.ivOpponentImage)
        private val status = view.findViewById<TextView>(R.id.tvHistoryStatus)

        fun bindItem(history: History, profileItems: User, position: Int, context: Context) {
            val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)
            playerName.typeface = typeface
            playerPoint.typeface = typeface
            status.typeface = typeface
            opponentName.typeface = typeface
            opponentPoint.typeface = typeface

            playerName.text = profileItems.name
            playerPoint.text = history.point.toString()
            opponentName.text = history.opponentName
            opponentPoint.text = history.opponentPoint.toString()
            status.text = history.status

            profileItems.facebookId?.let { Picasso.get().load(getFacebookProfilePicture(profileItems.facebookId!!)).fit().into(playerImage) }
            profileItems.facebookId?.let { Picasso.get().load(getFacebookProfilePicture(history.opponentFacebookId!!)).fit().into(opponentImage) }
        }

        fun getFacebookProfilePicture(userID: String): String {
            return "https://graph.facebook.com/$userID/picture?type=large"
        }
    }
}