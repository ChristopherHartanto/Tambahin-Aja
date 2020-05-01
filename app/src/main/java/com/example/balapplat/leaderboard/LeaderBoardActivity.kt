package com.example.balapplat.leaderboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.balapplat.presenter.Presenter
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_leader_board.*
import com.example.balapplat.R
import com.example.balapplat.model.HighScore
import com.example.balapplat.model.Inviter
import com.example.balapplat.model.User
import com.example.balapplat.play.CountdownActivity
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.example.balapplat.utils.showSnackBar
import com.example.balapplat.view.MainView
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.activity_leader_board.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*

class LeaderBoardActivity : AppCompatActivity(), NetworkConnectivityListener,
    MainView {

    private var items: MutableList<HighScore> = mutableListOf()
    private var profileItems: MutableList<User> = mutableListOf()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    lateinit var data: Inviter
    lateinit var presenter: Presenter

    private lateinit var adapter: LeaderBoardRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leader_board)

        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        adapter = LeaderBoardRecyclerViewAdapter(this,items,profileItems)
        presenter = Presenter(this, database)
        presenter.receiveInvitation()

        rvLeaderBoard.layoutManager = LinearLayoutManager(this)
        rvLeaderBoard.adapter = adapter

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
            database.child("leaderboards").orderByChild("total").addListenerForSingleValueEvent(postListener)

        }
    }

    fun fetchData(dataSnapshot: DataSnapshot){
        var count = dataSnapshot.childrenCount
        for ((index,ds) in dataSnapshot.children.withIndex()) {

            if (auth.currentUser != null){
                if (ds.key.equals(auth.currentUser!!.uid)){
                    tvLeaderboardInfo.text = "#$count " + auth.currentUser!!.displayName +" "+ ds.getValue(Leaderboard::class.java)!!.total
                }
            }
            count--
            val total = ds.getValue(Leaderboard::class.java)!!.total
            val id = ds.key

            id?.let { retrieveUser(it,total) }
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
            database.child("users").child(id).addListenerForSingleValueEvent(postListener)

        }
    }

    fun fetchDataUser(dataSnapshot: DataSnapshot){

        val item = dataSnapshot.getValue(User::class.java)!!

        profileItems.add(item)
        adapter.notifyDataSetChanged()
    }

    override fun loadData(dataSnapshot: DataSnapshot) {
        data = dataSnapshot.getValue(Inviter::class.java)!!

        alert(data!!.name + " invite you to play"){
            title = "Invitation"
            yesButton {
                presenter.replyInvitation(true)
            }
            noButton {
                presenter.replyInvitation(false)
            }
        }.show()
    }

    override fun response(message: String) {
        if (message === "acceptedGame"){
            toast("acceptedGame")

            startActivity(intentFor<CountdownActivity>("inviterFacebookId" to data.facebookId,
                "inviterName" to data.name))
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

    override fun onStart() {
        items.clear()
        profileItems.clear()
        retrieve()
        super.onStart()
}

}

data class Leaderboard(
    var normal: Int? = 0,
    var oddEven: Int? = 0,
    var rush: Int? = 0,
    var alphaNum: Int? = 0,
    var total:Int? = 0
)
