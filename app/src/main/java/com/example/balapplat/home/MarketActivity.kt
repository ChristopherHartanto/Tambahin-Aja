package com.example.balapplat.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.example.balapplat.R
import com.example.balapplat.leaderboard.LeaderBoardRecyclerViewAdapter
import com.example.balapplat.presenter.ShopPresenter
import com.example.balapplat.rank.Balance
import com.example.balapplat.view.MainView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_market.*
import kotlinx.android.synthetic.main.activity_market.tvEnergy
import kotlinx.android.synthetic.main.activity_market.tvPoint
import kotlinx.android.synthetic.main.activity_rank.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*

class MarketActivity : AppCompatActivity(), MainView {

    private lateinit var shopPresenter: ShopPresenter
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var adapter: ShopRecyclerViewAdapter
    private lateinit var mAdView : AdView
    private lateinit var database: DatabaseReference
    lateinit private var billingClient: BillingClient
    private lateinit var countDownTimer : CountDownTimer
    private var point = 0
    private var energy = 0
    private var energyLimit = 0
    private var remainTime = 0
    private var counted = 0
    private var diff: Long = 0
    private var checkUpdateEnergy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market)

        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance().reference
        sharedPreferences =  this.getSharedPreferences("LOCAL_DATA",Context.MODE_PRIVATE)
        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvShopTitle.typeface = typeface
        tvEnergy.typeface = typeface
        tvPoint.typeface = typeface

        shopPresenter = ShopPresenter(this,database)


        mAdView = this.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.SMART_BANNER
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        adapter = ShopRecyclerViewAdapter(this){

        }
        rvMarket.layoutManager = LinearLayoutManager(this)
        rvMarket.adapter = adapter

//
//        billingClient = BillingClient.newBuilder(this).setListener(this).build()
//        billingClient.startConnection(object : BillingClientStateListener {
//            override fun onBillingSetupFinished(billingResult: BillingResult) {
//                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
//                    // The BillingClient is ready. You can query purchases here.
//                    val b = BillingFlowParams
//                            .newBuilder()
//                            .setSkuDetails()
//                }
//            }
//            override fun onBillingServiceDisconnected() {
//                // Try to restart the connection on the next request to
//                // Google Play by calling the startConnection() method.
//            }
//        })
    }

    override fun onStart() {
        shopPresenter.fetchBalance()

        countDownTimer = object : CountDownTimer(1000,1000){
            override fun onFinish() {
            }

            override fun onTick(millisUntilFinished: Long) {
            }

        }
        super.onStart()
    }

    fun setUpEnergyTimer(){
        if (energy != energyLimit) {
            val remainingEnergyToFull = (energyLimit - energy) * 120 // 10 detik
            val currentDate = Date().time

            val lastCountEnergy = sharedPreferences.getLong("lastCountEnergy", currentDate)
            counted = sharedPreferences.getLong("countedEnergy", 0).toInt()

            diff = currentDate - lastCountEnergy

            remainTime = (remainingEnergyToFull - (diff / 1000) - counted).toInt()

            val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss")
            Log.d("testcurrent date : ",sdf.format(currentDate))
            Log.d("testlast date : ",sdf.format(lastCountEnergy))
            Log.d("testdiff : ",diff.toString())
            Log.d("testremainTime : ",remainTime.toString())
            energyTimer()
        }
    }

    fun energyTimer(){
        if (remainTime > 0 && !checkUpdateEnergy){
            checkUpdateEnergy = true
            var energyGet = diff / 1000 / 120
            if (energyGet > energyLimit)
                energyGet = energyLimit.toLong()
            Log.d("energy get", energyGet.toString())
            shopPresenter.updateEnergy(energy.toLong() + energyGet)
        }
        if (remainTime > 0){

            var timerSec = remainTime % 120 // 100 detik
            var timerMin = 0

            if(timerSec == 0){
                shopPresenter.updateEnergy(energy.toLong())
                remainTime--
                energyTimer()
            }else{
                if (timerSec > 60){
                    timerMin = timerSec / 60
                    timerSec %= 60
                }
                Log.d("remainTime : ",remainTime.toString())
                Log.d("timer seconds",timerSec.toString())
                var seconds = timerSec
                countDownTimer = object : CountDownTimer((timerSec.toLong()+1) * 1000,1000){
                    override fun onFinish() {
                        Log.d("tick",timerSec.toString())
                        seconds--
                        tvEnergyTimer.text = "${timerMin}:${seconds}"
                        remainTime--
                        energyTimer()
                        energy++
                        tvEnergy.text = "${energy}/${energyLimit}"
                        shopPresenter.updateEnergy(energy.toLong())
                    }

                    override fun onTick(millisUntilFinished: Long) {
                        Log.d("tick",timerSec.toString())
                        tvEnergyTimer.text = "${timerMin}:${seconds}"
                        seconds--
                        counted += 1
                        remainTime--
                    }

                }
                countDownTimer.start()
            }
        }else{
            tvEnergyTimer.text = "Full"
        }


    }

    override fun onPause() {
        editor = sharedPreferences.edit()
        editor.putLong("lastCountEnergy",Date().time)
        editor.putLong("countedEnergy",counted.toLong())
        editor.apply()

        Log.d("save last count", sharedPreferences.getLong("lastCountEnergy",0).toString())

        countDownTimer.cancel()
        super.onPause()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == "fetchBalance"){
            editor = sharedPreferences.edit()
            editor.putInt("point",dataSnapshot.getValue(Balance::class.java)!!.point!!)
            editor.apply()
            energy = dataSnapshot.getValue(Balance::class.java)!!.energy!!
            energyLimit = dataSnapshot.getValue(Balance::class.java)!!.energyLimit!!
            point = dataSnapshot.getValue(Balance::class.java)!!.point!!
            tvPoint.text = point.toString()
            toast("aa")
            setUpEnergyTimer()
            tvEnergy.text = "${energy}/${energyLimit}"
        }
    }

    override fun response(message: String) {
    }
}
