package com.example.balapplat.rank

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.balapplat.play.CountdownActivity
import com.example.balapplat.Helper
import com.example.balapplat.MainView
import com.example.balapplat.Presenter
import com.example.balapplat.R
import com.example.balapplat.model.Inviter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_rank.*
import org.jetbrains.anko.*

class RankActivity : AppCompatActivity(), MainView {

    lateinit var helper : Helper
    private lateinit var mAdView : AdView
    private lateinit var adapter: RankRecyclerViewAdapter
    private lateinit var database: DatabaseReference
    lateinit var presenter: Presenter
    lateinit var data: Inviter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rank)

        supportActionBar?.hide()
        helper = Helper()
        database = FirebaseDatabase.getInstance().reference
        presenter = Presenter(this,database)
        presenter.receiveInvitation()

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-1388436725980010/2294808192"

        val items : MutableList<String> = mutableListOf("Normal", "Odd Even", "Rush", "AlphaNum")
        adapter = RankRecyclerViewAdapter(this,items){
            finish()
            startActivity<CountdownActivity>()
        }

        rvRank.layoutManager = GridLayoutManager(this,2)
        rvRank.adapter = adapter
    }

    override fun onStart() {
        helper.userActive(true)
        super.onStart()
    }

    override fun onDestroy() {
        helper.userActive(false)
        super.onDestroy()
    }

    override fun loadData(dataSnapshot: DataSnapshot) {
        data = dataSnapshot.getValue(Inviter::class.java)!!

        alert(data!!.name + " invite you to play"){
            title = "Invitation"
            yesButton {
                presenter.replyInvitation(true)
            }
            noButton {
                presenter.replyInvitation(false)
            }
        }.show()
    }

    override fun response(message: String) {
        if (message === "acceptedGame"){
            toast("acceptedGame")

            startActivity(intentFor<CountdownActivity>("inviterFacebookId" to data.facebookId,
                "inviterName" to data.name))
        }

    }
}
