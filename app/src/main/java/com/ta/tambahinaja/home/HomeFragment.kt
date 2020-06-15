package com.ta.tambahinaja.home

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.ta.tambahinaja.R
import com.ta.tambahinaja.TutorialActivity
import com.ta.tambahinaja.friends.FriendsActivity
import com.ta.tambahinaja.leaderboard.LeaderBoardActivity
import com.ta.tambahinaja.main.LoginActivity
import com.ta.tambahinaja.main.MainActivity
import com.ta.tambahinaja.main.Reward
import com.ta.tambahinaja.model.Inviter
import com.ta.tambahinaja.play.CountdownActivity
import com.ta.tambahinaja.play.GameType
import com.ta.tambahinaja.play.StatusPlayer
import com.ta.tambahinaja.play.WaitingActivity
import com.ta.tambahinaja.presenter.HomePresenter
import com.ta.tambahinaja.rank.AvailableGame
import com.ta.tambahinaja.rank.Rank
import com.ta.tambahinaja.rank.RankActivity
import com.ta.tambahinaja.tournament.FragmentListener
import com.ta.tambahinaja.utils.showSnackBar
import com.ta.tambahinaja.view.MainView
import com.facebook.AccessToken
import com.facebook.Profile
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import com.squareup.picasso.Picasso
import com.ta.tambahinaja.play.practice.PracticeActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_rank.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.fragment_home
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.ctx
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(), NetworkConnectivityListener,MainView {

    private lateinit var callback: FragmentListener
    private lateinit var homePresenter: HomePresenter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor : SharedPreferences.Editor
    private var dataInviter : Inviter = Inviter()
    private lateinit var reward: Reward
    private var countPuzzle = 0
    private var showPopup = false
    private lateinit var customGameAdapter: CustomGameRecyclerViewAdapter
    private lateinit var currentRank : String
    private var puzzleType = 1
    private var puzzleAnswer = 0
    private var credit = 0
    private var coin = 0
    private var playedPuzzleToday = false
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)
    private lateinit var popupWindow : PopupWindow
    private var currentDate = ""
    private var handler = Handler()
    private lateinit var runnable : Runnable
    private var fragmentActive = false
    private var numberArr : MutableList<Int> = mutableListOf()
    private val availableGameList : MutableList<Boolean> = mutableListOf()
    private var mLayout = 0

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mLayout = savedInstanceState?.getInt("layoutId") ?: R.layout.fragment_home

        runnable = Runnable {
            setSeasonTimer()
            firebaseSingleListenerRepeat()
        }

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        return inflater.inflate(mLayout, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("layoutId",mLayout)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        fragmentActive = true
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        homePresenter = HomePresenter(this,database)
        callback = activity as MainActivity
        sharedPreference =  ctx.getSharedPreferences("LOCAL_DATA",Context.MODE_PRIVATE)
        currentRank = sharedPreference.getString("currentRank", Rank.Toddler.toString()).toString()

        val sdf = SimpleDateFormat("dd MMM yyyy")
        currentDate = sdf.format(Date())

        val animationBounce = AnimationUtils.loadAnimation(ctx, R.anim.fade_in)
        btnRank.startAnimation(animationBounce)

        val typeface = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)

        tvSeason.typeface = typeface
        tvCredit.typeface = typeface
        btnCustomPlay.typeface = typeface
        btnRank.typeface = typeface
        btnPractice.typeface = typeface
        btnPlayFriend.typeface = typeface
        btnOnline.typeface = typeface
        btnLeaderboard.typeface = typeface
        tvTitle.typeface = typeface
        tvCoin.typeface = typeface

        if (auth.currentUser != null){
            homePresenter.checkDailyPuzzle()
            homePresenter.fetchCredit()
            homePresenter.fetchCoin()
        }


        ivTutorial.onClick {
            startActivity<TutorialActivity>()
        }

        btnCustomPlay.onClick {
            if (auth.currentUser != null)
                homePresenter.fetchAvailableGame()
            else{
                availableGameList.clear()
                availableGameList.add(true)
                availableGameList.add(false)
                availableGameList.add(false)
                availableGameList.add(false)
                availableGameList.add(false)
                availableGameList.add(false)

            }
            popUpCustomGame()
        }

        btnNext.onClick {
            btnRank.visibility = View.GONE
            btnPractice.visibility = View.VISIBLE
            btnNext.startAnimation(clickAnimation)
            btnPractice.startAnimation(animationBounce)
        }

        btnPrev.onClick {
            btnRank.visibility = View.VISIBLE
            btnPractice.visibility = View.GONE
            btnPrev.startAnimation(clickAnimation)
            btnRank.startAnimation(animationBounce)
        }

        btnRank.onClick {
            btnRank.startAnimation(clickAnimation)
            if(auth.currentUser == null)
                popUpLogin()
            else{
                startActivity<RankActivity>()
            }
        }

        btnPractice.onClick {
            btnPractice.startAnimation(clickAnimation)
            if(auth.currentUser == null)
                popUpLogin()
            else{
                startActivity<PracticeActivity>()
            }
        }

        btnPlayFriend.onClick {

            if(auth.currentUser == null)
                popUpLogin()
            else
                startActivity(intentFor<FriendsActivity>().clearTask())
        }

        btnOnline.onClick {
            if(auth.currentUser == null)
                popUpLogin()
            else {
                startActivity(intentFor<WaitingActivity>("playOnline" to true))
            }
        }

        btnLeaderboard.onClick {
            startActivity(intentFor<LeaderBoardActivity>().clearTask())
        }

        ivDailyPuzzle.onClick {
                popUp()
        }

        ivShop.onClick {

            ivShop.startAnimation(clickAnimation)
            startActivity<MarketActivity>()
        }

        cvCredit.onClick {
            cvCredit.startAnimation(clickAnimation)
            startActivity<CreditActivity>()
        }

        cvCoin.onClick {
            cvCoin.startAnimation(clickAnimation)
            startActivity<MarketActivity>()
        }

        if(AccessToken.getCurrentAccessToken() == null)
            auth.signOut()

        setSeasonTimer()
        firebaseSingleListenerRepeat()

        super.onStart()
    }

    private fun checkPlayedPuzzle(date: String) {

        if (date != currentDate){
            playedPuzzleToday = true
            val animationFadeIn = AnimationUtils.loadAnimation(ctx, R.anim.fade_in)
            ivDailyPuzzle.startAnimation(animationFadeIn)
            ivDailyPuzzle.visibility = View.VISIBLE
            tvPuzzleInfo.visibility = View.VISIBLE

            val animationBounce = AnimationUtils.loadAnimation(ctx, R.anim.bounce)
            animationBounce.repeatCount = Animation.INFINITE
            animationBounce.repeatMode = Animation.REVERSE

            tvPuzzleInfo.startAnimation(animationBounce)

        }else{
            tvPuzzleInfo.visibility = View.GONE
            ivDailyPuzzle.visibility = View.GONE
        }

    }


    fun getFacebookProfilePicture(userID: String): String {
        return "https://graph.facebook.com/$userID/picture?type=large"
    }

    @SuppressLint("SetTextI18n")
    private fun generate(tvDailyPuzzleQuestion: TextView){

        var difficulty = 0

        tvDailyPuzzleQuestion.text = ""
        numberArr.clear()

        when(enumValueOf<Rank>(currentRank)){
            Rank.Toddler ->{
                difficulty = 0
                countPuzzle = 5
            }
            Rank.Beginner ->{
                difficulty = 0
                countPuzzle = 6
            }
            Rank.Senior ->{
                difficulty = 8
                countPuzzle = 6
            }
            Rank.Master ->{
                difficulty = 12
                countPuzzle = 8
            }
            Rank.GrandMaster ->{
                difficulty = 15
                countPuzzle = 10
            }
        }



        for(x in 0 until countPuzzle)
        {
            var value = 0
            var choose = 0

            if (difficulty != 0)
                choose = Random().nextInt(2)

            if (choose == 1){
                value = Random().nextInt(difficulty) + 65
                numberArr.add(value)
                tvDailyPuzzleQuestion.text = tvDailyPuzzleQuestion.text.toString() + value.toChar()
            }else{
                value = Random().nextInt(9)
                numberArr.add(value)
                tvDailyPuzzleQuestion.text = tvDailyPuzzleQuestion.text.toString() + value
            }
        }

        puzzleAnswer = generateAnswer()

        editor = sharedPreference.edit()
        editor.putInt("puzzleAnswer",puzzleAnswer)
        editor.putString("puzzleQuestion",tvDailyPuzzleQuestion.text.toString())
        editor.apply()

    }

    private fun generateAnswer() : Int {
        var result = 0
        val temp = numberArr
        for(x in 0 until countPuzzle)
        {
            if(x != countPuzzle-1) {
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
        return result
    }

    private fun checkAnswer(value : String) {
        if (value == puzzleAnswer.toString()){

            if (currentRank == Rank.Beginner.toString()){
                val progress4 = sharedPreference.getInt("beginner4",0)
                editor = sharedPreference.edit()
                editor.putInt("beginner4",progress4+1)
                editor.apply()
            }else if (currentRank == Rank.Master.toString()){
                val progress5 = sharedPreference.getInt("master5",0)
                editor = sharedPreference.edit()
                editor.putInt("master5",progress5+1)
                editor.apply()
            }
            popupWindow.dismiss()
            fragment_home.alpha = 1F
            homePresenter.rewardPuzzlePopUp()

            editor = sharedPreference.edit()
            editor.remove("puzzleAnswer")
            editor.remove("puzzleQuestion")
            editor.apply()
        }else{
            toast("Oops Try Again Tommorow")
            popupWindow.dismiss()
            fragment_home.alpha = 1F
        }

    }

    private fun popUp(){
        val inflater:LayoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.puzzle_pop_up,null)
        val main_view = inflater.inflate(R.layout.activity_main,null)

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


        val main_activity = main_view.findViewById<RelativeLayout>(R.id.activity_main)
        val dailyPuzzleTitle = view.findViewById<TextView>(R.id.tvDailyPuzzleTitle)

        val typeface : Typeface? = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
        dailyPuzzleTitle.typeface = typeface

        fragment_home.alpha = 0.1F
        main_activity.alpha = 0.1F

        val btnAnswerPuzzle = view.findViewById<Button>(R.id.btnAnswerPuzzle)
        val tvAnswerPuzzle = view.findViewById<TextView>(R.id.tvAnswerPuzzle)
        val btnClose = view.findViewById<Button>(R.id.btnClose)
        val tvDailyPuzzleQuestion = view.findViewById<TextView>(R.id.tvDailyPuzzleQuestion)
        tvDailyPuzzleQuestion.typeface = typeface
        btnAnswerPuzzle.typeface = typeface
        tvAnswerPuzzle.typeface = typeface

        val keyboardView= view.findViewById<View>(R.id.layout_keyboard)

        val btn1 = keyboardView.findViewById<Button>(R.id.btn1)
        val btn3 = keyboardView.findViewById<Button>(R.id.btn3)
        val btn4 = keyboardView.findViewById<Button>(R.id.btn4)
        val btn5 = keyboardView.findViewById<Button>(R.id.btn5)
        val btn6 = keyboardView.findViewById<Button>(R.id.btn6)
        val btn7 = keyboardView.findViewById<Button>(R.id.btn7)
        val btn8 = keyboardView.findViewById<Button>(R.id.btn8)
        val btn9 = keyboardView.findViewById<Button>(R.id.btn9)
        val btn2 = keyboardView.findViewById<Button>(R.id.btn2)
        val btn0 = keyboardView.findViewById<Button>(R.id.btn0)

        btnAnswerPuzzle.onClick {
            if (tvAnswerPuzzle.text.toString() == "")
                toast("Please Select Your Answer")
            else{
                checkAnswer(tvAnswerPuzzle.text.toString())
                ivDailyPuzzle.visibility = View.GONE
                tvPuzzleInfo.text = ""
                homePresenter.updatePuzzle()
            }

        }

        btnClose.onClick {
            btnClose.startAnimation(clickAnimation)
            fragment_home.alpha = 1F
            main_activity.alpha = 1F
            popupWindow.dismiss()
        }

        btn0.onClick {
            tvAnswerPuzzle.text = "0"
        }
        btn1.onClick {
            tvAnswerPuzzle.text = "1"
        }
        btn2.onClick {
            tvAnswerPuzzle.text = "2"
        }
        btn3.onClick {
            tvAnswerPuzzle.text = "3"
        }
        btn4.onClick {
            tvAnswerPuzzle.text = "4"
        }
        btn5.onClick {
            tvAnswerPuzzle.text = "5"
        }
        btn6.onClick {
            tvAnswerPuzzle.text = "6"
        }
        btn7.onClick {
            tvAnswerPuzzle.text = "7"
        }
        btn8.onClick {
            tvAnswerPuzzle.text = "8"
        }
        btn9.onClick {
            tvAnswerPuzzle.text = "9"
        }

        if(sharedPreference.getInt("puzzleAnswer",0) == 0)
            generate(tvDailyPuzzleQuestion)
        else{
            puzzleAnswer = sharedPreference.getInt("puzzleAnswer",0)
            tvDailyPuzzleQuestion.text = sharedPreference.getString("puzzleQuestion","0000")
        }

        // Finally, show the popup window on app
        TransitionManager.beginDelayedTransition(fragment_home)
        popupWindow.showAtLocation(
            fragment_home, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )

    }

    private fun popUpCustomGame(){
        val inflater:LayoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.pop_up_custom_game,null)
        val main_view = inflater.inflate(R.layout.activity_main,null)

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
        var position = -1
        var timer = 30
        val main_activity = main_view.findViewById<RelativeLayout>(R.id.activity_main)
        val rvCustomGame = view.findViewById<RecyclerView>(R.id.rvCustomGame)
        val ivCustomGame = view.findViewById<ImageView>(R.id.ivCustomGame)
        val tvCustomGameName = view.findViewById<TextView>(R.id.tvCustomGameName)
        val tvChooseGame = view.findViewById<TextView>(R.id.tvClickToChooseGame)
        val tvCustomGameTitle = view.findViewById<TextView>(R.id.tvCustomGameTitle)
        val tvCustomGameTime = view.findViewById<TextView>(R.id.tvCustomGameTime)
        val sbTime = view.findViewById<SeekBar>(R.id.sbTime)
        val btnPlay = view.findViewById<Button>(R.id.btnStartCustomGame)
        val btnClose = view.findViewById<Button>(R.id.btnClose)

        val typeface : Typeface? = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
        tvCustomGameName.typeface = typeface
        tvCustomGameTitle.typeface = typeface
        tvCustomGameTime.typeface = typeface

        timer = 30
        tvCustomGameTime.text = "Time : ${timer}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sbTime.min = 30
        }

        sbTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    timer = progress
                    tvCustomGameTime.text = "Time : $timer"
                }else{
                    if (position == 2){
                        timer = progress/10
                        tvCustomGameTime.text = "Time : $timer"
                    }else {
                        timer = progress
                        tvCustomGameTime.text = "Time : $timer"
                    }
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    timer = seekBar.progress
                }
            }

        })

        customGameAdapter = CustomGameRecyclerViewAdapter(ctx,availableGameList){
            if (!availableGameList[it]){
                toast("Not Available")
            }else{
                sbTime.progress = 30
                timer = 30
                tvCustomGameTime.text = "Time : $timer"
                position = it
                when(position){
                    0 -> {
                        tvCustomGameName.text = "Normal Game"
                        ivCustomGame.setImageResource(R.drawable.normal_game)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sbTime.min = 30
                            sbTime.max = 90
                        }
                    }
                    1 -> {
                        tvCustomGameName.text = "Odd Even"
                        ivCustomGame.setImageResource(R.drawable.odd_even_game)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sbTime.min = 30
                            sbTime.max = 90
                        }
                    }
                    2 -> {
                        tvCustomGameName.text = "Rush"
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sbTime.min = 3
                            sbTime.max = 8

                            sbTime.progress = 3
                            timer = 3
                            tvCustomGameTime.text = "Time : $timer"
                        }
                        ivCustomGame.setImageResource(R.drawable.rush_game)
                    }
                    3 -> {
                        tvCustomGameName.text = "Alpha Num"
                        ivCustomGame.setImageResource(R.drawable.alpha_num_game)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sbTime.min = 30
                            sbTime.max = 90
                        }
                    }
                    4 -> {
                        tvCustomGameName.text = "Mix"
                        ivCustomGame.setImageResource(R.drawable.mix_game)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sbTime.min = 30
                            sbTime.max = 90
                        }
                    }
                    5 -> {
                        tvCustomGameName.text = "Double Attack"
                        ivCustomGame.setImageResource(R.drawable.double_attack_game)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sbTime.min = 30
                            sbTime.max = 90
                        }
                    }
                }
                rvCustomGame.visibility = View.GONE
                tvCustomGameName.visibility = View.VISIBLE
                ivCustomGame.visibility = View.VISIBLE
                tvChooseGame.visibility = View.VISIBLE
                tvChooseGame.text = "Click to Choose Other Games"
            }

        }
        rvCustomGame.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL,false)

        ivCustomGame.onClick {
            tvCustomGameName.visibility = View.GONE
            tvChooseGame.visibility = View.GONE
            ivCustomGame.visibility = View.GONE
            rvCustomGame.visibility = View.VISIBLE
            position = -1
        }

        btnClose.onClick {
            btnClose.startAnimation(clickAnimation)
            fragment_home.alpha = 1F
            main_activity.alpha = 1F
            popupWindow.dismiss()
        }

        btnPlay.onClick {
            if (position == -1)
                toast("Choose game First")
            else{
                fragment_home.alpha = 1F
                main_activity.alpha = 1F
                popupWindow.dismiss()
                when (position) {
                    0 -> {
                        startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Single,
                                "type" to GameType.Normal, "timer" to timer))
                    }
                    1 -> {
                        startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Single,
                                "type" to GameType.OddEven, "timer" to timer))
                    }
                    2 -> {
                        startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Single,
                                "type" to GameType.Rush, "timer" to timer))
                    }
                    3 -> {
                        startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Single,
                                "type" to GameType.AlphaNum, "timer" to timer))
                    }
                    4 ->{
                        startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Single,
                                "type" to GameType.Mix, "timer" to timer))
                    }
                    5 ->{
                        startActivity(intentFor<CountdownActivity>("status" to StatusPlayer.Single,
                                "type" to GameType.DoubleAttack, "timer" to timer))
                    }
                }
            }

        }

        rvCustomGame.adapter = customGameAdapter
        fragment_home.alpha = 0.1F
        main_activity.alpha = 0.1F

        TransitionManager.beginDelayedTransition(fragment_home)
        popupWindow.showAtLocation(
                fragment_home, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

    }


    private fun popUpLogin(){
        val inflater: LayoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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

        layoutMessageInvitation.visibility = View.GONE
        layoutMessageBasic.visibility = View.VISIBLE
        layoutMessageReward.visibility = View.GONE

        btnReject.text = "Log In"
        btnClose.text = "Later"
        tvMessageInfo.text = "You Must Login First"

        btnClose.typeface = typeface
        btnReject.typeface = typeface
        tvMessageInfo.typeface = typeface

        btnClose.onClick {
            btnClose.startAnimation(clickAnimation)
            fragment_home.alpha = 1F
            popupWindow.dismiss()
        }

        btnReject.onClick {
            btnReject.startAnimation(clickAnimation)
            startActivity<LoginActivity>()
            fragment_home.alpha = 1F
            popupWindow.dismiss()
        }

        tvMessageTitle.typeface = typeface
        tvMessageInfo.typeface = typeface

        fragment_home.alpha = 0.1F

        TransitionManager.beginDelayedTransition(fragment_home)
        popupWindow.showAtLocation(
                fragment_home, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

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

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (context != null && fragmentActive){
            if (response == "dailyPuzzle"){
                val date = dataSnapshot.value
                checkPlayedPuzzle(date.toString())
            }else if(response == "fetchCredit"){
                credit = dataSnapshot.value.toString().toInt()
                tvCredit.text = "${credit}"
            }else if(response == "fetchCoin"){
                coin = dataSnapshot.value.toString().toInt()
                tvCoin.text = "${coin}"
            }else if(response == "updateCredit"){
                homePresenter.fetchCredit()
            }else if(response == "availableGame"){
                if (dataSnapshot.exists()){
                    availableGameList.clear()
                    availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.normal!!)
                    availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.oddEven!!)
                    availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.rush!!)
                    availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.alphaNum!!)
                    availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.mix!!)
                    availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.doubleAttack!!)
                }
                else{
                    availableGameList.clear()
                    availableGameList.add(true)
                    availableGameList.add(false)
                    availableGameList.add(false)
                    availableGameList.add(false)
                    availableGameList.add(false)
                    availableGameList.add(false)
                }
                customGameAdapter.notifyDataSetChanged()

            }else if(response == "reward"){
                reward = dataSnapshot.getValue(Reward::class.java)!!
                if (!showPopup){
                    showPopup = true
                    popUpMessage(com.ta.tambahinaja.friends.Message.ReadOnly,reward.description.toString())
                }
            }else{
                dataInviter = dataSnapshot.getValue(Inviter::class.java)!!
                if (!showPopup){
                    showPopup = true
                    popUpMessage(com.ta.tambahinaja.friends.Message.Reply,"${dataInviter.name} invited you to play")
                }
            }
        }

    }

    private fun popUpMessage(type: com.ta.tambahinaja.friends.Message, message: String){
        if (context != null){
            val inflater: LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            val view = inflater.inflate(R.layout.pop_up_message,null)

            // Initialize a new instance of popup window
            popupWindow = PopupWindow(
                    view, // Custom view to show in popup window
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    true
            )

            // Set an elevation for the popup window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.elevation = 10.0F
            }
            val typeface : Typeface? = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)

            val layoutMessageInvitation = view.findViewById<LinearLayout>(R.id.layout_message_invitation)
            val layoutMessageBasic = view.findViewById<LinearLayout>(R.id.layout_message_basic)
            val layoutMessageReward = view.findViewById<LinearLayout>(R.id.layout_message_reward)
            val btnClose = view.findViewById<Button>(R.id.btnMessageClose)
            val btnReject = view.findViewById<Button>(R.id.btnMessageReject)
            val tvMessageTitle = view.findViewById<TextView>(R.id.tvMessageTitle)
            val ivInviter = view.findViewById<CircleImageView>(R.id.ivInviter)
            val tvMessageInviter = view.findViewById<TextView>(R.id.tvMessageInviter)

            if (type == com.ta.tambahinaja.friends.Message.Reply){
                layoutMessageInvitation.visibility = View.VISIBLE
                layoutMessageBasic.visibility = View.GONE
                layoutMessageReward.visibility = View.GONE

                btnClose.onClick {
                    showPopup = false
                    homePresenter.replyInvitation(true)
                    btnClose.startAnimation(clickAnimation)
                    fragment_home.alpha = 1F
                    popupWindow.dismiss()
                }

                btnReject.onClick {
                    showPopup = false
                    homePresenter.replyInvitation(false)
                    btnReject.startAnimation(clickAnimation)
                    fragment_home.alpha = 1F
                    popupWindow.dismiss()
                }

                Picasso.get().load(getFacebookProfilePicture(dataInviter.facebookId!!)).fit().into(ivInviter)
                tvMessageInviter.text = message
            }else if (type == com.ta.tambahinaja.friends.Message.ReadOnly){
                val layoutMessageReward = view.findViewById<LinearLayout>(R.id.layout_message_reward)
                val ivMessageReward = view.findViewById<ImageView>(R.id.ivMessageReward)
                val tvMessageReward = view.findViewById<TextView>(R.id.tvMessageReward)

                layoutMessageReward.visibility = View.VISIBLE
                layoutMessageInvitation.visibility = View.GONE
                btnReject.visibility = View.GONE

                tvMessageTitle.text = "Reward"
                tvMessageReward.text = message
                tvMessageReward.typeface = typeface

                if (reward.type == "credit")
                    ivMessageReward.setImageResource(R.drawable.credit)
                else if (reward.type == "point")
                    ivMessageReward.setImageResource(R.drawable.money_bag)

                btnClose.onClick {
                    showPopup = false
                    btnClose.startAnimation(clickAnimation)
                    fragment_home.alpha = 1F
                    popupWindow.dismiss()
                    homePresenter.removePopUpReward()

                    val lastCredit = sharedPreference.getInt("lastCredit",0)
                    animateTextView(lastCredit,lastCredit + reward.quantity!!.toInt(),tvCredit)
                }
            }

            tvMessageTitle.typeface = typeface
            tvMessageInviter.typeface = typeface

            fragment_home.alpha = 0.1F

            if (fragment_home != null && isAdded)
                TransitionManager.beginDelayedTransition(fragment_home)

            popupWindow.showAtLocation(
                    getView(), // Location to display popup window
                    Gravity.CENTER, // Exact position of layout to display popup
                    0, // X offset
                    0 // Y offset
            )
        }

    }

    override fun response(message: String) {
        if (context != null && fragmentActive){
            if (message === "acceptedGame"){
                startActivity(intentFor<CountdownActivity>("inviterFacebookId" to dataInviter.facebookId,
                        "inviterName" to dataInviter.name,
                        "status" to StatusPlayer.JoinFriend,
                        "type" to dataInviter.type as GameType,
                        "timer" to dataInviter.timer))
            }else if(message === "dismissInvitation"){
                //popUpMessage(com.ta.tambahinaja.friends.Message.ReadOnly,"You Have been Rejected")
                toast("You Have been Rejected")
            }else if(message === "rewardPuzzlePopUp"){
                homePresenter.updateCredit(credit.toLong() + 20)
            }
        }
    }

    private fun setSeasonTimer(){
        val currentDate = Date().time

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 1)
        calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 0)

        val seasonEnd = calendar.time.time

        val diff = seasonEnd - currentDate

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        when {
            days >= 1 -> tvSeason.text = "Season End : ${days} Days ${hours%24} Hours"
            hours in 1..23 -> tvSeason.text = "Season End : ${hours} Hours ${minutes%60} Minutes"
            minutes in 1..59 -> tvSeason.text = "Season End : ${minutes}"
            minutes >= 0 -> tvSeason.text = "Season End : Less Than 1 Minute"
            else -> tvSeason.text = "End"
        }

    }

    private fun animateTextView(initialValue : Int,finalValue : Int,textView : TextView) {

        val valueAnimator = ValueAnimator.ofInt(initialValue, finalValue);
        valueAnimator.duration = 1500

        valueAnimator.addUpdateListener { textView.text = valueAnimator.animatedValue.toString() }
        valueAnimator.start()

        editor = sharedPreference.edit()
        editor.putInt("lastCredit",credit)
        editor.apply()
    }

    private fun firebaseSingleListenerRepeat(){
        if(auth.currentUser != null && Profile.getCurrentProfile() != null){
            homePresenter.receiveInvitation()
            homePresenter.receiveReward()
        }

        handler.postDelayed(runnable,2000)
    }

    override fun onPause() {
        fragmentActive = false
        handler.removeCallbacks(runnable)
        homePresenter.dismissListener()
        super.onPause()
    }
}
