package com.example.balapplat.friends

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.balapplat.R
import com.example.balapplat.model.Inviter
import com.example.balapplat.play.CountdownActivity
import com.example.balapplat.presenter.Presenter
import com.example.balapplat.utils.showSnackBar
import com.example.balapplat.view.MainView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.activity_friends.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx

class FriendsActivity : AppCompatActivity(), NetworkConnectivityListener, FriendsAddListener   {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var mAdView : AdView
    lateinit var data: Inviter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        val typeface = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
        tvFriendsTitle.typeface = typeface

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        checkFriends(savedInstanceState)

        cvAddFriends.onClick {
            startActivity<AddFriendsActivity>()
        }
    }

    private fun loadNoFriendFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_friend, NoFriendFragment(), NoFriendFragment::class.java.simpleName)
                .commit()
        }
    }

    private fun loadListFriendsFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_friend, ListFriendsFragment(), ListFriendsFragment::class.java.simpleName)
                .commit()
        }
    }

    fun checkFriends(savedInstanceState: Bundle?){
        var highScore = 0
        GlobalScope.async {
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    if(dataSnapshot.exists())
                        loadListFriendsFragment(savedInstanceState)
                    else
                        loadNoFriendFragment(savedInstanceState)
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            }

            database.child("friends").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(postListener)

        }

    }


    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(activity_friends, "The network is back !", "LONG")
                } else {
                    showSnackBar(activity_friends, "There is no more network", "INFINITE")
                }
            }
        }
    }

    override fun removeFragment() {
        finish()
    }

}
