package com.example.balapplat

import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.transition.TransitionManager
import com.example.balapplat.friends.FriendsActivity
import com.example.balapplat.leaderboard.LeaderBoardActivity
import com.example.balapplat.main.LoginActivity
import com.example.balapplat.play.WaitingActivity
import com.example.balapplat.rank.RankActivity
import com.example.balapplat.utils.showSnackBar
import com.facebook.AccessToken
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import com.google.firebase.database.*
import com.quantumhiggs.network.NetworkStateHolder
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.clearTask
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
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

        //var typeFace: Typeface? = ResourcesCompat.getFont(ctx, R.font.FredokaOne_Regular)

        btnCustomPlay.onClick {
            startActivity<WaitingActivity>()
        }

        btnRank.onClick {
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

        if(AccessToken.getCurrentAccessToken() == null)
            auth.signOut()

        //updateUI()

        super.onStart()
    }

    private fun checkPlayedPuzzle() {
        sharedPreference =  ctx.getSharedPreferences("LOCAL_DATA",Context.MODE_PRIVATE)
        val lastPlayed = sharedPreference.getString("playedTime","")

        if (lastPlayed.equals(currentDate))
            playedPuzzleToday = true
    }
//


    fun getFacebookProfilePicture(userID: String): String {
        return "https://graph.facebook.com/$userID/picture?type=large"
    }

    @SuppressLint("SetTextI18n")
    private fun generate(view: View){
        val tvDailyPuzzleQuestion = view.findViewById<TextView>(R.id.tvDailyPuzzleQuestion)
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
    }

    private fun popUp(){
        val inflater:LayoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.puzzle_pop_up,null)


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

        val layoutPreEnterRoom = view.findViewById<LinearLayout>(R.id.layout_puzzle_pop_up)

        layoutPreEnterRoom.onClick {
            popupWindow.dismiss()
        }

        val btnAnswerPuzzle = view.findViewById<Button>(R.id.btnAnswerPuzzle)
        val etAnswerPuzzle = view.findViewById<EditText>(R.id.etAnswerPuzzle)

        btnAnswerPuzzle.onClick {
            checkAnswer(etAnswerPuzzle.text.toString())
        }

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
