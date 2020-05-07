package com.example.balapplat.play

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.transition.TransitionManager
import com.example.balapplat.main.MainActivity
import com.example.balapplat.presenter.Presenter
import com.example.balapplat.R
import com.example.balapplat.model.Inviter
import com.example.balapplat.presenter.MatchPresenter
import com.example.balapplat.rank.Rank
import com.example.balapplat.tournament.TournamentData
import com.example.balapplat.utils.showSnackBar
import com.example.balapplat.view.MainView
import com.example.balapplat.view.MatchView
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_normal_game.*
import kotlinx.android.synthetic.main.activity_normal_game.tvPoint
import kotlinx.android.synthetic.main.activity_rank.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import java.util.*

class NormalGameActivity : AppCompatActivity(), NetworkConnectivityListener, MatchView {

    private lateinit var sharedPreference: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    lateinit var matchPresenter: MatchPresenter
    private lateinit var countDownTimer : CountDownTimer
    lateinit var data: Inviter
    lateinit var editor: SharedPreferences.Editor
    var creditReward = 0
    var pointReward = 0
    var creatorFacebookId = ""
    var creatorName = ""
    var joinOnlineFacebookId = ""
    var joinOnlineName = ""
    var joinFriendFacebookId = ""
    var joinFriendName = ""
    var inviterFacebookId = ""
    var inviterName = ""
    var count = 4
    var point = 0
    var timer = 45
    var defaultTimer = 45
    var answer = 999
    var highScore = 0
    var opponentPoint = 0
    var type = GameType.Normal
    var mix = false
    var player = StatusPlayer.Single
    private lateinit var popupWindow : PopupWindow
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)

    private var numberArr : MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_game)

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
        tvTimerTitle.typeface = typeface
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
                    value = Random().nextInt(10) + 65
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
        toast(""+answer)
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
            }else{
                if(point > 4)
                    point -= 4
                else
                    point = 0
            }
            countDownTimer.cancel()
            timer = defaultTimer
            control(true)
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

        if (player != StatusPlayer.Single){
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

    private fun control(status : Boolean){
        countDownTimer = object: CountDownTimer(timer.toLong()*1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timer--
                tvTimer.text = "" + timer

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
                        toast("your point : "+ point + "opponent point : " + opponentPoint)

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
                                toast("1.  your point : "+ point + "opponent point : " + opponentPoint)
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
                            startActivity(intentFor<PostGameActivity>("scorePlayer" to point,
                                    "scoreOpponent" to opponentPoint, "opponentName" to joinFriendName, "opponentFacebookId" to joinFriendFacebookId, "gameResult" to text))
                            finish()
                        }
                        StatusPlayer.JoinFriend -> {
                            matchPresenter.addToHistory(auth,point,opponentPoint, inviterFacebookId,
                                    inviterName,"friend"
                            )
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
                                calculateReward()
                                matchPresenter.getTournamentType()
                                startActivity(intentFor<PostGameActivity>("score" to point, "rewardCredit" to creditReward, "rewardPoint" to pointReward))
                                finish()
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
        }

        btnEven.onClick {
            checkAnswer(0)
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
                Picasso.get().load(getFacebookProfilePicture(joinOnlineFacebookId)).fit().into(ivOpponentImage)
                tvOpponentName.text = ""+joinOnlineName
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
                calculateReward()
                startActivity(intentFor<PostGameActivity>("score" to point, "rewardCredit" to creditReward, "rewardPoint" to pointReward))
                finish()
            }

            btnReject.onClick {
                btnReject.startAnimation(clickAnimation)
                matchPresenter.getTournamentType()
                timer = defaultTimer
                control(true)
                activity_normal_game.alpha = 1F
                popupWindow.dismiss()
                editor = sharedPreference.edit()
                editor.putBoolean("continueRank",false)
                editor.apply()
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
        toast(""+ message)
    }

    fun calculateReward(){
        val currentRank = sharedPreference.getString("currentRank", Rank.Toddler.toString())

        when(currentRank){
            Rank.Toddler.toString()->{
                pointReward = point * 3 / 100
                creditReward = point * 5 / 100
            }
        }
        matchPresenter.updateCredit(creditReward.toLong(),auth)
        matchPresenter.updatePoint(pointReward.toLong(),auth)
    }

    override fun onDestroy() {
        countDownTimer.cancel()
        matchPresenter.dismissListener()
        super.onDestroy()
    }

    override fun onPause() {
        countDownTimer.cancel()
        matchPresenter.dismissListener()
        super.onPause()
    }

    override fun onBackPressed() {
//        database.child("onPlay").child(facebookId).child("pause").setValue(true)
        if (player == StatusPlayer.JoinOnline || player == StatusPlayer.Creator)
            toast("Cannot Exist in the Middle Game")
        else{
            countDownTimer.cancel()

        alert {
            title = "Exit"
            yesButton {
                // di isi
                finish()
            }
            noButton {
                control(true)
            }
        }.show()
        }

    }

    fun addPointToTournament(tournamentType: String){
        val joinTournament = sharedPreference.getBoolean("joinTournament",false)
        if (joinTournament){
            if (tournamentType == type.toString()){
                matchPresenter.updateTournament(auth)
            }
        }
    }

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

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(activity_normal_game, "The network is back !", "LONG")
                } else {
                    showSnackBar(activity_normal_game, "There is no more network", "INFINITE")
                }
            }
        }
    }

    override fun loadHighScore(score: Long) {
       highScore = score.toInt()
    }

    override fun loadData(dataSnapshot: DataSnapshot, message: String) {
        if(message == "fetchTournamentType"){
            dataSnapshot.getValue(TournamentData::class.java)!!.type?.let { addPointToTournament(it) }
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