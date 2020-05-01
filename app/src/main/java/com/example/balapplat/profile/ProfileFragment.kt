package com.example.balapplat.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.balapplat.R
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.example.balapplat.model.HighScore
import com.example.balapplat.utils.showSnackBar
import com.facebook.AccessToken
import com.facebook.Profile
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import org.jetbrains.anko.support.v4.ctx

class ProfileFragment : Fragment(), NetworkConnectivityListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var mAdView : AdView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onStart() {
        auth = FirebaseAuth.getInstance()

        database = FirebaseDatabase.getInstance().reference

        if(AccessToken.getCurrentAccessToken() == null)
            auth.signOut()

        getStats()
        MobileAds.initialize(ctx)
        val adView = AdView(ctx)
        adView.adSize = AdSize.SMART_BANNER
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        updateUI()
        super.onStart()
    }

        fun updateUI(){
        if (auth.currentUser != null){
            tvProfileName.text = auth.currentUser!!.displayName.toString()
            //getHighScore()

            Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivProfile)
        }else{
            tvProfileName.text = "Unknown"
        }
    }

    fun getStats(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if (dataSnapshot.exists()){
                    tvWin.text = dataSnapshot.getValue(Stats::class.java)?.win.toString()
                    tvLose.text = dataSnapshot.getValue(Stats::class.java)?.lose.toString()
                    tvWinTournament.text = dataSnapshot.getValue(Stats::class.java)?.tournamentWin.toString()
                    tvWinTournament.text = dataSnapshot.getValue(Stats::class.java)?.joinedTournament.toString()
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        database.child("stats").child(auth.currentUser!!.uid).addValueEventListener(postListener)
    }

    fun getFacebookProfilePicture(userID: String): String {
        return "https://graph.facebook.com/$userID/picture?type=large"
    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(fragment_profile, "The network is back !", "LONG")
                } else {
                    showSnackBar(fragment_profile, "There is no more network", "INFINITE")
                }
            }
        }
    }

}

data class Stats(
    var win: Int? = 0,
    var lose: Int? = 0,
    var tournamentWin: Int? = 0,
    var joinedTournament: Int? = 0
)