package com.example.balapplat.friends

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.TextView
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
    lateinit var typeface: Typeface
    private lateinit var tvLoadingTitle : TextView
    private lateinit var tvLoadingInfo : TextView
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        typeface = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)!!

        val view = findViewById<View>(R.id.layout_loading)

        tvLoadingTitle = view.findViewById(R.id.tvLoadingTitle)
        tvLoadingInfo = view.findViewById(R.id.tvLoadingInfo)

        tvLoadingInfo.typeface = typeface
        tvLoadingTitle.typeface = typeface

        tvFriendsTitle.typeface = typeface

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        checkFriends(savedInstanceState)

        ivAddFriends.onClick {
            ivAddFriends.startAnimation(clickAnimation)
            startActivity<AddFriendsActivity>()
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
                    if(dataSnapshot.exists()){
                        container_friend.visibility = View.VISIBLE
                        layout_loading.visibility = View.GONE
                        loadListFriendsFragment(savedInstanceState)
                    }
                    else{
                        tvLoadingTitle.text = "Go and Get Some Friends"
                        tvLoadingInfo.text = "There are Friends, There is Family, and There are Friends Become Family"
                    }
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
