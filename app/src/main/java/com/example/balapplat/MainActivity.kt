package com.example.balapplat

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.TransitionManager
import com.example.balapplat.friends.FriendsActivity
import com.example.balapplat.leaderboard.LeaderBoardActivity
import com.example.balapplat.model.HighScore
import com.example.balapplat.model.NormalMatch
import com.facebook.AccessToken
import com.facebook.Profile
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.pre_enter_room.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    lateinit var helper : Helper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        helper = Helper()

        database = FirebaseDatabase.getInstance().reference


        btnPlay.onClick {
            popUp(2)

        }

        btnPlayFriend.onClick {
            if(auth.currentUser == null)
                startActivity(intentFor<LoginActivity>().clearTask())
            else
                startActivity(intentFor<FriendsActivity>().clearTask())
        }

        btnPlayOnline.onClick {
            if(auth.currentUser == null)
                startActivity(intentFor<LoginActivity>().clearTask())
            else
                popUp(1)
        }

        btnLeaderboard.onClick {
            startActivity(intentFor<LeaderBoardActivity>().clearTask())
        }

        cvProfile.onClick {
            startActivity(intentFor<LoginActivity>())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            Intent.FLAG_ACTIVITY_CLEAR_TASK ->
                finish()
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        if(AccessToken.getCurrentAccessToken() == null)
        auth.signOut()

        updateUI()

        super.onStart()
    }

    fun updateUI(){
        if (auth.currentUser != null){
            tvProfileName.text = auth.currentUser!!.displayName.toString()
            getHighScore()
            helper.userActive(true)
//            GlobalScope.launch {
//                tvHighScore.text = "" + getHighScore()
//            }
            Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivProfile)
        }else{
            tvProfileName.text = "Unknown"
        }
    }

    fun getHighScore(){
        var highScore = 0
        GlobalScope.async {
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    if(dataSnapshot.exists())
                        highScore = dataSnapshot.getValue(HighScore::class.java)?.score!!
                    tvHighScore.text = "" + highScore
                    val animationBounce = AnimationUtils.loadAnimation(ctx, R.anim.bounce)
                    tvHighScore.startAnimation(animationBounce)
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            }

            database.child("highscore").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(postListener)

        }

    }

    fun getFacebookProfilePicture(userID: String): String {
        return "https://graph.facebook.com/$userID/picture?type=large"
    }

    override fun onDestroy() {
        helper.userActive(false)
        super.onDestroy()
    }

    fun popUp(layout: Int){
        val inflater:LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = if (layout == 1)
            inflater.inflate(R.layout.pre_enter_room,null)
        else
            inflater.inflate(R.layout.game_category,null)


        // Initialize a new instance of popup window
        val popupWindow = PopupWindow(
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

        if (layout == 1){
            val layoutPreEnterRoom = view.findViewById<LinearLayout>(R.id.layout_pre_enter_room)

            layoutPreEnterRoom.onClick {
                popupWindow.dismiss()
            }
        }
        else{
            val layoutGameCategory = view.findViewById<LinearLayout>(R.id.layout_game_category)
            val btnPlay = view.findViewById<Button>(R.id.btnNormalGame)

            btnPlay.onClick {
                startActivity(intentFor<CountdownActivity>().clearTask())
            }

            layoutGameCategory.onClick {
                popupWindow.dismiss()
            }
        }


        // Finally, show the popup window on app
        TransitionManager.beginDelayedTransition(root_layout)
        popupWindow.showAtLocation(
            root_layout, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )

    }

    override fun onBackPressed() {
        alert("Do You Want to Exit?", "Exit") {
            yesButton {
                finish()
            }
            noButton {}
        }.show()
    }
}
