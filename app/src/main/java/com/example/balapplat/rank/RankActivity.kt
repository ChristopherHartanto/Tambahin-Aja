package com.example.balapplat.rank

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.balapplat.play.CountdownActivity
import com.example.balapplat.view.MainView
import com.example.balapplat.presenter.Presenter
import com.example.balapplat.R
import com.example.balapplat.model.Inviter
import com.facebook.Profile
import com.example.balapplat.utils.showSnackBar
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.activity_rank.*
import kotlinx.android.synthetic.main.activity_rank.ivProfile
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.row_rank.*
import org.jetbrains.anko.*

class RankActivity : AppCompatActivity(), NetworkConnectivityListener, MainView {

    private lateinit var mAdView : AdView
    private lateinit var adapter: RankRecyclerViewAdapter
    private lateinit var database: DatabaseReference
    lateinit var presenter: Presenter
    private lateinit var auth: FirebaseAuth
    lateinit var data: Inviter
    private val items : MutableList<ChooseGame> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rank)

        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance().reference
        presenter = Presenter(this, database)
        presenter.receiveInvitation()
        auth = FirebaseAuth.getInstance()

        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvRank.typeface = typeface
        tvPoint.typeface = typeface
        tvEnergy.typeface = typeface
        tvPayGame.typeface = typeface
        tvTotalScore.typeface = typeface
        Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivProfile)
        fetchScore()

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        adapter = RankRecyclerViewAdapter(this,items){
            finish()
            when (it) {
                0 -> {
                    startActivity(intentFor<CountdownActivity>("mode" to "single",
                        "type" to "normal","rank" to true))
                }
                1 -> {
                    startActivity(intentFor<CountdownActivity>("mode" to "single",
                        "type" to "oddEven","rank" to true))
                }
                2 -> {
                    startActivity(intentFor<CountdownActivity>("mode" to "single",
                        "type" to "rush","rank" to true))
                }
                3 -> {
                    startActivity(intentFor<CountdownActivity>("mode" to "single",
                        "type" to "alphaNum","rank" to true))
                }
            }

        }

        rvRank.layoutManager = LinearLayoutManager(this)
        rvRank.adapter = adapter
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
                    showSnackBar(activity_rank, "The network is back !", "LONG")
                } else {
                    showSnackBar(activity_rank, "There is no more network", "INFINITE")
                }
            }
        }
    }

    fun fetchScore(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                loadBestScore(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        database.child("leaderboards").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(postListener)
    }

    fun loadBestScore(dataSnapshot: DataSnapshot){

        if (dataSnapshot.exists()){
            tvTotalScore.text = "" + dataSnapshot.getValue(LeaderBoard::class.java)!!.total

            items.add(ChooseGame("Normal", 15,dataSnapshot.getValue(LeaderBoard::class.java)!!.normal))
            items.add(ChooseGame("Odd Even", 20,dataSnapshot.getValue(LeaderBoard::class.java)!!.oddEven))
            items.add(ChooseGame("Rush", 25,dataSnapshot.getValue(LeaderBoard::class.java)!!.rush))
            items.add(ChooseGame("AlphaNum", 30,dataSnapshot.getValue(LeaderBoard::class.java)!!.alphaNum))
        }else{
            tvTotalScore.text = "" + 0

            items.add(ChooseGame("Normal", 15,0))
            items.add(ChooseGame("Odd Even", 20,0))
            items.add(ChooseGame("Rush", 25,0))
            items.add(ChooseGame("AlphaNum", 30,0))
        }


        adapter.notifyDataSetChanged()
}

    fun getFacebookProfilePicture(userID: String): String {
        return "https://graph.facebook.com/$userID/picture?type=large"
    }
}

data class LeaderBoard(
    var total: Int? = 0,
    var normal: Int? = 0,
    var oddEven: Int? = 0,
    var rush: Int? = 0,
    var alphaNum: Int? = 0
)

data class ChooseGame(
    var title: String? = "",
    var energy: Int? = 0,
    var score: Int? = 0
)
