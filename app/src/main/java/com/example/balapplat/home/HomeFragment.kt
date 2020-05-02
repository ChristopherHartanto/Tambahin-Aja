package com.example.balapplat.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Layout
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
import com.example.balapplat.R
import com.example.balapplat.friends.FriendsActivity
import com.example.balapplat.leaderboard.LeaderBoardActivity
import com.example.balapplat.main.LoginActivity
import com.example.balapplat.play.WaitingActivity
import com.example.balapplat.rank.RankActivity
import com.example.balapplat.rank.RankRecyclerViewAdapter
import com.example.balapplat.utils.showSnackBar
import com.facebook.AccessToken
import com.google.android.material.bottomnavigation.BottomNavigationMenu
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.fragment_home
import kotlinx.android.synthetic.main.fragment_tournament.*
import org.jetbrains.anko.clearTask
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(), NetworkConnectivityListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var customGameAdapter: CustomGameRecyclerViewAdapter
    var puzzleType = 1
    var puzzleAnswer = 0
    var playedPuzzleToday = false
    private lateinit var popupWindow : PopupWindow

    private var currentDate = ""
    private var numberArr : MutableList<Int> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }



    override fun onStart() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val sdf = SimpleDateFormat("dd/M/yyyy")
        currentDate = sdf.format(Date())

        val animationBounce = AnimationUtils.loadAnimation(ctx, R.anim.fade_in)
        btnRank.startAnimation(animationBounce)

        checkPlayedPuzzle()
        val typeface = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)

        tvCredit.typeface = typeface
        tvTitle.typeface = typeface
        btnCustomPlay.typeface = typeface
        btnRank.typeface = typeface
        btnPlayFriend.typeface = typeface
        btnOnline.typeface = typeface
        btnLeaderboard.typeface = typeface

        val clickAnimation = AlphaAnimation(1.2F,0.6F)
        btnCustomPlay.onClick {
            startActivity<WaitingActivity>()
        }

        btnRank.onClick {
            btnRank.startAnimation(clickAnimation)
            startActivity<RankActivity>()
        }

        btnPlayFriend.onClick {
            if(auth.currentUser == null)
                startActivity(intentFor<LoginActivity>().clearTask())
            else
                startActivity(intentFor<FriendsActivity>().clearTask())
        }

        btnOnline.onClick {
            if(auth.currentUser == null)
                startActivity(intentFor<LoginActivity>().clearTask())
            else
                startActivity(intentFor<WaitingActivity>("playOnline" to true))

        }

        btnLeaderboard.onClick {
            startActivity(intentFor<LeaderBoardActivity>().clearTask())
        }

        ivDailyPuzzle.onClick {
            checkPlayedPuzzle()
            if (playedPuzzleToday)
                toast("You Already Played Today")
            else
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

        if(AccessToken.getCurrentAccessToken() == null)
            auth.signOut()

        //updateUI()

        super.onStart()
    }

    private fun checkPlayedPuzzle() {
        sharedPreference =  ctx.getSharedPreferences("LOCAL_DATA",Context.MODE_PRIVATE)
        val lastPlayed = sharedPreference.getString("playedTime","")

        playedPuzzleToday = false
        tvPuzzleInfo.visibility = View.VISIBLE

        val animationBounce = AnimationUtils.loadAnimation(ctx, R.anim.bounce)
        animationBounce.repeatCount = Animation.INFINITE
        animationBounce.repeatMode = Animation.REVERSE

        tvPuzzleInfo.startAnimation(animationBounce)

//        if (lastPlayed.equals(currentDate)){
//            playedPuzzleToday = true
//            tvPuzzleInfo.visibility = View.VISIBLE
//
//            val animationBounce = AnimationUtils.loadAnimation(ctx, R.anim.bounce)
//            animationBounce.repeatCount = Animation.INFINITE
//
//            tvPuzzleInfo.startAnimation(animationBounce)
//
//        }else
//            tvPuzzleInfo.visibility = View.GONE

    }
