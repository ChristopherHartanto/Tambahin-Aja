package com.example.balapplat.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.balapplat.R
import com.example.balapplat.leaderboard.LeaderBoardRecyclerViewAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_market.*
import org.jetbrains.anko.support.v4.ctx

class MarketActivity : AppCompatActivity() {
    private lateinit var adapter: ShopRecyclerViewAdapter
    private lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market)

        supportActionBar?.hide()
        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvShopTitle.typeface = typeface
        tvEnergy.typeface = typeface
        tvPoint.typeface = typeface
        adapter = ShopRecyclerViewAdapter(this)

        mAdView = this.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.SMART_BANNER
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        rvMarket.layoutManager = LinearLayoutManager(this)
        rvMarket.adapter = adapter
    }
}
