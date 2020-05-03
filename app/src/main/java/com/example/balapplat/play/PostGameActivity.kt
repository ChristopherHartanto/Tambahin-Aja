package com.example.balapplat.play

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.balapplat.R
import com.example.balapplat.main.MainActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_post_game.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class PostGameActivity : AppCompatActivity() {

    private lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_game)

        supportActionBar?.hide()
        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvPostGameTitle.typeface = typeface
        tvGetCredit.typeface = typeface
        tvBackMenu.typeface = typeface
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
        adView.adSize = AdSize.SMART_BANNER
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        layoutMultipleGame.visibility = View.GONE

        tvBackMenu.onClick {
            finish()
            startActivity<MainActivity>()
        }
    }
}