//


    fun getFacebookProfilePicture(userID: String): String {
        return "https://graph.facebook.com/$userID/picture?type=large"
    }

    @SuppressLint("SetTextI18n")
    private fun generate(view: View){
        val tvDailyPuzzleQuestion = view.findViewById<TextView>(R.id.tvDailyPuzzleQuestion)
        val typeface : Typeface? = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
        tvDailyPuzzleQuestion.typeface = typeface

        if (puzzleType == 1){

            tvDailyPuzzleQuestion.text = ""
            numberArr.clear()

            for(x in 0 until 8)
            {
                var value = 0
                val choose = Random().nextInt(2)

                if (choose == 1){
                    value = Random().nextInt(10) + 65
                    numberArr.add(value)
                    tvDailyPuzzleQuestion.text = tvDailyPuzzleQuestion.text.toString() + value.toChar()
                }else{
                    value = Random().nextInt(9)
                    numberArr.add(value)
                    tvDailyPuzzleQuestion.text = tvDailyPuzzleQuestion.text.toString() + value
                }
            }
        }

        puzzleAnswer = generateAnswer()
    }

    private fun generateAnswer() : Int {
        var result = 0
        val temp = numberArr
        for(x in 0 until 8)
        {
            if(x != 8-1) {
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
            toast("True")
        }else
            toast("false")

        val editor = sharedPreference.edit()
        editor.putString("playedTime",currentDate)
        editor.apply()

        checkPlayedPuzzle()
        popupWindow.dismiss()
        fragment_home.alpha = 1F
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


        // If API level 23 or higher then execute the code
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//            // Create a new slide animation for popup window enter transition
//            val slideIn = Slide()
//            slideIn.slideEdge = Gravity.TOP
//            popupWindow.enterTransition = slideIn
//
//            // Slide animation for popup window exit transition
//            val slideOut = Slide()
//            slideOut.slideEdge = Gravity.RIGHT
//            popupWindow.exitTransition = slideOut
//
//        }

        val main_activity = main_view.findViewById<RelativeLayout>(R.id.activity_main)
        val dailyPuzzleTitle = view.findViewById<TextView>(R.id.tvDailyPuzzleTitle)
        val layoutPuzzle = view.findViewById<LinearLayout>(R.id.layout_puzzle_pop_up)

        val typeface : Typeface? = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
        dailyPuzzleTitle.typeface = typeface

        fragment_home.alpha = 0.1F
        main_activity.alpha = 0.1F
        layoutPuzzle.onClick {
            fragment_home.alpha = 1F
            main_activity.alpha = 1F
            popupWindow.dismiss()
        }

        normalKeyboard(view)
        generate(view)
        // Finally, show the popup window on app
        TransitionManager.beginDelayedTransition(fragment_home)
        popupWindow.showAtLocation(
            fragment_home, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )

    }

    private fun normalKeyboard(view: View){
        val btnAnswerPuzzle = view.findViewById<Button>(R.id.btnAnswerPuzzle)
        val tvAnswerPuzzle = view.findViewById<TextView>(R.id.tvAnswerPuzzle)
        val typeface : Typeface? = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
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

        btnAnswerPuzzle.onClick {
            checkAnswer(tvAnswerPuzzle.text.toString())
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
    }

    private fun popUpCustomGame(){
        val inflater:LayoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.pop_up_custom_game,null)
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
        var position = -1
        val main_activity = main_view.findViewById<RelativeLayout>(R.id.activity_main)
        val rvCustomGame = view.findViewById<RecyclerView>(R.id.rvCustomGame)
        val ivCustomGame = view.findViewById<RecyclerView>(R.id.ivCustomGame)
        val tvChooseGame = view.findViewById<TextView>(R.id.tvClickToChooseGame)
        val tvCustomGameTitle = view.findViewById<TextView>(R.id.tvCustomGameTitle)
        val btnPlay = view.findViewById<Button>(R.id.btnCustomPlay)
        val layoutCustomGame = view.findViewById<LinearLayout>(R.id.layout_custom_game)

        val typeface : Typeface? = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
        tvChooseGame.typeface = typeface
        tvCustomGameTitle.typeface = typeface
        customGameAdapter = CustomGameRecyclerViewAdapter(ctx){
            position = it
            rvCustomGame.visibility = View.GONE
            ivCustomGame.visibility = View.VISIBLE
        }
        rvCustomGame.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL,false)

        ivCustomGame.onClick {
            ivCustomGame.visibility = View.GONE
            rvCustomGame.visibility = View.VISIBLE
            position = -1
        }


        rvCustomGame.adapter = customGameAdapter
        fragment_home.alpha = 0.1F
        main_activity.alpha = 0.1F
        layoutCustomGame.onClick {
            fragment_home.alpha = 1F
            main_activity.alpha = 1F
            popupWindow.dismiss()
        }

        normalKeyboard(view)
        generate(view)
        // Finally, show the popup window on app
        TransitionManager.beginDelayedTransition(fragment_home)
        popupWindow.showAtLocation(
                fragment_home, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

    }

    override fun onResume() {

        if (auth.currentUser != null){
            toast("on resume call")
            GlobalScope.launch {
                delay(100)
                database.child("users").child(auth.currentUser!!.uid).child("active").setValue(true)
            }
        }

        super.onResume()
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
}
