package com.example.balapplat.rank

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
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
import com.example.balapplat.play.CountdownActivity
import com.example.balapplat.view.MainView
import com.example.balapplat.presenter.Presenter
import com.example.balapplat.R
import com.example.balapplat.home.MarketActivity
import com.example.balapplat.model.Inviter
import com.example.balapplat.play.GameType
import com.example.balapplat.play.StatusPlayer
import com.example.balapplat.presenter.RankPresenter
import com.facebook.Profile
import com.example.balapplat.utils.showSnackBar
import com.example.balapplat.view.RankView
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
import kotlinx.android.synthetic.main.activity_market.*
import kotlinx.android.synthetic.main.activity_rank.*
import kotlinx.android.synthetic.main.activity_rank.ivProfile
import kotlinx.android.synthetic.main.activity_rank.tvEnergy
import kotlinx.android.synthetic.main.activity_rank.tvPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.pop_up_task.*
import kotlinx.android.synthetic.main.row_choose_game.*
import kotlinx.android.synthetic.main.row_rank.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
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
    lateinit var data: Inviter
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

        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvRank.typeface = typeface
        tvPoint.typeface = typeface
        tvEnergy.typeface = typeface
        tvTotalScore.typeface = typeface

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

        taskAdapter = TaskRecyclerViewAdapter(this,taskList,taskProgressList)

        adapter = RankRecyclerViewAdapter(this,items,availableGameList){
            if (auth.currentUser == null)
                popUpMessage(2,"You Must Sign In First")
            else{
                position = it
                if (!availableGameList[it]){
                    if (items[it].priceGame!! < tvPoint.text.toString().toInt())
                        popUpMessage(1,"Do You Want to Buy?")
                    else
                        popUpMessage(2,"Not Enough Money")
                }else{
                    if (items[it].energy!! > energy)
                        popUpMessage(2,"Not Enough Energy")
                    else{
                        position = it
                        val energyRemaining = energy - items[it].energy!!
                        rankPresenter.updateEnergy(energyRemaining.toLong(),true)
                    }

                }
            }
        }

        rvRank.layoutManager = LinearLayoutManager(this)
        rvRank.adapter = adapter
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

    override fun onStart() {
        sharedPreference =  this.getSharedPreferences("LOCAL_DATA",Context.MODE_PRIVATE)
        database = FirebaseDatabase.getInstance().reference
        rankPresenter = RankPresenter(this,database)
        auth = FirebaseAuth.getInstance()
        rankDetailAdapter = RankDetailRecyclerViewAdapter(this, rankDetailItems)
        Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivProfile)

        if (auth.currentUser != null){
            rankPresenter.fetchBalance()
            rankPresenter.fetchGameAvailable()
            rankPresenter.fetchScore()
            rankPresenter.fetchRank()
        }

        countDownTimer = object : CountDownTimer(1000,1000){
            override fun onFinish() {

            }

            override fun onTick(millisUntilFinished: Long) {
            }

        }

        super.onStart()
    }

    override fun onPause() {
        editor = sharedPreference.edit()
        editor.putLong("lastCountEnergy",Date().time)
        editor.putLong("countedEnergy",counted.toLong())
        editor.apply()
        countDownTimer.cancel()

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
            tvEnergy.text = "${energy}/${energyLimit}"
            tvPoint.text = "${point}"
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
            loadTask()
            if (levelUp){
                tvTaskInfo.text = "!"
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
            when (position) {
                0 -> {
                    startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Rank,
                            "type" to GameType.Normal))
                    finish()
                }
                1 -> {
                    startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Rank,
                            "type" to GameType.OddEven))
                    finish()
                }
                2 -> {
                    startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Rank,
                            "type" to GameType.Rush))
                    finish()
                }
                3 -> {
                    startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Rank,
                            "type" to GameType.AlphaNum))
                    finish()
                }
                4 ->{
                    startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Rank,
                            "type" to GameType.Mix))
                    finish()
                }
                5 ->{
                    startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Rank,
                            "type" to GameType.DoubleAttack))
                    finish()
                }
            }
        }else if(response == "error"){
            popUpMessage(2,message)
        }else if(response == "levelUp"){
            popUpMessage(2,message)
        }
    }

    fun loadRankInfo(currentRank: String){
        when(enumValueOf<Rank>(currentRank)){
            Rank.Toddler -> {
                rankDetailItems.clear()
                rankDetailItems.add("Energy Limit 100")
                rankDetailItems.add("Extra 1% Credit Reward")
            }
            Rank.Beginner -> {
                rankDetailItems.clear()
                rankDetailItems.add("Get 5 Extra Credit when Solve Puzzle")
                rankDetailItems.add("Energy Limit 105")
                rankDetailItems.add("Extra 2% Credit Reward")
            }
            Rank.Senior -> {
                rankDetailItems.clear()
                rankDetailItems.add("Get 8 Extra Credit when Solve Puzzle")
                rankDetailItems.add("Energy Limit 110")
                rankDetailItems.add("Extra 4% Credit Reward")
                rankDetailItems.add("Extra 10% Point when Answered Correct")
            }
            Rank.Master -> {
                rankDetailItems.clear()
                rankDetailItems.add("Get 10 Extra Credit when Solve Puzzle")
                rankDetailItems.add("Energy Limit 115")
                rankDetailItems.add("Extra 5% Credit Reward")
                rankDetailItems.add("Extra 12% Point when Answered Correct")
            }
            Rank.GrandMaster -> {
                rankDetailItems.clear()
                rankDetailItems.add("Get 12 Extra Credit when Solve Puzzle")
                rankDetailItems.add("Energy Limit 120")
                rankDetailItems.add("Extra 8% Credit Reward")
                rankDetailItems.add("Extra 15% Point when Answered Correct")
                rankDetailItems.add("Energy Revive 5% Faster")
            }
        }
        rankDetailAdapter.notifyDataSetChanged()
    }

    fun setUpEnergyTimer(){
        if (energy != energyLimit) {
            val remainingEnergyToFull = (energyLimit - energy) * 120 // 10 detik
            val currentDate = Date().time

            val lastCountEnergy = sharedPreference.getLong("lastCountEnergy", currentDate)
            counted = sharedPreference.getLong("countedEnergy", 0).toInt()

            diff = currentDate - lastCountEnergy
            remainTime = (remainingEnergyToFull - (diff / 1000) - counted).toInt()

            energyTimer()
        }
    }

    fun energyTimer(){
        if (remainTime > 0 && !checkUpdateEnergy){
            checkUpdateEnergy = true
            var energyGet = diff / 1000 / 120
            if (energyGet > energyLimit)
                energyGet = energyLimit.toLong()
            Log.d("energy get", energyGet.toString())
            rankPresenter.updateEnergy(energy.toLong() + energyGet,false)
            rankPresenter.fetchBalance()
        }
        if (remainTime > 0){

            var timerSec = remainTime % 120 // 100 detik
            var timerMin = 0

            if(timerSec == 0){
                rankPresenter.updateEnergy(energy.toLong(),false)
                remainTime--
                energyTimer()
            }else{
                if (timerSec > 60){
                    timerMin = timerSec / 60
                    timerSec %= 60
                }
                countDownTimer = object : CountDownTimer((timerSec.toLong()+1) * 1000,1000){
                    override fun onFinish() {
                        remainTime--
                        tvEnergy.text = "${energy}/${energyLimit}"
                        energyTimer()
                        energy++
                        rankPresenter.updateEnergy(energy.toLong(),false)
                        rankPresenter.fetchBalance()
                    }

                    override fun onTick(millisUntilFinished: Long) {
                        counted += 1
                        remainTime--
                    }

                }
                countDownTimer.start()
            }
        }


    }

    fun loadTask(){
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
                taskList.add("Play with Friends 1 times")
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
                taskList.add("Reach 200 Point in Odd Even")
                taskList.add("Reach 250 Point in Normal Mode")
                taskList.add("Play with Friends 1 times")
                taskList.add("Play Daily Quest 3 Times")

                val progress1 = sharedPreference.getInt("beginner1",0)
                val progress2 = sharedPreference.getInt("beginner2",0)
                val progress3 = sharedPreference.getInt("beginner3",0)
                val progress4 = sharedPreference.getInt("beginner4",0)
                taskProgressList.add("${progress1}/200")
                taskProgressList.add("${progress2}/250")
                taskProgressList.add("${progress3}/1")
                taskProgressList.add("${progress4}/3")
            }
            Rank.Master -> {
                taskList.add("Reach 200 Point in Odd Even")
                taskList.add("Reach 250 Point in Normal Mode")
                taskList.add("Play with Friends 1 times")
                taskList.add("Play Daily Quest 3 Times")

                val progress1 = sharedPreference.getInt("beginner1",0)
                val progress2 = sharedPreference.getInt("beginner2",0)
                val progress3 = sharedPreference.getInt("beginner3",0)
                val progress4 = sharedPreference.getInt("beginner4",0)
                taskProgressList.add("${progress1}/200")
                taskProgressList.add("${progress2}/250")
                taskProgressList.add("${progress3}/1")
                taskProgressList.add("${progress4}/3")
            }
            Rank.GrandMaster -> {
                taskList.add("Reach 200 Point in Odd Even")
                taskList.add("Reach 250 Point in Normal Mode")
                taskList.add("Play with Friends 1 times")
                taskList.add("Play Daily Quest 3 Times")

                val progress1 = sharedPreference.getInt("beginner1",0)
                val progress2 = sharedPreference.getInt("beginner2",0)
                val progress3 = sharedPreference.getInt("beginner3",0)
                val progress4 = sharedPreference.getInt("beginner4",0)
                taskProgressList.add("${progress1}/200")
                taskProgressList.add("${progress2}/250")
                taskProgressList.add("${progress3}/1")
                taskProgressList.add("${progress4}/3")
            }
        }
        taskAdapter.notifyDataSetChanged()
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