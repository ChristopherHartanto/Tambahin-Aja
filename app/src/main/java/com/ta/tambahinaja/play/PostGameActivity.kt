package com.ta.tambahinaja.play

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.ta.tambahinaja.R
import com.facebook.Profile
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_post_game.*
import kotlinx.android.synthetic.main.activity_post_game.ivPlayerImage
import kotlinx.android.synthetic.main.activity_post_game.layoutMultipleGame
import kotlinx.android.synthetic.main.activity_post_game.tvOpponentName
import kotlinx.android.synthetic.main.activity_post_game.tvOpponentPoint
import kotlinx.android.synthetic.main.activity_post_game.tvPlayerName
import kotlinx.android.synthetic.main.activity_post_game.tvPlayerPoint
import org.jetbrains.anko.sdk27.coroutines.onClick

class PostGameActivity : AppCompatActivity() {

    private lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_game)

        supportActionBar?.hide()
        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvPostGameTitle.typeface = typeface
        tvGetCredit.typeface = typeface
        btnBackMenu.typeface = typeface
        tvGetPoint.typeface = typeface
        tvSinglePoint.typeface = typeface
        tvOpponentName.typeface = typeface
        tvPlayerName.typeface = typeface
        tvPlayerPoint.typeface = typeface
        tvOpponentPoint.typeface = typeface

        mAdView = this.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        Thread.sleep(100)

        val score = intent.extras!!.getInt("score",0)
        val status = intent.extras!!.getSerializable("status")
        if (score != 0){
            layoutMultipleGame.visibility = View.GONE
            tvSinglePoint.visibility = View.VISIBLE
            tvSinglePoint.text = "" + score
            tvGetCredit.text = intent.extras!!.getInt("rewardCredit",0).toString()
            tvGetPoint.text = intent.extras!!.getInt("rewardPoint",0).toString()
        }else if (status == StatusPlayer.Single){
            layoutMultipleGame.visibility = View.GONE
            tvSinglePoint.visibility = View.VISIBLE
            tvSinglePoint.text = "" + score
        }else{
            tvSinglePoint.visibility = View.GONE
            layoutMultipleGame.visibility = View.VISIBLE

            tvPlayerName.text = Profile.getCurrentProfile().name
            Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivPlayerImage)
            tvPlayerPoint.text = intent.extras!!.getInt("scorePlayer",0).toString()

            tvOpponentName.text = intent.extras!!.getString("opponentName","Unknown")
            Picasso.get().load(getFacebookProfilePicture(intent.extras!!.getString("opponentFacebookId",""))).fit().into(ivOpponentImage)
            tvOpponentPoint.text = intent.extras!!.getInt("scoreOpponent",0).toString()

            tvPostGameTitle.text = "You " +intent.extras!!.getString("gameResult","Result")
        }

        btnBackMenu.onClick {
            finish()
        }
    }
    private fun getFacebookProfilePicture(userID: String): String {
        return "https://graph.facebook.com/$userID/picture?type=large"
    }

}
