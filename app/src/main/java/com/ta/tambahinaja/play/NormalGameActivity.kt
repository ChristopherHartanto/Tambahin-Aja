package com.ta.tambahinaja.play

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.res.ResourcesCompat
import androidx.transition.TransitionManager
import com.ta.tambahinaja.R
import com.ta.tambahinaja.model.Inviter
import com.ta.tambahinaja.presenter.MatchPresenter
import com.ta.tambahinaja.rank.Rank
import com.ta.tambahinaja.tournament.TournamentData
import com.ta.tambahinaja.utils.showSnackBar
import com.ta.tambahinaja.view.MatchView
import com.facebook.Profile
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_normal_game.*
import kotlinx.android.synthetic.main.activity_normal_game.tvPoint
import kotlinx.android.synthetic.main.activity_rank.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.text.SimpleDateFormat
import java.util.*

class NormalGameActivity : AppCompatActivity(), MatchView {

    private lateinit var sharedPreference: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    lateinit var matchPresenter: MatchPresenter
    private lateinit var countDownTimer : CountDownTimer
    lateinit var data: Inviter
    lateinit var editor: SharedPreferences.Editor
    private lateinit var rewardedAd: RewardedAd
    private var handler: Handler = Handler()
    private lateinit var runnable: Runnable
    private var continueGame = false
    private lateinit var currentRank : String
    //private lateinit var soundPool : SoundPool
    private var soundCorrect = 0
    private var creditReward = 0
    private var pointReward = 0
    private var creatorFacebookId = ""
    private var creatorName = ""
    private var joinOnlineFacebookId = ""
    private var joinOnlineName = ""
    private var joinFriendFacebookId = ""
    private var joinFriendName = ""
    private var inviterFacebookId = ""
    private var inviterName = ""
    private var count = 4
    private var point = 0
    private var timer = 45
    private var defaultTimer = 45
    private var answer = 999
    private var highScore = 0
    private var opponentPoint = 0
    private var type = GameType.Normal
    private var mix = false
    private var player = StatusPlayer.Single
    private var tournamentEndDate = ""
    private lateinit var popupWindow : PopupWindow
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)
    private lateinit var mAdView : AdView

    private var numberArr : MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_game)

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        rewardedAd = RewardedAd(this, "ca-app-pub-3940256099942544/5224354917")
        val adLoadCallback = object: RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                // Ad successfully loaded.
            }
            override fun onRewardedAdFailedToLoad(errorCode: Int) {
                // Ad failed to load.
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
        runnable = Runnable {
            if (!continueGame){
                progress_bar.visibility = View.GONE
                toast("Failed to Continue")
                calculateRankReward()
            }
        }

        supportActionBar?.hide()

        sharedPreference =  this.getSharedPreferences("LOCAL_DATA",Context.MODE_PRIVATE)
        database = FirebaseDatabase.getInstance().reference
        matchPresenter = MatchPresenter(this,database)
        auth = FirebaseAuth.getInstance()

        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvPlayerName.typeface = typeface
        tvOpponentName.typeface = typeface
        tvPoint.typeface = typeface
        tvTimer.typeface = typeface
        tvQuestion.typeface = typeface
        tvPlayerPoint.typeface = typeface
        tvOpponentPoint.typeface = typeface

        creatorFacebookId = intent.extras!!.getString("creatorFacebookId").toString()
        creatorName = intent.extras!!.getString("creatorName").toString()
        joinOnlineFacebookId = intent.extras!!.getString("joinOnlineFacebookId").toString()
        joinOnlineName = intent.extras!!.getString("joinOnlineName").toString()
        joinFriendFacebookId = intent.extras!!.getString("joinFriendFacebookId").toString()
        joinFriendName = intent.extras!!.getString("joinFriendName").toString()
        inviterFacebookId = intent.extras!!.getString("inviterFacebookId").toString()
        inviterName = intent.extras!!.getString("inviterName").toString()

        player = intent.extras!!.getSerializable("status") as StatusPlayer
        type = intent.extras!!.getSerializable("type") as GameType

        if (player != StatusPlayer.Single && player != StatusPlayer.Rank){
            fetchProfile()
            tvPoint.visibility = View.INVISIBLE
        }else
            layoutMultipleGame.visibility = View.INVISIBLE


        if (type == GameType.OddEven)
            oddEvenKeyboard()
        else
            normalKeyboard()

        when(player){
            StatusPlayer.Creator->{
                matchPresenter.fetchOpponent(joinOnlineFacebookId)

            }
            StatusPlayer.JoinOnline->{
                matchPresenter.fetchOpponent(Profile.getCurrentProfile().id)
            }
            StatusPlayer.JoinFriend->{
                defaultTimer = intent.extras!!.getInt("timer")
                timer = defaultTimer
                matchPresenter.fetchOpponent(Profile.getCurrentProfile().id)
            }
            StatusPlayer.Inviter->{
                defaultTimer = intent.extras!!.getInt("timer")
                timer = defaultTimer
                matchPresenter.fetchOpponent(joinFriendFacebookId)
            }
            StatusPlayer.Rank->{
                matchPresenter.getHighScore(auth,type)
            }
            StatusPlayer.Single -> {
                defaultTimer = intent.extras!!.getInt("timer")
                timer = defaultTimer
            }
        }

        currentRank = sharedPreference.getString("currentRank", Rank.Toddler.toString()).toString() // untuk reward dan update rank
        if (type == GameType.Rush){
            if (player == StatusPlayer.Rank || player == StatusPlayer.Creator || player == StatusPlayer.JoinOnline){
                defaultTimer = 5
                timer = defaultTimer
            }
        }

        if (type == GameType.Mix)
            mix = true

        generate()
        control(false)
        defaultTimer =  timer
    }

    @SuppressLint("SetTextI18n")
    private fun generate(){
        count = if (point > 200)
            6
        else if (point > 100)
            5
        else
            4

        if (mix){
            when((0 until 4).random()){
                0 -> {
                    type = GameType.Normal
                    normalKeyboard()
                }
                1 -> {
                    type = GameType.AlphaNum
                    normalKeyboard()
                }
                else -> {
                    type = GameType.OddEven
                    oddEvenKeyboard()
                }
            }
        }
        if (type == GameType.AlphaNum){
            tvQuestion.text = ""
            numberArr.clear()
            for(x in 0 until count)
            {
                var value = 0
                val choose = Random().nextInt(2)

                if (choose == 1){
                    value = Random().nextInt(8) + 65
                    numberArr.add(value)
                    tvQuestion.text = tvQuestion.text.toString() + value.toChar()
                }else{
                    value = Random().nextInt(9)
                    numberArr.add(value)
                    tvQuestion.text = tvQuestion.text.toString() + value
                }
            }
        }else if(type == GameType.DoubleAttack){

            tvQuestion.text = ""
            numberArr.clear()
            for(x in 0 until count)
            {
                val value = Random().nextInt(5)
                numberArr.add(value)

                tvQuestion.text = tvQuestion.text.toString() + value
            }
        }else{
            tvQuestion.text = ""
            numberArr.clear()
            for(x in 0 until count)
            {
                val value = Random().nextInt(9)
                numberArr.add(value)

                tvQuestion.text = tvQuestion.text.toString() + value
            }
        }

        answer = generateAnswer()
        //toast(""+answer)
    }

    private fun generateAnswer() : Int {
        var result = 0
        if(type == GameType.Normal|| type == GameType.OddEven || type == GameType.Rush){
            val temp = numberArr
            for(x in 0 until count)
            {
                if(x != count-1) {
                    temp[x+1] += temp[x]

                    if(temp[x+1] / 10 == 1)
                        temp[x+1] -= 9
                }
                else
                    result = temp[x]

            }
            if (type == GameType.OddEven){
                result = if (result % 2 == 0)
                    0 // genap
                else
                    1 // ganjil
            }
        }else if(type == GameType.AlphaNum){
            val temp = numberArr
            for(x in 0 until count)
            {
                if(x != count-1) {
                    if (temp[x] >= 65)
                        temp[x] -= 64
                    if (temp[x+1] >= 65)
                        temp[x+1] -= 64

                    temp[x+1] += temp[x]

                    if(temp[x+1] / 10 == 1)
                        temp[x+1] -= 9
                }
                else
                    result = temp[x]

            }
        }else if (type == GameType.DoubleAttack){
            val temp = numberArr
            for(x in 0 until count)
            {
                if(x != count-1) {
                    if (x == 0){
                        temp[0] *= 2
                        if(temp[0] / 10 == 1)
                            temp[0] = temp[0] * 2 - 9
                    }

                    temp[x+1] *= 2
                    if(temp[x+1] / 10 == 1)
                        temp[x+1] = temp[x+1] * 2 - 9

                    temp[x+1] += temp[x]
                    if(temp[x+1] / 10 == 1)
                        temp[x+1] -= 9
                }
                else
                    result = temp[x]

            }
        }

        return result
    }

    private fun checkAnswer(value : Int){
        if (timer > 0){
            if (type == GameType.Normal){
                if (answer == value){
                    point += 10
                    generate()
                }else{
                    if(point > 3)
                        point -= 3
                    else
                        point = 0
                }

            }else if (type == GameType.OddEven){
                if (answer == value){
                    point += 10
                    generate()
                }else{
                    if(point > 9)
                        point -= 9
                    else
                        point = 0
                }
            }else if (type == GameType.AlphaNum){
                if (answer == value){
                    point += 15
                    generate()
                }else{
                    if(point > 7)
                        point -= 7
                    else
                        point = 0
                }
            }else if (type == GameType.Rush){
                if (answer == value){
                    point += 13
                    generate()
                    countDownTimer.cancel()
                    timer = defaultTimer
                    control(true)
                }else{
                    if(point > 4)
                        point -= 4
                    else
                        point = 0
                }
            }
            else if(type == GameType.AlphaNum){
                if (answer == value){
                    point += 12
                    generate()
                }else{
                    if(point > 5)
                        point -= 5
                    else
                        point = 0
                }
            }else if (type == GameType.DoubleAttack){
                if (answer == value){
                    point += 14
                    generate()
                }else{
                    if(point > 4)
                        point -= 4
                    else
                        point = 0
                }

            }
            //soundPool.play(soundCorrect,3F,3F,0,0,1F)
            if (answer == value){ // tambah bonus point
                point += when(enumValueOf<Rank>(currentRank)){
                    Rank.Toddler -> 0
                    Rank.Beginner -> 1
                    Rank.Senior -> 2
                    Rank.Master -> 3
                    Rank.GrandMaster -> 5
                }
            }

            if (player != StatusPlayer.Single){ // update value
                when (player) {
                    StatusPlayer.Inviter -> matchPresenter.updateValue(true,point,opponentPoint,joinFriendFacebookId)
                    StatusPlayer.JoinFriend -> matchPresenter.updateValue(false,point,opponentPoint,inviterFacebookId)
                    StatusPlayer.JoinOnline -> matchPresenter.updateValue(false,point,opponentPoint,creatorFacebookId)
                    StatusPlayer.Creator -> matchPresenter.updateValue(true,point,opponentPoint,joinOnlineFacebookId)
                }
            }

            val animationBounce = AnimationUtils.loadAnimation(ctx, R.anim.bounce)

            if(player == StatusPlayer.Single || player == StatusPlayer.Rank){
                tvPoint.text = "" + point
                tvPoint.startAnimation(animationBounce)
            }else{
                tvPlayerPoint.text = ""+point
                tvPlayerPoint.startAnimation(animationBounce)
            }
        }
    }

    private fun control(status : Boolean){
        countDownTimer = object: CountDownTimer(timer.toLong()*1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (timer < 4){
                    val animationFadeIn = AnimationUtils.loadAnimation(ctx, R.anim.fade_in)
                    cvTimer.startAnimation(animationFadeIn)
                }
                if (timer <= 0){
                    tvTimer.text = "time : " + 0
                }else{
                    timer--
                    tvTimer.text = "time : " + timer
                }

            }
            override fun onFinish() {
                var text = ""
                if (auth.currentUser != null){
                    if (player == StatusPlayer.Rank){
                        if(highScore < point){
                                matchPresenter.sumHighScore(auth,
                                    type,point,highScore)
                        }

                    }else{
                        text = when {
                            point > opponentPoint -> {
                                matchPresenter.getStats(auth,true)
                                "Win"
                            }
                            point < opponentPoint -> {
                                matchPresenter.getStats(auth,false)
                                "Lose"
                            }
                            point == opponentPoint -> {
                                "Draw"
                            }
                            else -> "error"
                        }

                        if (player == StatusPlayer.JoinFriend || player == StatusPlayer.Creator)
                            matchPresenter.removeOnPlay()

                    }
                    when (player) {
                        StatusPlayer.Inviter -> {
                            matchPresenter.addToHistory(auth,point,opponentPoint, joinFriendFacebookId,
                                    joinFriendName,"friend")

                            if (currentRank == Rank.Beginner.toString())
                                updateRank(currentRank)
                            startActivity(intentFor<PostGameActivity>("scorePlayer" to point,
                                    "scoreOpponent" to opponentPoint, "opponentName" to joinFriendName, "opponentFacebookId" to joinFriendFacebookId, "gameResult" to text))
                            finish()
                        }
                        StatusPlayer.JoinFriend -> {
                            matchPresenter.addToHistory(auth,point,opponentPoint, inviterFacebookId,
                                    inviterName,"friend"
                            )
                            if (currentRank == Rank.Beginner.toString())
                                updateRank(currentRank)
                            startActivity(intentFor<PostGameActivity>("scorePlayer" to point,
                                    "scoreOpponent" to opponentPoint, "opponentName" to inviterName, "opponentFacebookId" to inviterFacebookId, "gameResult" to text))
                            finish()
                        }
                        StatusPlayer.Creator -> {
                            matchPresenter.addToHistory(auth,point,opponentPoint, joinOnlineFacebookId,
                                    joinOnlineName,"online"
                            )
                            startActivity(intentFor<PostGameActivity>("scorePlayer" to point,
                                    "scoreOpponent" to opponentPoint, "opponentName" to joinOnlineName, "opponentFacebookId" to joinOnlineName, "gameResult" to text))
                            finish()
                        }
                        StatusPlayer.JoinOnline -> {
                            matchPresenter.addToHistory(auth,point,opponentPoint, creatorFacebookId,
                                    creatorName,"online"
                            )
                            startActivity(intentFor<PostGameActivity>("scorePlayer" to point,
                                    "scoreOpponent" to opponentPoint, "opponentName" to joinOnlineName, "opponentFacebookId" to joinOnlineFacebookId, "gameResult" to text))
                            finish()
                        }
                        StatusPlayer.Rank ->{
                            val reply = sharedPreference.getBoolean("continueRank",false)
                            if (reply)
                                popUpMessage(1,"Do You Want to Continue?")
                            else{
                                calculateRankReward()
                            }
                        }
                        StatusPlayer.Single->{
                            startActivity(intentFor<PostGameActivity>("status" to StatusPlayer.Single,"score" to point))
                            finish()
                        }
                    }
                }else{
                    startActivity(intentFor<PostGameActivity>("status" to StatusPlayer.Single,"score" to point))
                    finish()
                }

            }
        }
        if(status)
            countDownTimer.start()
        else
            countDownTimer.cancel()
    }
    
    private fun normalKeyboard(){

        val view = findViewById<View>(R.id.layout_keyboard)
        val viewHide = findViewById<View>(R.id.layout_odd_even_keyboard)

        view.visibility = View.VISIBLE
        viewHide.visibility = View.GONE
        //layout_odd_even_keyboard.layoutParams = LinearLayout.LayoutParams(0,0)

        val btn1 = view.findViewById<Button>(R.id.btn1)
        val btn2 = view.findViewById<Button>(R.id.btn2)
        val btn3 = view.findViewById<Button>(R.id.btn3)
        val btn4 = view.findViewById<Button>(R.id.btn4)
        val btn5 = view.findViewById<Button>(R.id.btn5)
        val btn6 = view.findViewById<Button>(R.id.btn6)
        val btn7 = view.findViewById<Button>(R.id.btn7)
        val btn8 = view.findViewById<Button>(R.id.btn8)
        val btn9 = view.findViewById<Button>(R.id.btn9)
        val btn0 = view.findViewById<Button>(R.id.btn0)

        btn0.onClick {
            checkAnswer(0)
        }
        btn1.onClick {
            checkAnswer(1)
        }
        btn2.onClick {
            checkAnswer(2)
        }
        btn3.onClick {
            checkAnswer(3)
        }
        btn4.onClick {
            checkAnswer(4)
        }
        btn5.onClick {
            checkAnswer(5)
        }
        btn6.onClick {
            checkAnswer(6)
        }
        btn7.onClick {
            checkAnswer(7)
        }
        btn8.onClick {
            checkAnswer(8)
        }
        btn9.onClick {
            checkAnswer(9)
        }
    }

    private fun oddEvenKeyboard(){
        val view = findViewById<View>(R.id.layout_odd_even_keyboard)
        val viewHide = findViewById<View>(R.id.layout_keyboard)

        //layout_keyboard.layoutParams = LinearLayout.LayoutParams(0,0)
        viewHide.visibility = View.GONE
        view.visibility = View.VISIBLE

        val btnOdd = view.findViewById<Button>(R.id.btnOdd)
        val btnEven = view.findViewById<Button>(R.id.btnEven)

        btnOdd.onClick {
            checkAnswer(1)

            val handler = Handler()
            btnEven.isEnabled = false
            btnOdd.isEnabled = false
            handler.postDelayed({
                btnOdd.isEnabled = true
                btnEven.isEnabled = true
            }, 1000)
        }

        btnEven.onClick {
            checkAnswer(0)

            val handler = Handler()
            btnEven.isEnabled = false
            btnOdd.isEnabled = false
            handler.postDelayed({
                btnOdd.isEnabled = true
                btnEven.isEnabled = true
            }, 1000)
        }
    }

    private fun getFacebookProfilePicture(userID: String): String {
        return "https://graph.facebook.com/$userID/picture?type=large"
    }


    private fun fetchProfile(){

        when (player) {
            StatusPlayer.JoinFriend -> {
                Picasso.get().load(getFacebookProfilePicture(inviterFacebookId)).fit().into(ivOpponentImage)
                tvOpponentName.text = "" + inviterName
            }
            StatusPlayer.Inviter -> {
                Picasso.get().load(getFacebookProfilePicture(joinFriendFacebookId)).fit().into(ivOpponentImage)
                tvOpponentName.text = ""+joinFriendName
            }
            StatusPlayer.Creator -> {
                Picasso.get().load(getFacebookProfilePicture(joinOnlineFacebookId)).fit().into(ivOpponentImage)
                tvOpponentName.text = ""+joinOnlineName
            }
            StatusPlayer.JoinOnline -> {
                Picasso.get().load(getFacebookProfilePicture(creatorFacebookId)).fit().into(ivOpponentImage)
                tvOpponentName.text = ""+creatorName
            }
        }
        Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivPlayerImage)
        tvPlayerName.text = "" + Profile.getCurrentProfile().name
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

        tvMessageTitle.text = "Game Over"

        if (type == 1){
            layoutMessageInvitation.visibility = View.GONE
            layoutMessageBasic.visibility = View.VISIBLE
            layoutMessageReward.visibility = View.GONE

            btnReject.text = "Continue"
            btnClose.text = "No"
            tvMessageInfo.text = message

            btnClose.onClick {
                btnClose.startAnimation(clickAnimation)
                activity_normal_game.alpha = 1F
                popupWindow.dismiss()
                calculateRankReward()
            }

            btnReject.onClick {
                btnReject.startAnimation(clickAnimation)
                activity_normal_game.alpha = 1F
                popupWindow.dismiss()
                progress_bar.visibility = View.VISIBLE
                loadRewardAd()
                handler.postDelayed(runnable,5000)
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

        activity_normal_game.alpha = 0.1F

        TransitionManager.beginDelayedTransition(activity_normal_game)
        popupWindow.showAtLocation(
                activity_normal_game, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

    }

    override fun response(message: String) {
        if (message == "updateTournament"){
            finishRank()
        }
    }

    private fun finishRank(){
        startActivity(intentFor<PostGameActivity>("score" to point, "rewardCredit" to creditReward, "rewardPoint" to pointReward))
        finish()
    }

    fun calculateReward(){
        when(currentRank){
            Rank.Toddler.toString()->{
                pointReward = point * 4 / 100
                creditReward = point * 5 / 100
            }
            Rank.Beginner.toString()->{
                pointReward = point * 5 / 100
                creditReward = point * 6 / 100
            }
            Rank.Senior.toString()->{
                pointReward = point * 5 / 100
                creditReward = point * 6 / 100
            }
            Rank.Master.toString()->{
                pointReward = point * 6 / 100
                creditReward = point * 7 / 100
            }
            Rank.GrandMaster.toString()->{
                pointReward = point * 6 / 100
                creditReward = point * 7 / 100
            }
        }
        matchPresenter.updateCredit(creditReward.toLong(),auth)
        matchPresenter.updatePoint(pointReward.toLong(),auth)
    }

    override fun onDestroy() {
        //soundPool.release()
        countDownTimer.cancel()
        matchPresenter.dismissListener()
        super.onDestroy()
    }

    override fun onPause() {
        progress_bar.visibility = View.GONE
        handler.removeCallbacks(runnable)
        countDownTimer.cancel()
        matchPresenter.dismissListener()
        super.onPause()
    }

    override fun onBackPressed() {
//        database.child("onPlay").child(facebookId).child("pause").setValue(true)
//        if (player == StatusPlayer.JoinOnline || player == StatusPlayer.Creator)
//            toast("Cannot Exist in the Middle Game")
//        else{
//            countDownTimer.cancel()

        alert ("You'll be Leave on This Game"){
            title = "Exit"
            yesButton {
                // di isi
                finish()
            }
            noButton {
            }
        }.show()

    }

    fun addPointToTournament(tournamentType: String){
        val joinTournamentEndDate = sharedPreference.getString("joinTournament","")
        if (joinTournamentEndDate == tournamentEndDate){
            if (tournamentType == type.toString()){
                matchPresenter.updateTournament(auth, point.toLong())
            }
        }else{
            finishRank()
        }
    }

//    override fun onStart() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            val audioAttributes = AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                    .build()
//            soundPool = SoundPool.Builder()
//                    .setMaxStreams(100)
//                    .setAudioAttributes(audioAttributes)
//                    .build()
//        } else {
//            soundPool = SoundPool(100, AudioManager.STREAM_MUSIC, 0);
//        }
//        soundCorrect = soundPool.load(this,R.raw.answer_true,1)
//
//        super.onStart()
//    }

    override fun onResume() {
       // database.child("onPlay").child(facebookId).child("pause").setValue(false)
        control(true)

        super.onResume()
    }

    override fun fetchOpponentData(dataSnapshot: DataSnapshot, inviter: Boolean) {
        val check = opponentPoint

        opponentPoint = if (player == StatusPlayer.Inviter || player == StatusPlayer.Creator)
            dataSnapshot.getValue(Play::class.java)?.player2!!
        else
            dataSnapshot.getValue(Play::class.java)?.player1!!

        if (check != opponentPoint){ // jika value tidak sama dengan sebelumnya maka update data
            val animationBounce = AnimationUtils.loadAnimation(ctx, R.anim.bounce)
            tvOpponentPoint.text = "" + opponentPoint
            tvOpponentPoint.startAnimation(animationBounce)
        }
    }

//    override fun networkConnectivityChanged(event: Event) {
//        when (event) {
//            is Event.ConnectivityEvent -> {
//                if (event.state.isConnected) {
//                    showSnackBar(activity_normal_game, "The network is back !", "LONG")
//                } else {
//                    showSnackBar(activity_normal_game, "There is no more network", "INFINITE")
//                }
//            }
//        }
//    }

    override fun loadHighScore(score: Long) {
       highScore = score.toInt()
    }

    private fun loadRewardAd() : RewardedAd{
        if (rewardedAd.isLoaded) {
            val adCallback = object: RewardedAdCallback() {
                var watched = false
                override fun onRewardedAdOpened() {
                    progress_bar.visibility = View.GONE
                    continueGame = true
                }
                override fun onRewardedAdClosed() {
                    if (watched){
                        timer = defaultTimer
                        activity_normal_game.alpha = 1F
                        editor = sharedPreference.edit()
                        editor.putBoolean("continueRank",false)
                        editor.apply()
                    }else{
                        calculateRankReward()
                    }
                }
                override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                    watched = true

                }
                override fun onRewardedAdFailedToShow(errorCode: Int) {

                }
            }
            rewardedAd.show(this@NormalGameActivity, adCallback)
        }
        return  rewardedAd
    }

    override fun loadData(dataSnapshot: DataSnapshot, message: String) {
        if(message == "fetchTournamentType"){
            if (dataSnapshot.exists()){
                if (dataSnapshot.getValue(TournamentData::class.java)!!.type == type.toString()) {
                    addPointToTournament(dataSnapshot.getValue(TournamentData::class.java)!!.type.toString())
                }else
                    finishRank()
            }else if(!dataSnapshot.exists())
                finishRank()
        }else if(message == "getTournamentEndDate"){
            if (dataSnapshot.exists()){
                for ((index,data) in dataSnapshot.children.withIndex()){
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val currentDate = Date().time
                    val tournamentDate = sdf.parse(data.key.toString()).time
                    val diff: Long = tournamentDate - currentDate

                    if(diff > 0){
                        tournamentEndDate = data.key.toString()
                        matchPresenter.loadTournamentType(tournamentEndDate)
                    }else
                        finishRank()
                }
            }else if(!dataSnapshot.exists()){
                finishRank()
            }
        }
    }

    fun calculateRankReward(){
        progress_bar.visibility = View.VISIBLE
        updateRank(currentRank)
        calculateReward()
        matchPresenter.getTournamentType()
    }

    fun updateRank(currentRank: String){
        when(enumValueOf<Rank>(currentRank)){
            Rank.Toddler -> {
                if (type == GameType.Normal){
                    editor = sharedPreference.edit()
                    editor.putInt("toddler1",1)
                    editor.putInt("toddler2",point)
                    editor.apply()
                }
            }
            Rank.Beginner -> {
                if (player == StatusPlayer.JoinFriend || player == StatusPlayer.Inviter){
                    val progress3 = sharedPreference.getInt("beginner3",0)
                    editor = sharedPreference.edit()
                    editor.putInt("beginner3",progress3+1)
                    editor.apply()
                }else{
                    if (type == GameType.Normal){
                        val progress2 = sharedPreference.getInt("beginner2",0)
                        if (point > progress2){
                            editor = sharedPreference.edit()
                            editor.putInt("beginner2",point)
                            editor.apply()
                        }

                    }else if(type == GameType.OddEven){
                        val progress1 = sharedPreference.getInt("beginner1",0)
                        if (point > progress1){
                            editor = sharedPreference.edit()
                            editor.putInt("beginner1",point)
                            editor.apply()
                        }
                    }
                }
            }
            Rank.Senior -> {
                if (type == GameType.AlphaNum){
                    val progress1 = sharedPreference.getInt("senior1",0)
                    if (point > progress1){
                        editor = sharedPreference.edit()
                        editor.putInt("senior1",point)
                        editor.apply()
                    }

                }else if(type == GameType.Rush){
                    val progress2 = sharedPreference.getInt("senior2",0)
                    if (point > progress2){
                        editor = sharedPreference.edit()
                        editor.putInt("senior2",point)
                        editor.apply()
                    }
                }else if (type == GameType.Normal){
                    val progress3 = sharedPreference.getInt("senior3",0)
                    if (point > progress3){
                        editor = sharedPreference.edit()
                        editor.putInt("senior3",point)
                        editor.apply()
                    }
                }
            }
            Rank.Master -> {
                if (type == GameType.Rush){
                    val progress1 = sharedPreference.getInt("master2",0)
                    if (point > progress1){
                        editor = sharedPreference.edit()
                        editor.putInt("master2",point)
                        editor.apply()
                    }

                }else if(type == GameType.Mix){
                    val progress2 = sharedPreference.getInt("master3",0)
                    if (point > progress2){
                        editor = sharedPreference.edit()
                        editor.putInt("master3",point)
                        editor.apply()
                    }
                }else if (type == GameType.Normal){
                    val progress3 = sharedPreference.getInt("master4",0)
                    if (point > progress3){
                        editor = sharedPreference.edit()
                        editor.putInt("master4",point)
                        editor.apply()
                    }
                }
            }
            Rank.GrandMaster -> {
                if (type == GameType.DoubleAttack){
                    val progress1 = sharedPreference.getInt("gMaster3",0)
                    if (point > progress1){
                        editor = sharedPreference.edit()
                        editor.putInt("gMaster3",point)
                        editor.apply()
                    }

                }else if(type == GameType.Mix){
                    val progress2 = sharedPreference.getInt("gMaster4",0)
                    if (point > progress2){
                        editor = sharedPreference.edit()
                        editor.putInt("gMaster4",point)
                        editor.apply()
                    }
                }
            }
        }
    }
}

data class Play(
    var player1: Int? = 0,
    var player2: Int? = 0
)

enum class GameType(var price: Long){
    Normal(0),
    OddEven(100),
    Rush(300),
    AlphaNum(500),
    Mix(800),
    DoubleAttack(1000)
}

enum class StatusPlayer{
    Single,
    Inviter,
    JoinFriend,
    Creator,
    JoinOnline,
    Rank
}