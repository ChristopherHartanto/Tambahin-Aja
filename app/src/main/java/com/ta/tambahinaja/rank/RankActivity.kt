package com.ta.tambahinaja.rank

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Message
import android.text.Layout
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.ta.tambahinaja.play.CountdownActivity
import com.ta.tambahinaja.view.MainView
import com.ta.tambahinaja.presenter.Presenter
import com.ta.tambahinaja.R
import com.ta.tambahinaja.home.MarketActivity
import com.ta.tambahinaja.main.MainActivity
import com.ta.tambahinaja.model.Inviter
import com.ta.tambahinaja.play.GameType
import com.ta.tambahinaja.play.StatusPlayer
import com.ta.tambahinaja.presenter.RankPresenter
import com.facebook.Profile
import com.github.thunder413.datetimeutils.DateTimeUtils
import com.ta.tambahinaja.utils.showSnackBar
import com.ta.tambahinaja.view.RankView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.activity_rank.*
import kotlinx.android.synthetic.main.activity_rank.btnInfo
import kotlinx.android.synthetic.main.activity_rank.ivProfile
import kotlinx.android.synthetic.main.activity_rank.tvEnergy
import kotlinx.android.synthetic.main.activity_rank.tvPoint
import kotlinx.android.synthetic.main.fragment_tournament.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class RankActivity : AppCompatActivity(), NetworkConnectivityListener,RankView {

    private lateinit var sharedPreference: SharedPreferences
    private lateinit var adapter: RankRecyclerViewAdapter
    private lateinit var taskAdapter: TaskRecyclerViewAdapter
    private lateinit var rankDetailAdapter: RankDetailRecyclerViewAdapter
    private lateinit var countDownTimer : CountDownTimer
    private lateinit var database: DatabaseReference
    lateinit var rankPresenter: RankPresenter
    private lateinit var auth: FirebaseAuth
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private var bundle: Bundle = Bundle()
    lateinit var data: Inviter
    private var loadingCount = 4
    private lateinit var loadingTimer : CountDownTimer
    private lateinit var typeface: Typeface
    private var energyTime = 300
    var energy = 0
    var energyLimit = 100
    var point = 0
    private var remainTime = 0
    private var counted = 0
    var position = 0
    var currentRank = "Toddler"
    var levelUp = false
    private var diff: Long = 0
    private var checkUpdateEnergy = false
    lateinit var editor: SharedPreferences.Editor
    private lateinit var popupWindow : PopupWindow
    private var isClicked = false
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)
    private val items : MutableList<ChooseGame> = mutableListOf()
    private val taskList : MutableList<String> = mutableListOf()
    private val taskProgressList : MutableList<String> = mutableListOf()
    private val availableGameList : MutableList<Boolean> = mutableListOf()
    private val rankDetailItems : MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rank)

        supportActionBar?.hide()

        sharedPreference =  this.getSharedPreferences("LOCAL_DATA",Context.MODE_PRIVATE)
        database = FirebaseDatabase.getInstance().reference
        rankPresenter = RankPresenter(this,database)
        auth = FirebaseAuth.getInstance()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        rankDetailAdapter = RankDetailRecyclerViewAdapter(this, rankDetailItems)

        typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)!!
        tvRank.typeface = typeface
        tvPoint.typeface = typeface
        tvEnergy.typeface = typeface
        tvTotalScore.typeface = typeface
        tvrankMainTitle.typeface = typeface

        val clickAnimation = AlphaAnimation(1.2F,0.6F)

        ivTask.onClick {
            ivTask.startAnimation(clickAnimation)
            popUpTask()
        }

        layout_rank_detail.onClick {
            popUpRankDetail()
        }

        cvPointEnergy.onClick {
            if (!isClicked){
                isClicked = true
                cvPointEnergy.startAnimation(clickAnimation)
                startActivity<MarketActivity>()
                finish()
            }
        }

        countDownTimer = object : CountDownTimer(1000,1000){
            override fun onFinish() {

            }

            override fun onTick(millisUntilFinished: Long) {
            }

        }

        adapter = RankRecyclerViewAdapter(this,items,availableGameList){
            if (auth.currentUser == null)
                popUpMessage(2,"You Must Sign In First")
            else{
                position = it
                if (!availableGameList[it]){
                    if (items[it].priceGame!! <= point)
                        popUpMessage(1,"Do You Want to Buy?")
                    else
                        popUpMessage(2,"Not Enough Coins")
                }else{
                    if (items[it].energy!! > energy)
                        popUpMessage(2,"Not Enough Energy")
                    else{
                        position = it
                        popUpTutorial()
                    }

                }
            }
        }

        rvRank.layoutManager = LinearLayoutManager(this)
        rvRank.adapter = adapter

        taskAdapter = TaskRecyclerViewAdapter(this,taskList,taskProgressList)


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

    private fun loadBestScore(dataSnapshot: DataSnapshot){

        if (dataSnapshot.exists()){
            tvTotalScore.text = "" + dataSnapshot.getValue(LeaderBoard::class.java)!!.total

            if (enumValueOf<Rank>(currentRank) == Rank.GrandMaster){
                val progress1 = sharedPreference.getInt("gMaster1",0)
                if (dataSnapshot.getValue(LeaderBoard::class.java)!!.total!!.toInt() > progress1){
                    editor = sharedPreference.edit()
                    editor.putInt("gMaster1",point)
                    editor.apply()
                }
            }
            items.clear()
            items.add(ChooseGame("Normal", 14,dataSnapshot.getValue(LeaderBoard::class.java)!!.normal,0))
            items.add(ChooseGame("Odd Even", 13,dataSnapshot.getValue(LeaderBoard::class.java)!!.oddEven,100))
            items.add(ChooseGame("Rush", 15,dataSnapshot.getValue(LeaderBoard::class.java)!!.rush,300))
            items.add(ChooseGame("AlphaNum", 16,dataSnapshot.getValue(LeaderBoard::class.java)!!.alphaNum,500))
            items.add(ChooseGame("Mix", 17,dataSnapshot.getValue(LeaderBoard::class.java)!!.mix,800))
            items.add(ChooseGame("Double Attack", 15,dataSnapshot.getValue(LeaderBoard::class.java)!!.doubleAttack,1000))
        }else{
            tvTotalScore.text = "" + 0

            items.add(ChooseGame("Normal", 14,0,0))
            items.add(ChooseGame("Odd Even", 13,0,100))
            items.add(ChooseGame("Rush", 15,0,300))
            items.add(ChooseGame("AlphaNum", 16,0,500))
            items.add(ChooseGame("Mix", 17,0,800))
            items.add(ChooseGame("Double Attack", 15,0,1000))
        }
        adapter.notifyDataSetChanged()

        layout_loading.visibility = View.GONE
        loadingTimer.cancel()
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
                LinearLayout.LayoutParams.MATCH_PARENT
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

        rvTask.layoutManager = LinearLayoutManager(this)
        rvTask.adapter = taskAdapter
        loadTask()

        if (levelUp)
            btnTask.text ="Level Up"

        when(enumValueOf<Rank>(currentRank)){
            Rank.Toddler -> taskNextRank.text = "Next Rank : Beginner"
            Rank.Beginner -> taskNextRank.text = "Next Rank : Senior"
            Rank.Senior -> taskNextRank.text = "Next Rank : Master"
            Rank.Master -> taskNextRank.text = "Next Rank : GrandMaster"
            Rank.GrandMaster -> taskNextRank.text = "Next Rank : Soon"
        }

        btnTask.onClick {
            if (btnTask.text == "Level Up" && levelUp){
                levelUp = !levelUp
                tvTaskInfo.text = ""

                var nextRank = ""
                var nextEnergyLimit = 0
                when (enumValueOf<Rank>(currentRank)) {
                    Rank.Toddler -> {
                        nextRank = Rank.Beginner.toString()
                        energyLimit = 105
                    }
                    Rank.Beginner -> {
                        nextRank = Rank.Senior.toString()
                        energyLimit = 110
                    }
                    Rank.Senior -> {
                        nextRank = Rank.Master.toString()
                        energyLimit = 115
                    }
                    Rank.Master -> {
                        nextRank = Rank.GrandMaster.toString()
                        energyLimit = 120
                    }
                }
                currentRank = nextRank
                rankPresenter.fetchRank()
                rankPresenter.fetchGameAvailable()
                rankPresenter.updateRank(nextRank,energyLimit.toLong())
                btnTask.text == "Okay"
            }
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
                LinearLayout.LayoutParams.MATCH_PARENT
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

        rankTitle.text = currentRank

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

    private fun popUpMessage(type: Int,message: String){
        val inflater: LayoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.pop_up_message,null)

        // Initialize a new instance of popup window
        popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
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
                val remainingPoint = point - items[position].priceGame!!
                rankPresenter.buyGame(position,remainingPoint)
                rankPresenter.fetchGameAvailable()
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
            tvMessageInfo.typeface = typeface

            tvMessageInfo.text = message
            if (auth.currentUser != null)
                rankPresenter.fetchGameAvailable()

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

    private fun popUpTutorial(){
        val inflater: LayoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.tutorial,null)

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

        val btnWatchTutorial = view.findViewById<Button>(R.id.btnWatchTutorial)
        val btnStartPlay = view.findViewById<Button>(R.id.btnStartPlay)
        val btnClose = view.findViewById<Button>(R.id.btnClose)
        val tvGameTypeInfo = view.findViewById<TextView>(R.id.tvRankGameTypeInfo)
        val tvTutorialTitle = view.findViewById<TextView>(R.id.tvTutorialTitle)
        val ivRankGameType = view.findViewById<ImageView>(R.id.ivRankGameType)
        val tvTutorialDesc = view.findViewById<TextView>(R.id.tvTutorialDesc)

        tvTutorialTitle.typeface = typeface
        tvTutorialDesc.typeface = typeface
        tvGameTypeInfo.typeface = typeface
        btnStartPlay.typeface = typeface
        btnWatchTutorial.typeface = typeface

        activity_rank.alpha = 0.1F

        when(position){
            0 -> {
                ivRankGameType.setImageResource(R.drawable.normal_game)
                tvGameTypeInfo.text = "Once You Stop Learning, You Start Dying"
            }
            1 -> {
                ivRankGameType.setImageResource(R.drawable.odd_even_game)
                tvGameTypeInfo.text = "Try Something ODD, Because ODD Seems More EVEN"
            }
            2 -> {
                ivRankGameType.setImageResource(R.drawable.rush_game)
                tvGameTypeInfo.text = "Don't Rush Anything. When the Time is Right, it'll Happen"
            }
            3 -> {
                ivRankGameType.setImageResource(R.drawable.alpha_num_game)
                tvGameTypeInfo.text = "If Plan 'A' Didn't Work, the Alphabet has More 25 Letters"
            }
            4 ->{
                ivRankGameType.setImageResource(R.drawable.mix_game)
                tvGameTypeInfo.text = "Never Mix Business with Pleasure"
            }
            5 ->{
                ivRankGameType.setImageResource(R.drawable.double_attack_game)
                tvGameTypeInfo.text = "Do Good and The Good Life will Follow"
            }
        }

        btnClose.onClick {
            btnClose.startAnimation(clickAnimation)
            popupWindow.dismiss()
            activity_rank.alpha = 1F
        }

        btnStartPlay.onClick {
            val watchTutorial = sharedPreference.getBoolean("tutorial${position}",false)
            if (watchTutorial){
                val energyRemaining = energy - items[position].energy!!
                rankPresenter.updateEnergy(energyRemaining.toLong(),true)
                popupWindow.dismiss()
                activity_rank.alpha = 1F
            }else{
                ivRankGameType.layoutParams.height = 1200
                ivRankGameType.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                tvTutorialDesc.visibility = View.VISIBLE
                when(position){
                    0 -> {
                        tvTutorialDesc.visibility = View.GONE
                        ivRankGameType.setImageResource(R.drawable.normal_tutorial)
                    }
                    1 -> {
                        ivRankGameType.visibility = View.GONE
                        tvTutorialDesc.text = getString(R.string.oddEven_tutorial)
                    }
                    2 -> {
                        ivRankGameType.visibility = View.GONE
                        tvTutorialDesc.text = getString(R.string.rush_tutorial)
                    }
                    3 -> {
                        ivRankGameType.visibility = View.GONE
                        tvTutorialDesc.text = getString(R.string.alphaNum_tutorial)
                    }
                    4 ->{
                        ivRankGameType.visibility = View.GONE
                        tvTutorialDesc.text = getString(R.string.mix_tutorial)
                    }
                    5 ->{
                        ivRankGameType.visibility = View.GONE
                        tvTutorialDesc.text = getString(R.string.oddEven_tutorial)
                    }
                }
                tvGameTypeInfo.visibility = View.GONE
                btnWatchTutorial.visibility = View.GONE

                editor = sharedPreference.edit()
                editor.putBoolean("tutorial${position}",true)
                editor.apply()
            }

        }

        btnWatchTutorial.onClick {
            ivRankGameType.layoutParams.height = 1200
            ivRankGameType.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            tvTutorialDesc.visibility = View.VISIBLE
            when(position){
                0 -> {
                    tvTutorialDesc.visibility = View.GONE
                    ivRankGameType.setImageResource(R.drawable.normal_tutorial)
                }
                1 -> {
                    ivRankGameType.visibility = View.GONE
                    tvTutorialDesc.text = getString(R.string.oddEven_tutorial)
                }
                2 -> {
                    ivRankGameType.visibility = View.GONE
                    tvTutorialDesc.text = getString(R.string.rush_tutorial)
                }
                3 -> {
                    ivRankGameType.visibility = View.GONE
                    tvTutorialDesc.text = getString(R.string.alphaNum_tutorial)
                }
                4 ->{
                    ivRankGameType.visibility = View.GONE
                    tvTutorialDesc.text = getString(R.string.mix_tutorial)
                }
                5 ->{
                    ivRankGameType.visibility = View.GONE
                    tvTutorialDesc.text = getString(R.string.oddEven_tutorial)
                }
            }
            tvGameTypeInfo.visibility = View.GONE
            btnWatchTutorial.visibility = View.GONE
        }


        TransitionManager.beginDelayedTransition(activity_rank)
        popupWindow.showAtLocation(
                activity_rank, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

    }

    override fun onResume() {
        super.onResume()

        val animationBounce = AnimationUtils.loadAnimation(ctx, R.anim.bounce)
        btnInfo.startAnimation(animationBounce)
        loadingTimer()

        if (auth.currentUser != null){
            rankPresenter.fetchRank()
            rankPresenter.fetchBalance()
            rankPresenter.fetchGameAvailable()
        }
    }

    override fun onStart() {
        if (auth.currentUser != null && Profile.getCurrentProfile() != null)
        Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivProfile)

        super.onStart()
    }

    override fun onPause() {
        isClicked = false
        editor = sharedPreference.edit()
        editor.putLong("lastCountEnergy",Date().time)
        editor.putLong("countedEnergy",counted.toLong())
        editor.apply()
        countDownTimer.cancel()
        loadingTimer.cancel()

        super.onPause()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == "fetchBalance"){
            editor = sharedPreference.edit()
            editor.putInt("point",dataSnapshot.getValue(Balance::class.java)!!.point!!)
            editor.apply()
            energy = dataSnapshot.getValue(Balance::class.java)!!.energy!!
            energyLimit = dataSnapshot.getValue(Balance::class.java)!!.energyLimit!!
            point = dataSnapshot.getValue(Balance::class.java)!!.point!!
            setUpEnergyTimer()
            tvEnergy.text = "e:${energy}/${energyLimit}"
            tvPoint.text = "c:${point}"
        }else if(response == "fetchGameAvailable"){
            availableGameList.clear()
            availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.normal!!)
            availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.oddEven!!)
            availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.rush!!)
            availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.alphaNum!!)
            availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.mix!!)
            availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.doubleAttack!!)

            adapter.notifyDataSetChanged()
        }else if(response == "fetchScore")
            loadBestScore(dataSnapshot)
        else if (response == "fetchRank"){
            editor = sharedPreference.edit()
            editor.putString("currentRank", dataSnapshot.value.toString())
            editor.apply()
            currentRank = dataSnapshot.value.toString()

            when(enumValueOf<Rank>(currentRank)){
                Rank.Toddler -> tvRank.textColorResource = R.color.fbutton_color_silver
                Rank.Beginner -> tvRank.textColorResource = R.color.fbutton_color_emerald
                Rank.Senior -> tvRank.textColorResource = R.color.fbutton_color_sun_flower
                Rank.Master -> tvRank.textColorResource = R.color.fbutton_color_pomegranate
                Rank.GrandMaster -> tvRank.textColorResource = R.color.fbutton_color_silver
            }

            loadTask()
            rankPresenter.fetchScore()
            if (levelUp){
                tvTaskInfo.visibility = View.VISIBLE
                val animationBounce = AnimationUtils.loadAnimation(ctx, R.anim.bounce)
                animationBounce.repeatCount = Animation.INFINITE
                animationBounce.repeatMode = Animation.REVERSE

                tvTaskInfo.startAnimation(animationBounce)
            }
            tvRank.text = currentRank
            loadRankInfo(currentRank)
        }

    }

    override fun response(message: String, response: String) {
        if (response === "buyGame")
            popUpMessage(2, message)
        else if(response === "updateEnergy"){
            if (!isClicked){
                isClicked = true
                when (position) {
                    0 -> {
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "normal");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "game_type");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle)

                        startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Rank,
                                "type" to GameType.Normal))
                        finish()
                    }
                    1 -> {
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "odd_even");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "game_type");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle)

                        startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Rank,
                                "type" to GameType.OddEven))
                        finish()
                    }
                    2 -> {
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "rush");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "game_type");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle)

                        startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Rank,
                                "type" to GameType.Rush))
                        finish()
                    }
                    3 -> {
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "alpha_num");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "game_type");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle)

                        startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Rank,
                                "type" to GameType.AlphaNum))
                        finish()
                    }
                    4 ->{
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "mix");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "game_type");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle)

                        startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Rank,
                                "type" to GameType.Mix))
                        finish()
                    }
                    5 ->{
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "double_attack");
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "game_type");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle)

                        startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Rank,
                                "type" to GameType.DoubleAttack))
                        finish()
                    }
                }
            }
        }else if(response == "error"){
            popUpMessage(2,message)
        }else if(response == "levelUp"){
            popUpMessage(2,message)
        }
    }

    private fun loadRankInfo(currentRank: String){
        when(enumValueOf<Rank>(currentRank)){
            Rank.Toddler -> {
                rankDetailItems.clear()
                rankDetailItems.add("Energy Limit 100")
                rankDetailItems.add("Get Energy Every 5 Minutes")
            }
            Rank.Beginner -> {
                rankDetailItems.clear()
                rankDetailItems.add("Energy Limit 105")
                rankDetailItems.add("Bonus 1 Point when Answered Correct")
                rankDetailItems.add("Extra 1% Extra Reward")
            }
            Rank.Senior -> {
                rankDetailItems.clear()
                rankDetailItems.add("Energy Limit 110")
                rankDetailItems.add("Get Energy Every 4 Minutes")
                rankDetailItems.add("Bonus 2 Point when Answered Correct")
            }
            Rank.Master -> {
                rankDetailItems.clear()
                rankDetailItems.add("Energy Limit 115")
                rankDetailItems.add("Bonus 3 Point when Answered Correct")
                rankDetailItems.add("Extra 2% Extra Reward")
            }
            Rank.GrandMaster -> {
                rankDetailItems.clear()
                rankDetailItems.add("Energy Limit 120")
                rankDetailItems.add("Get Energy Every 3 Minutes")
                rankDetailItems.add("Bonus 5 Point when Answered Correct")
            }
        }
        rankDetailAdapter.notifyDataSetChanged()
    }

    private fun setUpEnergyTimer(){
        if (energy <= energyLimit) {

            currentRank = sharedPreference.getString("currentRank", Rank.Toddler.toString()).toString()

            energyTime = when(enumValueOf<Rank>(currentRank)){
                Rank.Toddler -> 300
                Rank.Beginner -> 300
                Rank.Senior -> 240
                Rank.Master -> 240
                Rank.GrandMaster -> 180
            }

            val remainingEnergyToFull = (energyLimit - energy) * energyTime
            val currentDate = Date().time

            val lastCountEnergy = sharedPreference.getLong("lastCountEnergy", currentDate)
            counted = sharedPreference.getLong("countedEnergy", 0).toInt()

            diff = currentDate - lastCountEnergy
            remainTime = (remainingEnergyToFull - (diff / 1000) - counted).toInt()

            energyTimer()
        }
    }

    fun energyTimer(){
        if (!checkUpdateEnergy){
            checkUpdateEnergy = true
            val energyGet = diff / 1000 / energyTime
            if (energyGet + energy >= energyLimit) // energy get + energy >= energy limit, energy = energy limit
                energy = energyLimit // else energy += energyget
            else
                energy += energyGet.toInt()
            if (energyGet.toInt() > 0){
                rankPresenter.updateEnergy(energy.toLong(),false)
                counted = 0
            }
        }
        if (remainTime > 0 && energy < energyLimit){

            var timerSec = remainTime % energyTime
            var timerMin = 0

            if (timerSec > 60){
                timerMin = timerSec / 60
                timerSec %= 60
            }
            countDownTimer = object : CountDownTimer((timerSec.toLong()+1) * 1000,1000){
                override fun onFinish() {
                    remainTime--
                    tvEnergy.text = "e:${energy}/${energyLimit}"
                    if (timerMin == 0){
                        energy++
                        rankPresenter.updateEnergy(energy.toLong(),false)
                        energyTimer()
                        counted = 0
                    }

                }

                override fun onTick(millisUntilFinished: Long) {
                    counted += 1
                    remainTime--
                }

            }
            countDownTimer.start()
        }


    }

    private fun loadTask(){
        taskList.clear()
        taskProgressList.clear()
        when(enumValueOf<Rank>(currentRank)){
            Rank.Toddler -> {
                taskList.add("Play Normal Mode 1 Time")
                taskList.add("Reach 100 Point in Normal Mode")

                val progress1 = sharedPreference.getInt("toddler1",0)
                val progress2 = sharedPreference.getInt("toddler2",0)
                if (progress1 >= 1)
                    taskProgressList.add("completed")
                else
                    taskProgressList.add("${progress1}/1")
                if (progress2 >= 100)
                    taskProgressList.add("completed")
                else
                    taskProgressList.add("${progress2}/100")
                if (progress1 >= 1 && progress2 >= 100)
                    levelUp = true

            }
            Rank.Beginner ->{
                taskList.add("Reach 200 Point in Odd Even")
                taskList.add("Reach 250 Point in Normal Mode")
                taskList.add("Play with Friends 1 Time")
                taskList.add("Play Daily Quest 3 Times")

                val progress1 = sharedPreference.getInt("beginner1",0)
                val progress2 = sharedPreference.getInt("beginner2",0)
                val progress3 = sharedPreference.getInt("beginner3",0)
                val progress4 = sharedPreference.getInt("beginner4",0)
                if (progress1 >= 200)
                    taskProgressList.add("completed")
                else
                    taskProgressList.add("${progress1}/200")
                if (progress2 >= 250)
                    taskProgressList.add("completed")
                else
                    taskProgressList.add("${progress2}/250")
                if (progress3 >= 1)
                    taskProgressList.add("completed")
                else
                    taskProgressList.add("${progress3}/1")
                if (progress4 >= 3)
                    taskProgressList.add("completed")
                else
                    taskProgressList.add("${progress4}/3")
                if (progress1 >= 200 && progress2 >= 250 && progress3 >=1 && progress4 >=3)
                    levelUp = true
            }
            Rank.Senior -> {
                taskList.add("Reach 200 Point in AlphaNum")
                taskList.add("Reach 250 Point in Rush")
                taskList.add("Reach 300 Point in Normal Mode")
                taskList.add("Join Tournament 1 Time")

                val progress1 = sharedPreference.getInt("senior1",0)
                val progress2 = sharedPreference.getInt("senior2",0)
                val progress3 = sharedPreference.getInt("senior3",0)
                val progress4 = sharedPreference.getInt("senior4",0)

                if (progress1 >= 200)
                    taskProgressList.add("completed")
                else
                    taskProgressList.add("${progress1}/200")
                if (progress2 >= 250)
                    taskProgressList.add("completed")
                else
                    taskProgressList.add("${progress2}/250")
                if (progress3 >= 300)
                    taskProgressList.add("completed")
                else
                    taskProgressList.add("${progress3}/300")
                if (progress4 >= 1)
                    taskProgressList.add("completed")
                else
                    taskProgressList.add("${progress4}/1")
                if (progress1 >= 200 && progress2 >= 250 && progress3 >= 300 && progress4 >= 1)
                    levelUp = true
            }
            Rank.Master -> {
                taskList.add("Win Tournament 1 Time")
                taskList.add("Reach 300 Points in Rush")
                taskList.add("Reach 300 Points in Mix")
                taskList.add("Reach 400 Points in Normal")
                taskList.add("Solve Daily Quest 5 Times")

                val progress1 = sharedPreference.getInt("master1",0)
                val progress2 = sharedPreference.getInt("master2",0)
                val progress3 = sharedPreference.getInt("master3",0)
                val progress4 = sharedPreference.getInt("master4",0)
                val progress5 = sharedPreference.getInt("master5",0)

                if (progress1 >= 1)
                    taskProgressList.add("completed")
                else
                    taskProgressList.add("${progress1}/1")
                if (progress2 >= 300)
                    taskProgressList.add("completed")
                else
                    taskProgressList.add("${progress2}/300")
                if (progress3 >= 300)
                    taskProgressList.add("completed")
                else
                    taskProgressList.add("${progress3}/300")
                if (progress4 >= 400)
                    taskProgressList.add("completed")
                else
                    taskProgressList.add("${progress4}/400")
                if (progress5 >= 5)
                    taskProgressList.add("completed")
                else
                    taskProgressList.add("${progress5}/5")

                if (progress1 >= 1 && progress2 >= 300 && progress3 >= 300 && progress4 >= 400 && progress5 >= 5)
                    levelUp = true
            }
            Rank.GrandMaster -> {
                taskList.add("Total Point Rank 2000")
                taskList.add("Win Tournament 3 Times")
                taskList.add("Reach 300 Points in Double Attack")
                taskList.add("Reach 400 Points in Mix")

                val progress1 = sharedPreference.getInt("gMaster1",0)
                val progress2 = sharedPreference.getInt("gMaster2",0)
                val progress3 = sharedPreference.getInt("gMaster3",0)
                val progress4 = sharedPreference.getInt("gMaster4",0)
                taskProgressList.add("${progress1}/2000")
                taskProgressList.add("${progress2}/3")
                taskProgressList.add("${progress3}/300")
                taskProgressList.add("${progress4}/400")
            }
        }
        taskAdapter.notifyDataSetChanged()
    }

    private fun loadingTimer(){
        layout_loading.visibility = View.VISIBLE
        val view = findViewById<View>(R.id.layout_loading)

        val tvLoadingTitle = view.findViewById<TextView>(R.id.tvLoadingTitle)
        val tvLoadingInfo = view.findViewById<TextView>(R.id.tvLoadingInfo)

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
                finish()
                toast("Oops Something Wrongs!")
            }
        }
        loadingTimer.start()
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

enum class Rank {
    Toddler,
    Beginner,
    Senior,
    Master,
    GrandMaster
}