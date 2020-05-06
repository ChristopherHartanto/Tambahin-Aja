package com.example.balapplat.home

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
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
import com.example.balapplat.R
import com.example.balapplat.presenter.CreditPresenter
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
import kotlinx.android.synthetic.main.activity_credit.tvCredit
import kotlinx.android.synthetic.main.activity_market.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.act
import org.jetbrains.anko.sdk27.coroutines.onClick

class CreditActivity : AppCompatActivity(), MainView {

    private lateinit var creditHistoryAdapter: CreditHistoryRecyclerViewAdapter
    private lateinit var adapter: CreditRecyclerViewAdapter
    private lateinit var mAdView : AdView
    private lateinit var presenter: CreditPresenter
    private lateinit var database: DatabaseReference
    private var creditHistoryItems : MutableList<CreditHistory> = mutableListOf()
    private var creditShopItems : MutableList<CreditShop> = mutableListOf()
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)
    private lateinit var popupWindow : PopupWindow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit)

        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance().reference
        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvCredit.typeface = typeface
        tvCreditTitle.typeface = typeface
        presenter = CreditPresenter(this,database)

        adapter = CreditRecyclerViewAdapter(this,creditShopItems)

        mAdView = this.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.SMART_BANNER
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        presenter.fetchCredit()
        presenter.fetchCreditShop()
        rvCredit.layoutManager = LinearLayoutManager(this)
        rvCredit.adapter = adapter

        ivCreditHistory.onClick {
            creditHistoryItems.clear()
            popUpCreditHistory()
        }
    }

    private fun popUpCreditHistory(){
        val inflater: LayoutInflater = this!!.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.pop_up_credit_history,null)
        val main_view = inflater.inflate(R.layout.activity_main,null)


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
        rvCreditHistory.layoutManager = LinearLayoutManager(this)

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
        if (response == "fetchCredit")
            tvCredit.text = dataSnapshot.getValue(Balance::class.java)?.credit.toString()
        else if(response == "fetchCreditHistory"){
            for (data in dataSnapshot.children){
                val item = data.getValue(CreditHistory::class.java)
                creditHistoryItems.add(CreditHistory(data.getValue(CreditHistory::class.java)!!.info,
                        data.getValue(CreditHistory::class.java)!!.credit,
                        data.key))
            }
            creditHistoryAdapter.notifyDataSetChanged()
        }else if(response == "fetchCreditShop"){
            for (data in dataSnapshot.children){
                val item = data.getValue(CreditShop::class.java)
                creditShopItems.add(item!!)
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun response(message: String) {

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