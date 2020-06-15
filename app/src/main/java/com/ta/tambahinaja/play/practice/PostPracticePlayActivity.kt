package com.ta.tambahinaja.play.practice

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ta.tambahinaja.R
import kotlinx.android.synthetic.main.activity_post_practice_play.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.text.SimpleDateFormat
import java.util.*

class PostPracticePlayActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var mAdView : AdView
    private var success = false
    private var level = 0
    private var reward = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_practice_play)

        supportActionBar?.hide()

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-1388436725980010/5926503810"

        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvPostPracticeTitle.typeface = typeface
        tvPostPracticeReward.typeface = typeface
        tvPostPracticeLevelUpTo.typeface = typeface
        tvPostPracticeRewardTitle.typeface = typeface
        btnPostPracticeClose.typeface = typeface

        success = intent.extras!!.getBoolean("success")
        level = intent.extras!!.getInt("level")
        reward = intent.extras!!.getInt("reward")

        if (!success){
            tvPostPracticeTitle.text = "Try Again"
            tvPostPracticeLevelUpTo.visibility = View.INVISIBLE
        }else if(reward == 0)
            tvPostPracticeLevelUpTo.visibility = View.INVISIBLE

        btnPostPracticeClose.onClick {
            finish()
        }
    }

    override fun onStart() {
        if(success){
            updateCredit(reward.toLong())
            if (level != 30)
                updateLevel(level+1)
        }
        super.onStart()
    }

    override fun onResume() {

        if (success) {
            if (level != 30)
                tvPostPracticeLevelUpTo.text = "Up to Level ${level+1}"

            animateTextView(0,reward,tvPostPracticeReward)
        }

        super.onResume()
    }

    fun updateCredit(credit:Long){
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss")
        val currentDate = sdf.format(Date())


        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    val totalCredit = p0.value.toString().toInt() + credit
                    val values: HashMap<String, Any> = hashMapOf(
                            "info" to "practice",
                            "credit" to credit
                    )

                    database.child("users").child(auth.currentUser!!.uid)
                            .child("balance").child("credit").setValue(totalCredit)
                    database.child("users").child(auth.currentUser!!.uid).child("creditHistory")
                            .child(currentDate).setValue(values)
                }
            }
        }
        database.child("users").child(auth.currentUser!!.uid).child("balance").child("credit").addListenerForSingleValueEvent(postListener)

    }

    fun updateLevel(level: Int){
        database.child("users").child(auth.currentUser!!.uid).child("currentLevel")
                .setValue(level)

    }

    private fun animateTextView(initialValue : Int,finalValue : Int,textView : TextView) {

        val valueAnimator = ValueAnimator.ofInt(initialValue, finalValue)
        valueAnimator.duration = 1200

        valueAnimator.addUpdateListener { textView.text = valueAnimator.animatedValue.toString() }
        valueAnimator.start()

    }
}
