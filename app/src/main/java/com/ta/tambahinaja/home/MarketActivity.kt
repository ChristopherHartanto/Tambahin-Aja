package com.ta.tambahinaja.home

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import com.facebook.Profile
import com.github.thunder413.datetimeutils.DateTimeUnits
import com.github.thunder413.datetimeutils.DateTimeUtils
import com.ta.tambahinaja.R
import com.ta.tambahinaja.friends.Message
import com.ta.tambahinaja.presenter.ShopPresenter
import com.ta.tambahinaja.rank.Balance
import com.ta.tambahinaja.rank.Rank
import com.ta.tambahinaja.utils.showSnackBar
import com.ta.tambahinaja.view.MainView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import com.quantumhiggs.network.NetworkStateHolder
import kotlinx.android.synthetic.main.activity_add_friends.*
import kotlinx.android.synthetic.main.activity_market.*
import kotlinx.android.synthetic.main.activity_market.tvEnergy
import kotlinx.android.synthetic.main.activity_market.tvPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.ctx
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MarketActivity : AppCompatActivity(),NetworkConnectivityListener, MainView, PurchasesUpdatedListener {

    private lateinit var shopPresenter: ShopPresenter
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var adapter: ShopRecyclerViewAdapter
    private lateinit var mAdView : AdView
    private lateinit var database: DatabaseReference
    private lateinit var billingClient: BillingClient
    private lateinit var countDownTimer : CountDownTimer
    private lateinit var auth: FirebaseAuth
    private lateinit var currentRank : String
    private var skuList: MutableList<String> = mutableListOf("coin_500","coin_1000","coin_2000","energy_limit_300","energy_limit_200","energy_to_limit")
    private var items: MutableList<SkuDetails> = mutableListOf()
    private lateinit var buyItem : SkuDetails
    private var energyTime = 300
    private var point = 0
    private var energy = 0
    private var energyLimit = 0
    private var remainTime = 0
    private var counted = 0
    private var diff: Long = 0
    private lateinit var typeface: Typeface
    private var loadingCount = 4
    private lateinit var loadingTimer : CountDownTimer
    private var checkUpdateEnergy = false
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)
    private lateinit var popupWindow : PopupWindow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        sharedPreferences =  this.getSharedPreferences("LOCAL_DATA",Context.MODE_PRIVATE)
        typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)!!
        tvShopTitle.typeface = typeface
        tvEnergy.typeface = typeface
        tvPoint.typeface = typeface
        tvEnergyTimer.typeface = typeface

        shopPresenter = ShopPresenter(this,database)


        mAdView = this.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-1388436725980010/4892909155"

        adapter = ShopRecyclerViewAdapter(this,items){
            //toast("On Development")
            buyItem = it
            val billingFlowParams = BillingFlowParams
                    .newBuilder()
                    .setSkuDetails(it)
                    .build()
            billingClient.launchBillingFlow(this, billingFlowParams)
        }

        rvMarket.layoutManager = LinearLayoutManager(this)
        rvMarket.adapter = adapter

        layout_loading.visibility = View.GONE
        loadingTimer()

        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here
                    loadAllSKUs()
                }
            }
            override fun onBillingServiceDisconnected() {
                toast("Oops Try Again")
            }
        })
    }

    fun loadAllSKUs(){
        val params = SkuDetailsParams.newBuilder().setSkusList(skuList)
                .setType(BillingClient.SkuType.INAPP).build()

        billingClient.querySkuDetailsAsync(params) {billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList.isNotEmpty()) {
                items.clear()
                for (skuDetails in skuDetailsList ) {
                    items.add(skuDetails)

                }
                loadingTimer.cancel()
                layout_loading.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onStart() {
        currentRank = sharedPreferences.getString("currentRank", Rank.Toddler.toString()).toString()

        energyTime = when(enumValueOf<Rank>(currentRank)){
            Rank.Toddler -> 300
            Rank.Beginner -> 300
            Rank.Senior -> 240
            Rank.Master -> 240
            Rank.GrandMaster -> 180
        }


        shopPresenter.fetchBalance()

        countDownTimer = object : CountDownTimer(1000,1000){
            override fun onFinish() {
            }

            override fun onTick(millisUntilFinished: Long) {
            }

        }
        super.onStart()
    }

    private fun setUpEnergyTimer(){
        if (energy <= energyLimit) {
            val remainingEnergyToFull = (energyLimit - energy) * energyTime
            val currentDate = Date().time

            val lastCountEnergy = sharedPreferences.getLong("lastCountEnergy", currentDate)
            counted = sharedPreferences.getLong("countedEnergy", 0).toInt()

            diff = currentDate - lastCountEnergy
            remainTime = (remainingEnergyToFull - (diff / 1000) - counted).toInt()

            energyTimer()
        }
    }

    fun energyTimer(){
        if (!checkUpdateEnergy){ // last
            checkUpdateEnergy = true
            val energyGet = diff / 1000 / energyTime
            if (energyGet + energy >= energyLimit) // energy get + energy >= energy limit, energy = energy limit
                energy = energyLimit // else energy += energyget
            else if (energyGet > 0)
                energy += energyGet.toInt()
            if (energyGet.toInt() > 0){
                shopPresenter.updateEnergy(energy.toLong())
                counted = 0
            }
        }
        if (remainTime > 0 && energy < energyLimit){

            var timerSec = remainTime % energyTime
            var timerMin = 0

            if (timerSec > 60){
                timerMin = timerSec / 60
                timerSec %= 60
            }
            var seconds = timerSec
            countDownTimer = object : CountDownTimer((timerSec.toLong()+2) * 1000,1000){
                override fun onFinish() {
                    seconds--
                    if (seconds <= 0)
                        seconds = 0
                    tvEnergyTimer.text = "${String.format("%02d", timerMin)}:${String.format("%02d", seconds)}"
                    remainTime--
                    if (timerMin == 0){
                        energy++
                        shopPresenter.updateEnergy(energy.toLong())
                        counted = 0
                    }
                    energyTimer()
                }

                override fun onTick(millisUntilFinished: Long) {
                    if (seconds <= 0)
                        seconds = 0

                    tvEnergyTimer.text = "${String.format("%02d", timerMin)}:${String.format("%02d", seconds)}"
                    seconds--
                    counted += 1
                    remainTime--
                }

            }
            countDownTimer.start()
        }else{
            tvEnergyTimer.text = "Full"
        }


    }

    private fun popUpMessage(type: Message, message: String){
        val inflater: LayoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.pop_up_message,null)

        // Initialize a new instance of popup window
        popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
                LinearLayout.LayoutParams.MATCH_PARENT// Window height
        )

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }
        val typeface : Typeface? = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)

        val basicLayout = view.findViewById<LinearLayout>(R.id.layout_message_basic)
        val invitationLayout = view.findViewById<LinearLayout>(R.id.layout_message_invitation)
        val btnClose = view.findViewById<Button>(R.id.btnMessageClose)
        val btnReject = view.findViewById<Button>(R.id.btnMessageReject)
        val tvMessageTitle = view.findViewById<TextView>(R.id.tvMessageTitle)
        val tvMessageInfo = view.findViewById<TextView>(R.id.tvMessageInfo)

        invitationLayout.visibility = View.GONE
        basicLayout.visibility = View.VISIBLE
        tvMessageTitle.text = "Exchange"
        tvMessageInfo.typeface = typeface

        if (type == Message.Reply){
            tvMessageInfo.text = message
            btnReject.text = "No"
            btnClose.text = "yes"
            btnClose.onClick {
                btnClose.startAnimation(clickAnimation)
                activity_market.alpha = 1F
                popupWindow.dismiss()
            }

            btnReject.onClick {
                btnReject.startAnimation(clickAnimation)
                activity_market.alpha = 1F
                popupWindow.dismiss()
            }
        }
        else if(type == Message.ReadOnly){
            tvMessageInfo.text = message
            btnReject.visibility = View.GONE

            btnClose.onClick {
                btnClose.startAnimation(clickAnimation)
                activity_market.alpha = 1F
                popupWindow.dismiss()
            }
        }

        tvMessageTitle.typeface = typeface

        activity_market.alpha = 0.1F

        androidx.transition.TransitionManager.beginDelayedTransition(activity_market)
        popupWindow.showAtLocation(
                activity_market, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

    }

    override fun onPause() {
        editor = sharedPreferences.edit()
        editor.putLong("lastCountEnergy",Date().time)
        editor.putLong("countedEnergy",counted.toLong())
        editor.apply()

        Log.d("save last count", sharedPreferences.getLong("lastCountEnergy",0).toString())
        //loadingTimer.cancel()
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
            setUpEnergyTimer()
            tvEnergy.text = "${energy}/${energyLimit}"
        }
    }

    override fun response(message: String) {
        if (message == "updateEnergy"){
            tvEnergy.text = "${energy}/${energyLimit}"
        }else if (message == "updateEnergyLimit"){
            popUpMessage(Message.ReadOnly,"Success Buy")
        }else if (message == "updatePoint"){
            popUpMessage(Message.ReadOnly,"Success Buy")
        }
    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(activity_market, "The network is back !", "LONG")
                } else {
                    showSnackBar(activity_market, "There is no more network", "INFINITE")
                }
            }
        }
    }

    private fun loadingTimer(){
        val view = findViewById<View>(R.id.layout_loading)

        val tvLoadingTitle = view.findViewById<TextView>(R.id.tvLoadingTitle)
        val tvLoadingInfo = view.findViewById<TextView>(R.id.tvLoadingInfo)

        tvLoadingInfo.text = "Setting Up Best Price for You!"

        tvLoadingInfo.typeface = typeface
        tvLoadingTitle.typeface = typeface


        loadingTimer = object: CountDownTimer(12000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                when (loadingCount) {
                    3 -> tvLoadingTitle.text = "Fetching Data ."
                    2 -> tvLoadingTitle.text = "Fetching Data . ."
                    1 -> tvLoadingTitle.text = "Fetching Data . . ."
                    else -> loadingCount = 4
                }
                loadingCount--
            }

            override fun onFinish() {
                tvLoadingInfo.text = ""
                tvLoadingTitle.text = "Coming Soon . . ."
                toast("Try Again Later!")
            }
        }
        loadingTimer.start()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState === Purchase.PurchaseState.PURCHASED) {
                    acknowledgePurchase(purchase)
                }

            }
        } else if (billingResult?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {

        } else {
            toast("Error when Purchase")
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

//                if (!purchase.isAcknowledged) {
//                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
//                            .setPurchaseToken(purchase.purchaseToken)
//                    val ackPurchaseResult = withContext(Dispatchers.IO) {
//                        billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
//                    }
//                }

                val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss")
                val currentDate = sdf.format(Date())
                var name = sharedPreferences.getString("name","")
                if (name == "" && auth.currentUser != null)
                    name = Profile.getCurrentProfile().name

                val values: HashMap<String, Any>
                values  = hashMapOf(
                        "buyItem" to buyItem.title,
                        "name" to name!!,
                        "uid" to auth.currentUser!!.uid,
                        "purchaseToken" to purchase.purchaseToken
                )
                when {
                    buyItem.title.contains("500 Coin") -> shopPresenter.updatePoint((point+500).toLong())
                    buyItem.title.contains("1000 Coin") -> shopPresenter.updatePoint((point+1000).toLong())
                    buyItem.title.contains("2000 Coin") -> shopPresenter.updatePoint((point+2000).toLong())
                    buyItem.title.contains("Energy Limit to 300") -> shopPresenter.updateEnergyLimit(300)
                    buyItem.title.contains("Energy Limit to 200") -> shopPresenter.updateEnergyLimit(200)
                    buyItem.title.contains("Fulling Energy to Limit") -> shopPresenter.updateEnergyLimit(energyLimit.toLong())

                }
                if (auth.currentUser != null)
                    database.child("payment").child(currentDate).setValue(values).addOnSuccessListener {
                        toast("Success Purchase")
                        countDownTimer.cancel()
                        shopPresenter.fetchBalance()
                    }


            }

        }
    }
}
