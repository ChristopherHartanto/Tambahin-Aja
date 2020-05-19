package com.example.tambahinaja.leaderboard

import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_leader_board.*
import com.example.tambahinaja.R
import com.example.tambahinaja.model.HighScore
import com.example.tambahinaja.model.Inviter
import com.example.tambahinaja.model.User
import com.example.tambahinaja.play.CountdownActivity
import com.example.tambahinaja.rank.LeaderBoard
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.example.tambahinaja.utils.showSnackBar
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*

class LeaderBoardActivity : AppCompatActivity(), NetworkConnectivityListener {

    private var items: MutableList<HighScore> = mutableListOf()
    private var profileItems: MutableList<User> = mutableListOf()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    lateinit var data: Inviter
    private lateinit var typeface: Typeface
    private var loadingCount = 4
    private lateinit var loadingTimer : CountDownTimer
    private lateinit var adapter: LeaderBoardRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leader_board)


        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        adapter = LeaderBoardRecyclerViewAdapter(this,items,profileItems)

        typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)!!
        tvLeaderboardInfo.typeface = typeface
        tvLeaderboardTitle.typeface = typeface
        loadingTimer()

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        rvLeaderBoard.layoutManager = linearLayoutManager

        Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivLeaderboard)
        ivLeaderboard.backgroundResource = R.drawable.button_bg_round
    }

    fun retrieve(){
        items.clear()
        profileItems.clear()
        GlobalScope.launch {
            val postListener = object :  ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    //toast("" + p0.children)
                    fetchData(p0)
                }

            }
            database.child("leaderboards").orderByChild("total").limitToFirst(50).addListenerForSingleValueEvent(postListener)

        }
    }

    fun fetchData(dataSnapshot: DataSnapshot){
        dataSnapshot.children.sortedBy {
            it.getValue(LeaderBoard::class.java)!!.total
        }

        var count = 0

        for ((index,ds) in dataSnapshot.children.withIndex()) {
            if (auth.currentUser != null){
                if (ds.key.equals(auth.currentUser!!.uid)){
                    tvLeaderboardInfo.text = "#${dataSnapshot.childrenCount - count} " + auth.currentUser!!.displayName +" "+ ds.getValue(Leaderboard::class.java)!!.total
                }
            }
            count++
            Thread.sleep(5)
            val total = ds.getValue(Leaderboard::class.java)!!.total
            val id = ds.key

            id?.let { retrieveUser(it,total) }
        }
    }

    fun retrieveUser(id : String,score: Int?){
        GlobalScope.launch {
            val postListener = object :  ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    items.add(HighScore(score))
                    //toast("" + p0.children)
                    fetchDataUser(p0)
                }

            }
            database.child("users").child(id).addListenerForSingleValueEvent(postListener)

        }
    }

    fun fetchDataUser(dataSnapshot: DataSnapshot){

        val item = dataSnapshot.getValue(User::class.java)!!
        profileItems.add(item)

        if (profileItems.size == items.size){
            rvLeaderBoard.adapter = adapter
            loadingTimer.cancel()
            layout_loading.visibility = View.GONE
        }
    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(activity_leaderboard, "The network is back !", "LONG")
                } else {
                    showSnackBar(activity_leaderboard, "There is no more network", "INFINITE")
                }
            }
        }
    }

    fun getFacebookProfilePicture(userID: String): String {
        return "https://graph.facebook.com/$userID/picture?type=large"
    }

    fun loadingTimer(){
        val view = findViewById<View>(R.id.layout_loading)

        val tvLoadingTitle = view.findViewById<TextView>(R.id.tvLoadingTitle)
        val tvLoadingInfo = view.findViewById<TextView>(R.id.tvLoadingInfo)

        tvLoadingInfo.text = "Be Number One!"

        tvLoadingInfo.typeface = typeface
        tvLoadingTitle.typeface = typeface


        loadingTimer = object: CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                when (loadingCount) {
                    3 -> tvLoadingTitle.text = "Fetching Data ."
                    2 -> tvLoadingTitle.text = "Fetching Data . ."
                    1 -> tvLoadingTitle.text = "Fetching Data . . ."
                    else -> loadingCount = 4
                }
                loadingCount--
            }

            override fun onFinish() {
                finish()
                toast("Oops Something Wrongs!")
            }
        }
        loadingTimer.start()
    }

    override fun onStart() {
        retrieve()
        super.onStart()
    }

    override fun onPause() {
        items.clear()
        profileItems.clear()
        adapter.notifyDataSetChanged()
        loadingTimer.cancel()
        super.onPause()
    }


}

data class Leaderboard(
    var normal: Int? = 0,
    var oddEven: Int? = 0,
    var rush: Int? = 0,
    var alphaNum: Int? = 0,
    var total:Int? = 0
)
