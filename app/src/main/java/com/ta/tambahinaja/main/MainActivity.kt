package com.ta.tambahinaja.main

import android.content.Context
import android.content.SharedPreferences
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
import com.ta.tambahinaja.home.HomeFragment
import com.ta.tambahinaja.R
import com.ta.tambahinaja.TutorialActivity
import com.ta.tambahinaja.tournament.Tournament
import com.ta.tambahinaja.model.Inviter
import com.ta.tambahinaja.play.CountdownActivity
import com.ta.tambahinaja.play.GameType
import com.ta.tambahinaja.play.StatusPlayer
import com.ta.tambahinaja.presenter.Presenter
import com.ta.tambahinaja.profile.ProfileFragment
import com.ta.tambahinaja.rank.Balance
import com.ta.tambahinaja.tournament.FragmentListener
import com.ta.tambahinaja.utils.UtilsConstants
import com.ta.tambahinaja.utils.showSnackBar
import com.ta.tambahinaja.view.MainView
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
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class MainActivity : AppCompatActivity(), MainView, FragmentListener {

    private lateinit var sharedPreference: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    var data : Inviter = Inviter()
    private lateinit var reward: Reward
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
                    if (auth.currentUser == null)
                        toast("Login First")
                    else
                        loadProfileFragment(savedInstanceState)
                }

            }
            true
        }
        bottom_navigation.selectedItemId = R.id.home
    }



    private fun loadHomeFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            if (supportFragmentManager.findFragmentById(R.id.fragment_profile) != null)
                supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentById(R.id.fragment_profile)!!)
            if (supportFragmentManager.findFragmentById(R.id.fragment_tournament) != null)
                supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentById(R.id.fragment_tournament)!!)
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
            if (supportFragmentManager.findFragmentById(R.id.fragment_profile) != null)
                supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentById(R.id.fragment_profile)!!)
            if (supportFragmentManager.findFragmentById(R.id.fragment_home) != null)
                supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentById(R.id.fragment_home)!!)
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
            if (supportFragmentManager.findFragmentById(R.id.fragment_home) != null)
                supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentById(R.id.fragment_home)!!)
            if (supportFragmentManager.findFragmentById(R.id.fragment_tournament) != null)
                supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentById(R.id.fragment_tournament)!!)
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.main_container,
                    ProfileFragment(), ProfileFragment::class.java.simpleName)
                .commit()
        }
    }


    override fun loadData(dataSnapshot: DataSnapshot, response: String) {


    }

    override fun response(message: String) {


    }

    private fun handleConnectivityChange(networkState: NetworkState) {
        if (networkState.isConnected && !prevState) {
            recreate()
            showSnackBar(activity_main, "The network is back !", "LONG")
        }

        if (!networkState.isConnected && prevState) {
            showSnackBar(activity_main, "No Network !", "INFINITE")
        }

        prevState = networkState.isConnected
    }

    override fun onStart() {
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        sharedPreference =  this.getSharedPreferences("LOCAL_DATA",Context.MODE_PRIVATE)

        val watchTutorial = sharedPreference.getBoolean("tutorial",false)

        if (!watchTutorial){
            val editor = sharedPreference.edit()
            editor.putBoolean("tutorial", true)
            editor.apply()

            startActivity<TutorialActivity>()
        }

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) handleConnectivityChange(it.state)
        })

        val editor = sharedPreference.edit()
        editor.putBoolean("continueRank",true)
        editor.apply()

        handleConnectivityChange(NetworkStateHolder)
        super.onStart()
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

    override fun finishFragment() {
        finish()
    }

    override fun backToHome() {
        loadHomeFragment(null)
    }

}

data class Reward(
        var description: String? = "",
        var type: String? = "",
        var quantity: Long? = 0
)
