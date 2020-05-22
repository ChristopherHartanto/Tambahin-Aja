package com.ta.tambahinaja.home

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ta.tambahinaja.R
import com.ta.tambahinaja.friends.Message
import com.ta.tambahinaja.model.User
import com.ta.tambahinaja.presenter.CreditPresenter
import com.ta.tambahinaja.rank.Balance
import com.ta.tambahinaja.utils.showSnackBar
import com.ta.tambahinaja.view.MainView
import com.facebook.Profile
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.activity_add_friends.*
import kotlinx.android.synthetic.main.activity_credit.*
import kotlinx.android.synthetic.main.activity_credit.tvCredit
import kotlinx.android.synthetic.main.activity_market.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast

class CreditActivity : AppCompatActivity(), MainView, NetworkConnectivityListener {

    private lateinit var creditHistoryAdapter: CreditHistoryRecyclerViewAdapter
    private lateinit var adapter: CreditRecyclerViewAdapter
    private lateinit var mAdView : AdView
    private lateinit var auth: FirebaseAuth
    private lateinit var presenter: CreditPresenter
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private var creditHistoryItems : MutableList<CreditHistory> = mutableListOf()
    private var creditShopItems : MutableList<CreditShop> = mutableListOf()
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)
    private lateinit var popupWindow : PopupWindow
    private lateinit var typeface: Typeface
    private var loadingCount = 4
    private lateinit var loadingTimer : CountDownTimer
    private var credit = 0
    private var name = ""
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)!!
        sharedPreferences = this.getSharedPreferences("LOCAL_DATA",Context.MODE_PRIVATE)
        name = sharedPreferences.getString("name","").toString()
        if (name == "" && Profile.getCurrentProfile() != null)
            name = Profile.getCurrentProfile().name
        tvCredit.typeface = typeface
        tvCreditTitle.typeface = typeface
        presenter = CreditPresenter(this,database)

        loadingTimer()

        adapter = CreditRecyclerViewAdapter(this,creditShopItems){
            if (auth.currentUser == null || Profile.getCurrentProfile() == null)
                popUpMessage(Message.ReadOnly,"You Must Sign In First")
            else{
                index = it
                if (credit.toLong() < creditShopItems[it].price!!){
                    popUpMessage(Message.ReadOnly,"Not Enough Credit")
                }else if(creditShopItems[it].quantity!!.toInt() <= 0){ // lebih bagus ulang fetch soalnya kalau qty udah habis bakal berkurang lagi
                    popUpMessage(Message.ReadOnly, "Already Sold Out")
                }else{
                    popUpMessage(Message.Reply,"Do You Want to Buy?")
                }
            }
        }

        mAdView = this.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-1388436725980010/8361095467"

        if (auth.currentUser != null)
            presenter.fetchCredit()

        presenter.fetchCreditShop()
        rvCredit.layoutManager = LinearLayoutManager(this)
        rvCredit.adapter = adapter

        ivCreditHistory.onClick {
            if (auth.currentUser != null){
                creditHistoryItems.clear()
                popUpCreditHistory()
            }else
                popUpMessage(Message.ReadOnly,"You Must Sign In First")

        }
    }

    private fun popUpCreditHistory(){
        val inflater: LayoutInflater = this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.pop_up_credit_history,null)

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
        val rvCreditHistory = view.findViewById<RecyclerView>(R.id.rvCreditHistory)
        val tvCreditHistoryTitle = view.findViewById<TextView>(R.id.tvCreditHistoryTitle)
        val btnClose = view.findViewById<Button>(R.id.btnCreditHistory)

        val typeface : Typeface? = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvCreditHistoryTitle.typeface = typeface

        presenter.fetchCreditHistory()
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        rvCreditHistory.layoutManager = linearLayoutManager

        btnClose.onClick {
            btnClose.startAnimation(clickAnimation)
            activity_credit.alpha = 1F
            popupWindow.dismiss()
        }

        creditHistoryAdapter = CreditHistoryRecyclerViewAdapter(this,creditHistoryItems)
        rvCreditHistory.adapter = creditHistoryAdapter
        activity_credit.alpha = 0.1F

        TransitionManager.beginDelayedTransition(activity_credit)
        popupWindow.showAtLocation(
                activity_credit, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == "fetchCredit"){
            credit = dataSnapshot.getValue(Balance::class.java)?.credit.toString().toInt()
            tvCredit.text = "${credit}"
        }else if(response == "fetchCreditHistory"){
            for (data in dataSnapshot.children){
                val item = data.getValue(CreditHistory::class.java)
                creditHistoryItems.add(CreditHistory(data.getValue(CreditHistory::class.java)!!.info,
                        data.getValue(CreditHistory::class.java)!!.credit,
                        data.key))
            }
            creditHistoryAdapter.notifyDataSetChanged()
        }else if(response == "fetchCreditShop"){
            creditShopItems.clear()
            for (data in dataSnapshot.children){
                val item = data.getValue(CreditShop::class.java)
                creditShopItems.add(item!!)
            }
            adapter.notifyDataSetChanged()
            layout_loading.visibility = View.GONE
            loadingTimer.cancel()
        }else if(response == "fetchUser"){
            if (dataSnapshot.getValue(User::class.java)!!.email == "" || dataSnapshot.getValue(User::class.java)!!.noHandphone == "")
                popUpMessage(Message.ReadOnly,"Please Fill Your Profile First")
            else
                presenter.exchangeCredit(creditShopItems[index].price!!.toInt(),name)
        }
    }

    override fun response(message: String) {
        if (message == "exchangeCredit"){
            presenter.updateQuantity(index+1)
            val remainingCredit = credit - creditShopItems[index].price!!
            presenter.updateCredit(remainingCredit)
            presenter.fetchCredit()
            presenter.fetchCreditShop()
            popUpMessage(Message.ReadOnly,"We'll Inform You by Email After Success")
        }
    }

    private fun popUpMessage(type: Message, message: String){
        val inflater: LayoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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
        val typeface : Typeface? = ResourcesCompat.getFont(this, R.font.fredokaone_regular)

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
                presenter.fetchUser()
                btnClose.startAnimation(clickAnimation)
                activity_credit.alpha = 1F
                popupWindow.dismiss()
            }

            btnReject.onClick {
                btnReject.startAnimation(clickAnimation)
                activity_credit.alpha = 1F
                popupWindow.dismiss()
            }
        }
        else if(type == Message.ReadOnly){
            tvMessageInfo.text = message
            btnReject.visibility = View.GONE

            btnClose.onClick {
                btnClose.startAnimation(clickAnimation)
                activity_credit.alpha = 1F
                popupWindow.dismiss()
            }
        }

        tvMessageTitle.typeface = typeface

        activity_credit.alpha = 0.1F

        androidx.transition.TransitionManager.beginDelayedTransition(activity_credit)
        popupWindow.showAtLocation(
                activity_credit, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

    }

    fun loadingTimer(){
        val view = findViewById<View>(R.id.layout_loading)

        val tvLoadingTitle = view.findViewById<TextView>(R.id.tvLoadingTitle)
        val tvLoadingInfo = view.findViewById<TextView>(R.id.tvLoadingInfo)

        tvLoadingInfo.typeface = typeface
        tvLoadingTitle.typeface = typeface

        tvLoadingInfo.text = "Save Money and Money will Save You"

        loadingTimer = object: CountDownTimer(15000, 1000) {
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
                finish()
                toast("Oops Something Wrongs!")
            }
        }
        loadingTimer.start()
    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(activity_credit, "The network is back !", "LONG")
                } else {
                    showSnackBar(activity_credit, "There is no more network", "INFINITE")
                }
            }
        }
    }

    override fun onPause() {
        loadingTimer.cancel()
        super.onPause()
    }
}

data class CreditHistory(
     var info: String? = "",
     var credit: Long? = 0,
     var date: String? = ""
)

data class CreditShop(
        var title: String? = "",
        var price: Long? = 0,
        var quantity: Long? = 0
)