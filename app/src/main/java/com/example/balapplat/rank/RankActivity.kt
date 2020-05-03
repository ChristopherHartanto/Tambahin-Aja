package com.example.balapplat.rank

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
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
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_rank.*
import kotlinx.android.synthetic.main.activity_rank.ivProfile
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.pop_up_task.*
import kotlinx.android.synthetic.main.row_choose_game.*
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
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)
    private val items : MutableList<ChooseGame> = mutableListOf()
    private val availableGameList : MutableList<Boolean> = mutableListOf()
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
        fetchBalance()
        fetchGameAvailable()
        val clickAnimation = AlphaAnimation(1.2F,0.6F)

        ivTask.onClick {
            ivTask.startAnimation(clickAnimation)
            popUpTask()
        }

        layout_rank_detail.onClick {
            popUpRankDetail()
        }

        layout_point_energy.onClick {
            finish()
            startActivity<MarketActivity>()
        }

        adapter = RankRecyclerViewAdapter(this,items,availableGameList){
            if (!availableGameList[it]){
                if (items[it].priceGame!! < tvPoint.text.toString().toInt())
                    popUpMessage(1,"Do You Want to Buy?",it)
                else
                    popUpMessage(2,"Not Enough Money",it)
            }else{
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
                    4 ->{
                        startActivity(intentFor<CountdownActivity>("mode" to "single",
                                "type" to "mix","rank" to true))
                    }
                    5 ->{
                        startActivity(intentFor<CountdownActivity>("mode" to "single",
                                "type" to "doubleAttack","rank" to true))
                    }
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

    fun fetchBalance(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if (dataSnapshot.exists()){
                    tvEnergy.text = "${dataSnapshot.getValue(Balance::class.java)!!.energy}/${dataSnapshot.getValue(Balance::class.java)!!.energyLimit}"
                    tvPoint.text = "${dataSnapshot.getValue(Balance::class.java)!!.point}"
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        database.child("users").child(auth.currentUser!!.uid).child("balance").addListenerForSingleValueEvent(postListener)
    }

    fun fetchGameAvailable(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if (dataSnapshot.exists()){
                    availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.normal!!)
                    availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.oddEven!!)
                    availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.rush!!)
                    availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.alphaNum!!)
                    availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.mix!!)
                    availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.doubleAttack!!)

                    adapter.notifyDataSetChanged()
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        database.child("users").child(auth.currentUser!!.uid).child("availableGame").addListenerForSingleValueEvent(postListener)
    }

    fun buyGame(position: Int){
        var gameName = ""
        val point = tvPoint.text.toString().toLong() - items[position].priceGame!!
        when(position){
            1 -> gameName = "oddEven"
            2 -> gameName = "rush"
            3 -> gameName = "alphaNum"
            4 -> gameName = "mix"
            5 -> gameName = "doubleAttack"
        }
        database.child("users").child(auth.currentUser!!.uid).child("availableGame").child(gameName).setValue(true).addOnFailureListener {
            popUpMessage(2, it.message.toString(),0)
        }
        database.child("users").child(auth.currentUser!!.uid).child("balance").child("point").setValue(point).addOnFailureListener {
            popUpMessage(2, it.message.toString(),0)
        }.addOnSuccessListener {
            popUpMessage(2,"Success Buy this Game",0)
            fetchBalance()
            fetchGameAvailable()
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

            items.add(ChooseGame("Normal", 15,dataSnapshot.getValue(LeaderBoard::class.java)!!.normal,0))
            items.add(ChooseGame("Odd Even", 18,dataSnapshot.getValue(LeaderBoard::class.java)!!.oddEven,100))
            items.add(ChooseGame("Rush", 25,dataSnapshot.getValue(LeaderBoard::class.java)!!.rush,300))
            items.add(ChooseGame("AlphaNum", 20,dataSnapshot.getValue(LeaderBoard::class.java)!!.alphaNum,500))
            items.add(ChooseGame("Mix", 28,dataSnapshot.getValue(LeaderBoard::class.java)!!.mix,800))
            items.add(ChooseGame("Double Attack", 25,dataSnapshot.getValue(LeaderBoard::class.java)!!.doubleAttack,1000))
        }else{
            tvTotalScore.text = "" + 0

            items.add(ChooseGame("Normal", 15,0,0))
            items.add(ChooseGame("Odd Even", 20,0,100))
            items.add(ChooseGame("Rush", 25,0,300))
            items.add(ChooseGame("AlphaNum", 30,0,500))
            items.add(ChooseGame("Mix", 28,0,800))
            items.add(ChooseGame("Double Attack", 25,0,1000))
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

    private fun popUpMessage(type: Int,message: String, position: Int){
        val inflater: LayoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.pop_up_message,null)

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
        val typeface : Typeface? = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)

        val layoutMessageInvitation = view.findViewById<LinearLayout>(R.id.layout_message_invitation)
        val layoutMessageBasic = view.findViewById<LinearLayout>(R.id.layout_message_basic)
        val layoutMessageReward = view.findViewById<LinearLayout>(R.id.layout_message_reward)
        val tvMessageInfo = view.findViewById<TextView>(R.id.tvMessageInfo)
        val btnClose = view.findViewById<Button>(R.id.btnMessageClose)
        val btnReject = view.findViewById<Button>(R.id.btnMessageReject)
        val tvMessageTitle = view.findViewById<TextView>(R.id.tvMessageTitle)

        if (type == 1){
            layoutMessageInvitation.visibility = View.GONE
            layoutMessageBasic.visibility = View.VISIBLE
            layoutMessageReward.visibility = View.GONE

            btnReject.text = "No"
            tvMessageInfo.text = message

            btnClose.onClick {
                buyGame(position)
                btnClose.startAnimation(clickAnimation)
                activity_rank.alpha = 1F
                popupWindow.dismiss()
            }

            btnReject.onClick {
                btnReject.startAnimation(clickAnimation)
                activity_rank.alpha = 1F
                popupWindow.dismiss()
            }

        }
        else if(type == 2){
            layoutMessageInvitation.visibility = View.GONE
            layoutMessageBasic.visibility = View.VISIBLE
            layoutMessageReward.visibility = View.GONE

            btnReject.visibility = View.GONE
            tvMessageInfo.text = message

            btnClose.onClick {
                btnClose.startAnimation(clickAnimation)
                activity_rank.alpha = 1F
                popupWindow.dismiss()
            }
        }

        tvMessageTitle.typeface = typeface

        activity_rank.alpha = 0.1F

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
    var alphaNum: Int? = 0,
    var mix: Int? = 0,
    var doubleAttack: Int? = 0
)

data class ChooseGame(
    var title: String? = "",
    var energy: Int? = 0,
    var score: Int? = 0,
    var priceGame: Long? = 0
)

data class Balance(
    var credit: Int? = 0,
    var energy: Int? = 0,
    var energyLimit: Int? = 0,
    var point: Int? = 0
)

data class AvailableGame(
    var normal: Boolean? = true,
    var rush: Boolean? = false,
    var oddEven: Boolean? = false,
    var alphaNum: Boolean? = false,
    var mix: Boolean? = false,
    var doubleAttack: Boolean? = false
)