package com.example.balapplat.rank

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.example.balapplat.play.CountdownActivity
import com.example.balapplat.view.MainView
import com.example.balapplat.presenter.Presenter
import com.example.balapplat.R
import com.example.balapplat.home.MarketActivity
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
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.pop_up_task.*
import kotlinx.android.synthetic.main.row_rank.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx

class RankActivity : AppCompatActivity(), NetworkConnectivityListener, MainView {

    private lateinit var adapter: RankRecyclerViewAdapter
    private lateinit var taskAdapter: TaskRecyclerViewAdapter
    private lateinit var rankDetailAdapter: RankDetailRecyclerViewAdapter
    private lateinit var database: DatabaseReference
    lateinit var presenter: Presenter
    private lateinit var auth: FirebaseAuth
    lateinit var data: Inviter
    private lateinit var popupWindow : PopupWindow
    private val items : MutableList<ChooseGame> = mutableListOf()
    private val rankDetailItems : MutableList<String> = mutableListOf()

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
        tvTotalScore.typeface = typeface
        Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivProfile)
        fetchScore()
        val clickAnimation = AlphaAnimation(1.2F,0.6F)

        ivTask.onClick {
            ivTask.startAnimation(clickAnimation)
            popUpTask()
        }

        layout_rank_detail.onClick {
            popUpRankDetail()
        }

        layout_point_energy.onClick {
            startActivity<MarketActivity>()
        }

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

    private fun popUpTask(){
        val inflater: LayoutInflater = this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.pop_up_task,null)


        // Initialize a new instance of popup window
        popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
                LinearLayout.LayoutParams.MATCH_PARENT// Window height
        )

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }

        activity_rank.alpha = 0.1F
        val btnTask = view.findViewById<Button>(R.id.btnTask)
        val taskTitle = view.findViewById<TextView>(R.id.tvTaskTitle)
        val taskNextRank = view.findViewById<TextView>(R.id.tvTaskNextRank)
        val rvTask = view.findViewById<RecyclerView>(R.id.rvTask)
        val typeface = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)

        taskTitle.typeface = typeface
        taskNextRank.typeface = typeface
        btnTask.typeface = typeface

        taskAdapter = TaskRecyclerViewAdapter(this)
        rvTask.layoutManager = LinearLayoutManager(this)
        rvTask.adapter = taskAdapter

        btnTask.onClick {
            activity_rank.alpha = 1F
            popupWindow.dismiss()
        }

        // Finally, show the popup window on app
        TransitionManager.beginDelayedTransition(activity_rank)
        popupWindow.showAtLocation(
                activity_rank, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

    }

    private fun popUpRankDetail(){
        val inflater: LayoutInflater = this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.pop_up_rank_detail,null)


        // Initialize a new instance of popup window
        popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
                LinearLayout.LayoutParams.MATCH_PARENT// Window height
        )

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }

        activity_rank.alpha = 0.1F
        val btnRankDetail = view.findViewById<Button>(R.id.btnRankDetail)
        val rankTitle = view.findViewById<TextView>(R.id.tvRankTitle)
        val rvRankDetail = view.findViewById<RecyclerView>(R.id.rvRankDetail)
        val typeface = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)

        rankTitle.typeface = typeface
        btnRankDetail.typeface = typeface

        rankTitle.text = "Beginner"
        rankDetailItems.clear()
        rankDetailItems.add("Get 10 Extra Credit when Solve Puzzle")
        rankDetailItems.add("Energy Limit 110")
        rankDetailItems.add("Extra 2 Point in Rank Mode")

        rankDetailAdapter = RankDetailRecyclerViewAdapter(this, rankDetailItems)
        rvRankDetail.layoutManager = LinearLayoutManager(this)
        rvRankDetail.adapter = rankDetailAdapter

        btnRankDetail.onClick {
            activity_rank.alpha = 1F
            popupWindow.dismiss()
        }

        // Finally, show the popup window on app
        TransitionManager.beginDelayedTransition(activity_rank)
        popupWindow.showAtLocation(
                activity_rank, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

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
