package com.ta.tambahinaja.play.practice

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ta.tambahinaja.R
import com.ta.tambahinaja.play.GameType
import com.ta.tambahinaja.utils.setGamePlay
import kotlinx.android.synthetic.main.activity_countdown.*
import kotlinx.android.synthetic.main.activity_pre_practice_play.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class PrePracticePlayActivity : AppCompatActivity() {

    private lateinit var mAdView : AdView
    private var level = 0
    private var creditEarnAdvanced = 0
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var gamePlay: PracticeGamePlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_practice_play)

        supportActionBar?.hide()

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-1388436725980010/5926503810"

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvPracticeCountDown.typeface = typeface
        tvPracticeTutorial.typeface = typeface
        btnPracticeStartPlay.typeface = typeface
        tvPracticeTutorialTitle.typeface = typeface
        tvPracticeTimeDetail.typeface = typeface
        tvPracticeTargetScoreDetail.typeface = typeface
        tvPracticeGameTypeDetail.typeface = typeface

        tvPracticeCountDown.visibility = View.GONE

        level = intent.extras!!.getInt("level")
        creditEarnAdvanced = intent.extras!!.getInt("reward")

        btnPracticeStartPlay.onClick {
            countDown(true)
            tvPracticeCountDown.visibility = View.VISIBLE
            tvPracticeTutorial.visibility = View.GONE
            btnPracticeStartPlay.visibility = View.GONE
        }

        gamePlay = setGamePlay(level)
    }

    override fun onStart() {
        super.onStart()
        tvPracticeTargetScoreDetail.text = "Score: ${gamePlay.targetScore}"
        tvPracticeGameTypeDetail.text = "${gamePlay.gameType}"
        tvPracticeTimeDetail.text = "Time: ${gamePlay.time}"

        if(gamePlay.gameType == GameType.Normal)
            tvPracticeTutorial.text = getString(R.string.practice_normal_tutorial)
    }

    override fun onBackPressed() {
        alert ("You will Missing Your Coins"){
            title = "Exit"
            yesButton {
                super.onBackPressed()
            }
            noButton {  }
        }.show()
    }

    fun countDown(status : Boolean){
        var count = 3
        tvPracticeTutorialTitle.visibility = View.INVISIBLE

        val timer = object: CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvPracticeCountDown.text = count.toString()
                val animationSlideDown = AnimationUtils.loadAnimation(ctx,
                        R.anim.zoom_out
                )

                if (count <= 0)
                    tvPracticeCountDown.text = "Start !!"

                tvPracticeCountDown.startAnimation(animationSlideDown)
                count--
            }

            override fun onFinish() {
                startActivity(intentFor<PracticePlayActivity>("level" to level, "reward" to creditEarnAdvanced))
                finish()
            }
        }
        if (status)
            timer.start()
        else
            timer.cancel()
    }
}
