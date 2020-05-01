package com.example.balapplat.main

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.balapplat.HomeFragment
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkEvents
import com.quantumhiggs.network.NetworkState
import com.quantumhiggs.network.NetworkStateHolder
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*

class MainActivity : AppCompatActivity(), MainView {

    private lateinit var database: DatabaseReference
    lateinit var presenter: Presenter
    private lateinit var auth: FirebaseAuth
    var data : Inviter = Inviter()
    private var prevState = true

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        bottom_navigation.itemIconTintList = null
        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        presenter = Presenter(this, database)

        savedInstanceState?.let {
            prevState = it.getBoolean(UtilsConstants.LOST_CONNECTION)
        }

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) handleConnectivityChange(it.state)
        })

        if(AccessToken.getCurrentAccessToken() != null)
            presenter.receiveInvitation()

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

        alert(data!!.name + " invite you to play"){
            title = "Invitation"
            yesButton {
                presenter.replyInvitation(true)
            }
            noButton {
               presenter.replyInvitation(false)
            }
        }.show()
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

    override fun onStart() {
        handleConnectivityChange(NetworkStateHolder)
        super.onStart()
    }
}



