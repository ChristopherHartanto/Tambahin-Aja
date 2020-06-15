package com.ta.tambahinaja.main

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
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
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
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
import com.ta.tambahinaja.NewUpdateActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.startActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), MainView, FragmentListener {

    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor : SharedPreferences.Editor
    private lateinit var database: DatabaseReference
    private lateinit var presenter: Presenter
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

        //bottom_navigation.itemIconTintList = null
        supportActionBar?.hide()

        savedInstanceState?.let {
            prevState = it.getBoolean(UtilsConstants.LOST_CONNECTION)
        }

//        bottom_navigation.setOnNavigationItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.home -> {
//                    loadHomeFragment(savedInstanceState)
//                }
//                R.id.tournament -> {
//                    loadTournamentFragment(savedInstanceState)
//                }
//                R.id.profile -> {
//                    if (auth.currentUser == null)
//                        startActivity<LoginActivity>()
//                    else
//                        loadProfileFragment(savedInstanceState)
//                }
//
//            }
//            true
//        }
//        bottom_navigation.selectedItemId = R.id.home

        val item1 = AHBottomNavigationItem("home",R.drawable.home,R.color.colorPrimary)
        val item2 = AHBottomNavigationItem("tournament",R.drawable.award,R.color.colorPrimary)
        val item3 = AHBottomNavigationItem("profile",R.drawable.user,R.color.colorPrimary)
        bottom_navigation.addItem(item1)
        bottom_navigation.addItem(item2)
        bottom_navigation.addItem(item3)

        bottom_navigation.setDefaultBackgroundResource(R.color.colorWhite)
        bottom_navigation.titleState = AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE
        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)!!
        bottom_navigation.setTitleTypeface(typeface)
        //bottom_navigation.defaultBackgroundColor = Color.parseColor("#FFFFFF");
       //bottom_navigation.isColored = true
        bottom_navigation.isTranslucentNavigationEnabled = true
        bottom_navigation.isForceTint = true

        bottom_navigation.backgroundResource = R.color.colorWhite

        bottom_navigation.setOnTabSelectedListener(object : AHBottomNavigation.OnTabSelectedListener{
            override fun onTabSelected(position: Int, wasSelected: Boolean): Boolean {
                when (position) {
                    0 -> {
                        //bottom_navigation.defaultBackgroundColor = Color.parseColor("#2ecc71")
                        bottom_navigation.accentColor = Color.parseColor("#2ecc71")
                        loadHomeFragment(savedInstanceState)
                    }
                    1 -> {
                        bottom_navigation.accentColor = Color.parseColor("#f1c40f")
                        //bottom_navigation.defaultBackgroundColor = Color.parseColor("#f1c40f")
                        loadTournamentFragment(savedInstanceState)

                    }
                    2 -> {
                        bottom_navigation.accentColor = Color.parseColor("#3498db")
                        if (auth.currentUser == null)
                            startActivity<LoginActivity>()
                        else
                            loadProfileFragment(savedInstanceState)
                    }

                }
                return true
            }

        })

        bottom_navigation.currentItem = 0

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
        if (response == "fetchTournament"){
            if (dataSnapshot.exists()) {
                for ((index, data) in dataSnapshot.children.withIndex()) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val currentDate = Date().time
                    val tournamentDate = sdf.parse(data.key.toString()).time
                    val diff = tournamentDate - currentDate

                    if (diff > 0){
                        val watchTournamentDate = sharedPreference.getString(tournamentDate.toString(),"")
                        if (watchTournamentDate != tournamentDate.toString()){
                            bottom_navigation.setNotificationBackgroundColor(Color.parseColor("#F63D2B"));
                            bottom_navigation.setNotification("!", 1)

                            editor = sharedPreference.edit()
                            editor.putString(tournamentDate.toString(),tournamentDate.toString())
                            editor.apply()
                        }

                    }
                }
            }
        }

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
        presenter = Presenter(this,database)
        presenter.fetchTournament()
        sharedPreference =  this.getSharedPreferences("LOCAL_DATA",Context.MODE_PRIVATE)

        val watchTutorial = sharedPreference.getBoolean("tutorial",false)
        val checkVersionUpdate = sharedPreference.getString("versionUpdate","")
        var watchNewUpdates = false

        if (checkVersionUpdate == packageManager.getPackageInfo(packageName,0).versionName)
            watchNewUpdates = true

        if (!watchTutorial){
            val editor = sharedPreference.edit()
            editor.putBoolean("tutorial", true)
            editor.apply()

            startActivity<TutorialActivity>()
        }else if(!watchNewUpdates){
            editor = sharedPreference.edit()
            editor.putString("versionUpdate",packageManager.getPackageInfo(packageName,0).versionName)
            editor.apply()

            startActivity<NewUpdateActivity>()
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

