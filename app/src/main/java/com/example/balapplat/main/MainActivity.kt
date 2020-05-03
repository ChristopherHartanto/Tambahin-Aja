package com.example.balapplat.main

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.transition.TransitionManager
import com.example.balapplat.home.HomeFragment
import com.example.balapplat.R
import com.example.balapplat.Tournament
import com.example.balapplat.model.Inviter
import com.example.balapplat.play.CountdownActivity
import com.example.balapplat.presenter.Presenter
import com.example.balapplat.profile.ProfileFragment
import com.example.balapplat.utils.UtilsConstants
import com.example.balapplat.utils.showSnackBar
import com.example.balapplat.view.MainView
import com.facebook.AccessToken
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkEvents
import com.quantumhiggs.network.NetworkState
import com.quantumhiggs.network.NetworkStateHolder
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx

class MainActivity : AppCompatActivity(), MainView {

    private lateinit var database: DatabaseReference
    lateinit var presenter: Presenter
    private lateinit var auth: FirebaseAuth
    var data : Inviter = Inviter()
    private var prevState = true
    private var doubleBackToExitPressedOnce = false
    private lateinit var popupWindow : PopupWindow
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        bottom_navigation.itemIconTintList = null
        supportActionBar?.hide()
        savedInstanceState?.let {
            prevState = it.getBoolean(UtilsConstants.LOST_CONNECTION)
        }

        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    loadHomeFragment(savedInstanceState)
                }
                R.id.tournament -> {
                    loadTournamentFragment(savedInstanceState)
                }
                R.id.profile -> {
                    loadProfileFragment(savedInstanceState)
                }

            }
            true
        }
        bottom_navigation.selectedItemId = R.id.home
    }



    private fun loadHomeFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.main_container,
                        HomeFragment(), HomeFragment::class.java.simpleName)
                .commit()
        }
    }

    private fun loadTournamentFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.main_container,
                    Tournament(), Tournament::class.java.simpleName)
                .commit()
        }
    }

    private fun loadProfileFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.main_container,
                    ProfileFragment(), ProfileFragment::class.java.simpleName)
                .commit()
        }
    }


    override fun loadData(dataSnapshot: DataSnapshot) {
        data = dataSnapshot.getValue(Inviter::class.java)!!

        popUpMessage(1)
    }

    override fun response(message: String) {
        if (message === "acceptedGame"){
            toast("acceptedGame")

            startActivity(intentFor<CountdownActivity>("inviterFacebookId" to data.facebookId,
                "inviterName" to data.name))
        }

    }

    private fun handleConnectivityChange(networkState: NetworkState) {
        if (networkState.isConnected && !prevState) {
            showSnackBar(activity_main, "The network is back !", "LONG")
        }

        if (!networkState.isConnected && prevState) {
            showSnackBar(activity_main, "No Network !", "INFINITE")
        }

        prevState = networkState.isConnected
    }

    private fun popUpMessage(type: Int){
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
        val btnClose = view.findViewById<Button>(R.id.btnMessageClose)
        val btnReject = view.findViewById<Button>(R.id.btnMessageReject)
        val tvMessageTitle = view.findViewById<TextView>(R.id.tvMessageTitle)
        val ivInviter = view.findViewById<CircleImageView>(R.id.ivInviter)
        val tvMessageInviter = view.findViewById<TextView>(R.id.tvMessageInviter)

        if (type == 1){
            layoutMessageInvitation.visibility = View.VISIBLE
            layoutMessageBasic.visibility = View.GONE
            layoutMessageReward.visibility = View.GONE

            btnClose.onClick {
                presenter.replyInvitation(true)
                btnClose.startAnimation(clickAnimation)
                activity_main.alpha = 1F
                popupWindow.dismiss()
            }

            btnReject.onClick {
                presenter.replyInvitation(false)
                btnReject.startAnimation(clickAnimation)
                activity_main.alpha = 1F
                popupWindow.dismiss()
            }

            Picasso.get().load(getFacebookProfilePicture(data.facebookId!!)).fit().into(ivInviter)
            tvMessageInviter.text = "${data.name} invited you to play"

        }

        tvMessageTitle.typeface = typeface
        tvMessageInviter.typeface = typeface

        activity_main.alpha = 0.1F

        TransitionManager.beginDelayedTransition(fragment_profile)
        popupWindow.showAtLocation(
                activity_main, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

    }

    fun getFacebookProfilePicture(userID: String): String {
        return "https://graph.facebook.com/$userID/picture?type=large"
    }

    override fun onStart() {
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        presenter = Presenter(this, database)

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) handleConnectivityChange(it.state)
        })

        if(AccessToken.getCurrentAccessToken() != null)
            presenter.receiveInvitation()

        handleConnectivityChange(NetworkStateHolder)
        super.onStart()
    }

    override fun onPause() {
        presenter.dismissListener()
        super.onPause()
    }
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        toast("Please click BACK again to exit")

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }
}



