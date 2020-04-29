package com.example.balapplat.leaderboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.balapplat.R
import com.example.balapplat.model.HighScore
import com.example.balapplat.model.User
import com.example.balapplat.utils.UtilsConstants
import com.example.balapplat.utils.showSnackBar
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.activity_leader_board.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.toast

class LeaderBoardActivity : AppCompatActivity(), NetworkConnectivityListener {

    private var items: MutableList<HighScore> = mutableListOf()
    private var profileItems: MutableList<User> = mutableListOf()
    private lateinit var database: DatabaseReference

    private lateinit var adapter: LeaderBoardRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leader_board)

        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance().reference
        adapter = LeaderBoardRecyclerViewAdapter(this,items,profileItems)

        rvLeaderBoard.layoutManager = LinearLayoutManager(this)
        rvLeaderBoard.adapter = adapter

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
            database.child("highscore").orderByChild("score").addValueEventListener(postListener)

        }
    }

    fun fetchData(dataSnapshot: DataSnapshot){

        for (ds in dataSnapshot.children) {
            val score = ds.getValue(HighScore::class.java)!!.score
            val id = ds.key



            id?.let { retrieveUser(it,score) }
        }
        toast("" + items)
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
            database.child("users").child(id).addValueEventListener(postListener)

        }
    }

    override fun onStart() {
        items.clear()
        profileItems.clear()
        retrieve()
        super.onStart()
    }

    fun fetchDataUser(dataSnapshot: DataSnapshot){

        val item = dataSnapshot.getValue(User::class.java)!!

        profileItems.add(item)
        adapter.notifyDataSetChanged()
    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(
                        activity_leaderboard,
                        "Connection Established",
                        UtilsConstants.SNACKBAR_LONG
                    ).show()
                } else {
                    showSnackBar(
                        activity_leaderboard,
                        "No Network !",
                        UtilsConstants.SNACKBAR_INFINITE
                    ).show()
                }
            }
        }
    }
}
