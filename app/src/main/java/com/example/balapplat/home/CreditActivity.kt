package com.example.balapplat.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.balapplat.R
import com.example.balapplat.presenter.Presenter
import com.example.balapplat.rank.Balance
import com.example.balapplat.view.MainView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_credit.*
import kotlinx.android.synthetic.main.activity_market.*

class CreditActivity : AppCompatActivity(), MainView {
    private lateinit var adapter: CreditRecyclerViewAdapter
    private lateinit var mAdView : AdView
    private lateinit var presenter: Presenter
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit)

        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance().reference
        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvCredit.typeface = typeface
        tvCreditTitle.typeface = typeface
        presenter = Presenter(this,database)

        adapter = CreditRecyclerViewAdapter(this)

        mAdView = this.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.SMART_BANNER
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        presenter.fetchCredit()
        rvCredit.layoutManager = LinearLayoutManager(this)
        rvCredit.adapter = adapter
    }

    override fun loadData(dataSnapshot: DataSnapshot) {
       tvCredit.text = dataSnapshot.getValue(Balance::class.java)?.credit.toString()
    }

    override fun response(message: String) {

    }
}
