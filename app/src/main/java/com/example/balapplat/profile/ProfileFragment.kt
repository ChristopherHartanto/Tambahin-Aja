package com.example.balapplat.profile

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import com.example.balapplat.R
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.example.balapplat.home.CustomGameRecyclerViewAdapter
import com.example.balapplat.model.HighScore
import com.example.balapplat.play.CountdownActivity
import com.example.balapplat.utils.showSnackBar
import com.facebook.AccessToken
import com.facebook.Profile
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

class ProfileFragment : Fragment(), NetworkConnectivityListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var mAdView : AdView
    private lateinit var popupWindow : PopupWindow
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)

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

        mAdView = view!!.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        MobileAds.initialize(ctx)
        val adView = AdView(ctx)
        adView.adSize = AdSize.SMART_BANNER
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        updateUI()

        ivSetting.onClick {
            startActivity<SettingActivity>()
        }
        super.onStart()
    }

        fun updateUI(){
            val typeface = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
            tvProfileName.typeface = typeface
            tvHistoryTitle.typeface = typeface
            tvWinTournament.typeface = typeface
            tvWin.typeface = typeface

        if (auth.currentUser != null){
            tvProfileName.text = auth.currentUser!!.displayName.toString()
            //getHighScore()

            Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivProfile)

            layout_edit_profile.onClick {
                popUpEditProfile()
            }
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
                    tvWinTournament.text = dataSnapshot.getValue(Stats::class.java)?.tournamentWin.toString()
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

    private fun popUpEditProfile(){
        val inflater:LayoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.pop_up_edit_profile,null)

        // Initialize a new instance of popup window
        popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
                LinearLayout.LayoutParams.MATCH_PARENT,// Window height
                true
        )

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }

        val btnClose = view.findViewById<Button>(R.id.btnClose)
        val btnSave = view.findViewById<Button>(R.id.btnEditProfileSave)
        val tvEdiProfileTitle = view.findViewById<TextView>(R.id.tvEditProfileTitle)
        val tvEditProfileName = view.findViewById<TextView>(R.id.tvEditProfileName)
        val tvEditProfileEmail = view.findViewById<TextView>(R.id.tvEditProfileEmail)
        val tvEditProfileHandphone = view.findViewById<TextView>(R.id.tvEditProfileHandphone)
        val etEditProfileName = view.findViewById<EditText>(R.id.etEditProfileName)
        val etEditProfileEmail = view.findViewById<EditText>(R.id.etEditProfileEmail)
        val etEditProfileHandphone = view.findViewById<EditText>(R.id.etEditProfileHandphone)

        val typeface : Typeface? = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
        btnClose.typeface = typeface
        btnSave.typeface = typeface
        tvEdiProfileTitle.typeface = typeface
        tvEditProfileName.typeface = typeface
        tvEditProfileEmail.typeface = typeface
        tvEditProfileHandphone.typeface = typeface
        etEditProfileEmail.typeface = typeface
        etEditProfileHandphone.typeface = typeface
        etEditProfileName.typeface = typeface


        btnClose.onClick {
            btnClose.startAnimation(clickAnimation)
            fragment_profile.alpha = 1F
            popupWindow.dismiss()
        }

        fragment_profile.alpha = 0.1F

        TransitionManager.beginDelayedTransition(fragment_profile)
        popupWindow.showAtLocation(
                fragment_profile, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

    }

}

data class Stats(
    var win: Int? = 0,
    var lose: Int? = 0,
    var tournamentWin: Int? = 0
)