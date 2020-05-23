package com.ta.tambahinaja.leaderboard

import android.content.Context
import android.content.SharedPreferences
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
import com.ta.tambahinaja.R
import com.ta.tambahinaja.model.HighScore
import com.ta.tambahinaja.model.Inviter
import com.ta.tambahinaja.model.User
import com.ta.tambahinaja.play.CountdownActivity
import com.ta.tambahinaja.rank.LeaderBoard
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.ta.tambahinaja.utils.showSnackBar
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.onRefresh

class LeaderBoardActivity : AppCompatActivity(), NetworkConnectivityListener {

    private var items: MutableList<HighScore> = mutableListOf()
    private var profileItems: MutableList<User> = mutableListOf()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    lateinit var data: Inviter
    private lateinit var typeface: Typeface
    private var loadingCount = 4
    private var name = ""
    private lateinit var loadingTimer : CountDownTimer
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: LeaderBoardRecyclerViewAdapter
    private lateinit var postListener : ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leader_board)

        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        adapter = LeaderBoardRecyclerViewAdapter(this,items,profileItems)
        rvLeaderBoard.adapter = adapter

        sharedPreferences = this.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)
        name = sharedPreferences.getString("name","").toString()
        if (name == "" && Profile.getCurrentProfile() != null)
            name = Profile.getCurrentProfile().name
        typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)!!
        tvLeaderboardInfo.typeface = typeface
        tvLeaderboardTitle.typeface = typeface

        postListener = object :  ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

            }

        }

        srLeaderboard.onRefresh {
            loadingTimer()
            layout_loading.visibility = View.VISIBLE
            retrieve()
        }

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        rvLeaderBoard.layoutManager = linearLayoutManager
        if (auth.currentUser != null && Profile.getCurrentProfile() != null)
            Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivLeaderboard)
        else{
            ivLeaderboard.visibility = View.GONE
            tvLeaderboardInfo.visibility = View.GONE
        }
        ivLeaderboard.backgroundResource = R.drawable.button_bg_round
    }

    private fun retrieve(){
        GlobalScope.launch {
            postListener = object :  ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists())
                        fetchData(p0)
                }

            }
            database.child("leaderboards").orderByChild("total").limitToFirst(50).addValueEventListener(postListener)
            database.keepSynced(true)
        }
    }

    fun fetchData(dataSnapshot: DataSnapshot){
        items.clear()
        profileItems.clear()

        dataSnapshot.children.sortedBy {
            it.getValue(LeaderBoard::class.java)!!.total
        }
        var count = 0
        for ((index,ds) in dataSnapshot.children.withIndex()) {
            val total = ds.getValue(Leaderboard::class.java)!!.total
            items.add(HighScore(total))
            if (auth.currentUser != null){
                if (ds.key.equals(auth.currentUser!!.uid)){
                    tvLeaderboardInfo.text = "#${dataSnapshot.childrenCount - count} " + name +" "+ ds.getValue(Leaderboard::class.java)!!.total
                }
            }
            count++
        }

        for (ds in dataSnapshot.children) {
            Thread.sleep(20)
            val id = ds.key
            id?.let { retrieveUser(it) }
        }
    }

    private fun retrieveUser(id : String){
        val postListener = object :  ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                    fetchDataUser(p0)
            }

        }
        database.child("users").child(id).addListenerForSingleValueEvent(postListener)
    }

    fun fetchDataUser(dataSnapshot: DataSnapshot){

        val item = dataSnapshot.getValue(User::class.java)!!
        profileItems.add(item)

        if (profileItems.size == items.size){
            srLeaderboard.isRefreshing = false
            adapter.notifyDataSetChanged()
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

    private fun loadingTimer(){
        val view = findViewById<View>(R.id.layout_loading)

        val tvLoadingTitle = view.findViewById<TextView>(R.id.tvLoadingTitle)
        val tvLoadingInfo = view.findViewById<TextView>(R.id.tvLoadingInfo)

        tvLoadingInfo.text = "Be Number One!"

        tvLoadingInfo.typeface = typeface
        tvLoadingTitle.typeface = typeface


        loadingTimer = object: CountDownTimer(12000, 1000) {
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
                tvLoadingTitle.text = "No Data"
                tvLoadingInfo.text = ""
                toast("Try Again Later")
            }
        }
        loadingTimer.start()
    }


    override fun onStart() {
        loadingTimer()
        retrieve()
        super.onStart()
    }

    override fun onPause() {
        items.clear()
        profileItems.clear()
        adapter.notifyDataSetChanged()
        database.removeEventListener(postListener)
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
