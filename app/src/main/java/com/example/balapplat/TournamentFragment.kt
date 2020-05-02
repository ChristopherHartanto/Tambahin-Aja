package com.example.balapplat

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.balapplat.leaderboard.LeaderBoardRecyclerViewAdapter
import com.example.balapplat.leaderboard.Leaderboard
import com.example.balapplat.model.HighScore
import com.example.balapplat.model.User
import com.example.balapplat.utils.showSnackBar
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.fragment_tournament.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.ctx


class Tournament : Fragment(), NetworkConnectivityListener {

    private var items: MutableList<HighScore> = mutableListOf()
    private var profileItems: MutableList<User> = mutableListOf()
    private lateinit var database: DatabaseReference

    private lateinit var adapter: LeaderBoardRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tournament, container, false)
    }

    fun retrieve(){
        items.clear()
        profileItems.clear()
        GlobalScope.launch {
            val postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    //toast("" + p0.children)
                    fetchData(p0)
                }

            }
            database.child("leaderboards").orderByChild("total").addListenerForSingleValueEvent(postListener)

        }
    }

    fun fetchData(dataSnapshot: DataSnapshot){

        for (ds in dataSnapshot.children) {
            val score = ds.getValue(Leaderboard::class.java)!!.total
            val id = ds.key



            id?.let { retrieveUser(it,score) }
        }
    }

    fun retrieveUser(id : String,score: Int?){
        GlobalScope.launch {
            val postListener = object : ValueEventListener {
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

    override fun onStart() {
        database = FirebaseDatabase.getInstance().reference
        adapter = LeaderBoardRecyclerViewAdapter(ctx,items,profileItems)

        rvStanding.layoutManager = LinearLayoutManager(ctx)
        rvStanding.adapter = adapter

        items.clear()
        profileItems.clear()

        val typeface = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
        tvTournamentTitle.typeface = typeface
        tvStandingTitle.typeface = typeface

        fetchTournament()
        super.onStart()
    }

    fun fetchDataUser(dataSnapshot: DataSnapshot){

        val item = dataSnapshot.getValue(User::class.java)!!

        profileItems.add(item)
        adapter.notifyDataSetChanged()
    }

    fun fetchTournament(){
        GlobalScope.launch {
            val postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        loadData(p0,true)
                    }else
                        loadData(p0,false)
                }

            }
            database.child("tournament").addListenerForSingleValueEvent(postListener)

        }
    }

    fun loadData(dataSnapshot: DataSnapshot, status: Boolean){
//        val data = dataSnapshot.getValue(TournamentData::class.java)
//
//        if (data != null && status) {
//            tvTournamentTitle.text = data.title
//            tvTournamentDesc.text = data.description
//            tvTournamentTimeLeft.text = data.deadLine
//
//            retrieve()
////            GlobalScope.launch {
////                val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
////                val currentDate = sdf.format(Date())
////
////                Duration.
////                val diff: Long = Date.from() - date2.getTime()
////                val seconds = diff / 1000
////                val minutes = seconds / 60
////                val hours = minutes / 60
////                val days = hours / 24
////            }
//        }
//        else{
//            tvTournamentTitle.text = "No Tournament Right Now"
//            tvTournamentDesc.text = ""
//            tvTournamentTimeLeft.text = ""
//        }
    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(fragment_home, "The network is back !", "LONG")
                } else {
                    showSnackBar(fragment_home, "There is no more network", "INFINITE")
                }
            }
        }
    }
}

data class TournamentData(
    var Reward1: Long? = 0,
    var Reward2: Long? = 0,
    var Reward3: Long? = 0,
    var description: String? = "",
    var deadLine: String? = "",
    var title: String? = ""
)
