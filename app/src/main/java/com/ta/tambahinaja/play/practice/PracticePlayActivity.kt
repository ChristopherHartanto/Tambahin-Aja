package com.ta.tambahinaja.play.practice

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ta.tambahinaja.R
import com.ta.tambahinaja.model.Inviter
import com.ta.tambahinaja.play.GameType
import com.ta.tambahinaja.play.StatusPlayer
import com.ta.tambahinaja.presenter.MatchPresenter
import com.ta.tambahinaja.presenter.PracticePresenter
import com.ta.tambahinaja.utils.setGamePlay
import kotlinx.android.synthetic.main.activity_normal_game.*
import kotlinx.android.synthetic.main.activity_practice_play.*
import kotlinx.android.synthetic.main.activity_practice_play.cvTimer
import kotlinx.android.synthetic.main.activity_practice_play.tvPoint
import kotlinx.android.synthetic.main.activity_practice_play.tvQuestion
import kotlinx.android.synthetic.main.activity_practice_play.tvTimer
import kotlinx.android.synthetic.main.activity_practice_play.tvWinStreak
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.util.*

class PracticePlayActivity : AppCompatActivity() {

    private lateinit var mAdView : AdView
    private lateinit var gamePlay : PracticeGamePlay
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private var bundle: Bundle = Bundle()
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var countDownTimer : CountDownTimer
    private var isRunningCountDownTimer = false
    lateinit var editor: SharedPreferences.Editor
    private lateinit var soundPool : SoundPool
    private var soundCorrect = 0
    private var level = 0
    private var creditReward = 0
    private var point = 0
    private var timer = 40
    private var answer = 999
    private var type = GameType.Normal
    private var mix = false
    private var winStreak = 0
    private lateinit var popupWindow : PopupWindow
    private var showPopUp = false
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)
    private var numberArr : MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice_play)

        supportActionBar?.hide()

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        sharedPreference =  this.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvPoint.typeface = typeface
        tvTimer.typeface = typeface
        tvQuestion.typeface = typeface

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-1388436725980010/5926503810"

        level = intent.extras!!.getInt("level")
        creditReward = intent.extras!!.getInt("reward")
        gamePlay = setGamePlay(level)
        timer = gamePlay.time!!

        if (gamePlay.gameType == GameType.Normal)
            normalKeyboard()
        else if (gamePlay.gameType == GameType.OddEven)
            oddEvenKeyboard()
        else if (gamePlay.gameType == GameType.Mix)
            mix = true

        generate()
    }

    override fun onStart() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            soundPool = SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build()
        } else {
            soundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
        soundCorrect = soundPool.load(this,R.raw.answer_true,1)

        super.onStart()
    }

    override fun onResume() {
        if (!isRunningCountDownTimer && gamePlay.usingTime!!)
            countDownTimerStart(true)
        else if(!gamePlay.usingTime!!)
            cvTimer.visibility = View.GONE

        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        alert ("You'll be Leave on This Game"){
            title = "Exit"
            yesButton {
                // di isi
                finish()
            }
            noButton {
            }
        }.show()

    }

    @SuppressLint("SetTextI18n")
    private fun generate(){
        if (mix){
            when((0 until 4).random()){
                0 -> {
                    type = GameType.Normal
                    normalKeyboard()
                }
                1 -> {
                    type = GameType.AlphaNum
                    normalKeyboard()
                }
                else -> {
                    type = GameType.OddEven
                    oddEvenKeyboard()
                }
            }
        }
        if (type == GameType.AlphaNum){
            tvQuestion.text = ""
            numberArr.clear()
            for(x in 0 until gamePlay.countDigit!!)
            {
                var value = 0
                val choose = Random().nextInt(2)

                if (choose == 1){
                    value = Random().nextInt(gamePlay.difficultyAlpha!!) + 65
                    numberArr.add(value)
                    tvQuestion.text = tvQuestion.text.toString() + value.toChar()
                }else{
                    value = Random().nextInt(gamePlay.difficulty!!)
                    numberArr.add(value)
                    tvQuestion.text = tvQuestion.text.toString() + value
                }
            }
        }else if(type == GameType.DoubleAttack){

            tvQuestion.text = ""
            numberArr.clear()
            for(x in 0 until gamePlay.countDigit!!)
            {
                val value = Random().nextInt(5)
                numberArr.add(value)

                tvQuestion.text = tvQuestion.text.toString() + value
            }
        }else{
            tvQuestion.text = ""
            numberArr.clear()
            for(x in 0 until gamePlay.countDigit!!)
            {
                val value = gamePlay.difficulty?.let { Random().nextInt(it) }
                numberArr.add(value!!)

                tvQuestion.text = tvQuestion.text.toString() + value
            }
        }

        answer = generateAnswer()
        //toast(""+answer)
    }

    private fun generateAnswer() : Int {
        var result = 0
        if(type == GameType.Normal|| type == GameType.OddEven || type == GameType.Rush){
            val temp = numberArr
            for(x in 0 until gamePlay.countDigit!!)
            {
                if(x != gamePlay.countDigit!!-1) {
                    temp[x+1] += temp[x]

                    if(temp[x+1] / 10 == 1)
                        temp[x+1] -= 9
                }
                else
                    result = temp[x]

            }
            if (type == GameType.OddEven){
                result = if (result % 2 == 0)
                    0 // genap
                else
                    1 // ganjil
            }
        }else if(type == GameType.AlphaNum){
            val temp = numberArr
            for(x in 0 until gamePlay.countDigit!!)
            {
                if(x != gamePlay.countDigit!!-1) {
                    if (temp[x] >= 65)
                        temp[x] -= 64
                    if (temp[x+1] >= 65)
                        temp[x+1] -= 64

                    temp[x+1] += temp[x]

                    if(temp[x+1] / 10 == 1)
                        temp[x+1] -= 9
                }
                else
                    result = temp[x]

            }
        }else if (type == GameType.DoubleAttack){
            val temp = numberArr
            for(x in 0 until gamePlay.countDigit!!)
            {
                if(x != gamePlay.countDigit!!-1) {
                    if (x == 0){
                        temp[0] *= 2
                        if(temp[0] / 10 == 1)
                            temp[0] = temp[0] * 2 - 9
                    }

                    temp[x+1] *= 2
                    if(temp[x+1] / 10 == 1)
                        temp[x+1] = temp[x+1] * 2 - 9

                    temp[x+1] += temp[x]
                    if(temp[x+1] / 10 == 1)
                        temp[x+1] -= 9
                }
                else
                    result = temp[x]

            }
        }

        return result
    }

    private fun checkAnswer(value : Int){
        if (timer > 0){
            if (type == GameType.Normal){
                if (answer == value){
                    point += 7
                    if (mix)
                        point += 2
                }else{
                    if(point > 3)
                        point -= 3
                    else
                        point = 0
                }

            }else if (type == GameType.OddEven){
                if (answer == value){
                    point += 6
                    if (mix)
                        point += 2
                }else{
                    if(point > 5)
                        point -= 5
                    else
                        point = 0
                }
            }else if (type == GameType.AlphaNum){
                if (answer == value){
                    point += 10
                    if (mix)
                        point += 2
                }else{
                    if(point > 5)
                        point -= 5
                    else
                        point = 0
                }
            }else if (type == GameType.Rush){
                if (answer == value){
                    point += 9
                    countDownTimer.cancel()
                    timer = gamePlay.time!!
                    countDownTimerStart(true)
                }else{
                    if(point > 3)
                        point -= 3
                    else
                        point = 0
                }
            }
            else if (type == GameType.DoubleAttack){
                if (answer == value){
                    point += 12
                }else{
                    if(point > 4)
                        point -= 4
                    else
                        point = 0
                }

            }

            if (answer == value){

                soundPool.play(soundCorrect,1.0F,1.0F,1,0,1.0F)

                generate()
            }

            if (point >= gamePlay.targetScore!!){
                startActivity(intentFor<PostPracticePlayActivity>("success" to true,
                        "reward" to creditReward, "level" to level))
                finish()
            }

            val animationBounce = AnimationUtils.loadAnimation(ctx, R.anim.bounce)

            tvPoint.text = ""+point
            tvPoint.startAnimation(animationBounce)
        }
    }

    private fun countDownTimerStart(start: Boolean){
        countDownTimer = object: CountDownTimer(timer.toLong()*1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                isRunningCountDownTimer = true
                if (timer < 4) {
                    val animationFadeIn = AnimationUtils.loadAnimation(ctx, R.anim.fade_in)
                    cvTimer.startAnimation(animationFadeIn)
                }
                if (timer <= 0) {
                    tvTimer.text = "time : " + 0
                } else {
                    timer--
                    tvTimer.text = "time : " + timer
                }

            }

            override fun onFinish() {
                startActivity(intentFor<PostPracticePlayActivity>("success" to false))
                finish()
            }
        }
    }

    private fun normalKeyboard(){

        val view = findViewById<View>(R.id.layout_keyboard)
        val viewHide = findViewById<View>(R.id.layout_odd_even_keyboard)

        view.visibility = View.VISIBLE
        viewHide.visibility = View.GONE
        //layout_odd_even_keyboard.layoutParams = LinearLayout.LayoutParams(0,0)

        val btn1 = view.findViewById<Button>(R.id.btn1)
        val btn2 = view.findViewById<Button>(R.id.btn2)
        val btn3 = view.findViewById<Button>(R.id.btn3)
        val btn4 = view.findViewById<Button>(R.id.btn4)
        val btn5 = view.findViewById<Button>(R.id.btn5)
        val btn6 = view.findViewById<Button>(R.id.btn6)
        val btn7 = view.findViewById<Button>(R.id.btn7)
        val btn8 = view.findViewById<Button>(R.id.btn8)
        val btn9 = view.findViewById<Button>(R.id.btn9)
        val btn0 = view.findViewById<Button>(R.id.btn0)

        btn0.onClick {
            checkAnswer(0)
        }
        btn1.onClick {
            checkAnswer(1)
        }
        btn2.onClick {
            checkAnswer(2)
        }
        btn3.onClick {
            checkAnswer(3)
        }
        btn4.onClick {
            checkAnswer(4)
        }
        btn5.onClick {
            checkAnswer(5)
        }
        btn6.onClick {
            checkAnswer(6)
        }
        btn7.onClick {
            checkAnswer(7)
        }
        btn8.onClick {
            checkAnswer(8)
        }
        btn9.onClick {
            checkAnswer(9)
        }
    }

    private fun oddEvenKeyboard(){
        val view = findViewById<View>(R.id.layout_odd_even_keyboard)
        val viewHide = findViewById<View>(R.id.layout_keyboard)

        //layout_keyboard.layoutParams = LinearLayout.LayoutParams(0,0)
        viewHide.visibility = View.GONE
        view.visibility = View.VISIBLE

        val btnOdd = view.findViewById<Button>(R.id.btnOdd)
        val btnEven = view.findViewById<Button>(R.id.btnEven)

        btnOdd.onClick {
            checkAnswer(1)

            val handler = Handler()
            btnEven.isEnabled = false
            btnOdd.isEnabled = false
            handler.postDelayed({
                btnOdd.isEnabled = true
                btnEven.isEnabled = true
            }, 1000)
        }

        btnEven.onClick {
            checkAnswer(0)

            val handler = Handler()
            btnEven.isEnabled = false
            btnOdd.isEnabled = false
            handler.postDelayed({
                btnOdd.isEnabled = true
                btnEven.isEnabled = true
            }, 1000)
        }
    }

}
data class PracticeGamePlay(
        var gameType : GameType? = GameType.Normal,
        var difficulty : Int? = 4,
        var difficultyAlpha : Int? = 5,
        var countDigit : Int? = 5,
        var scoreTrue : Int? = 10,
        var scoreFalse : Int? = 5,
        var targetScore : Int? = 50,
        var usingTime : Boolean? = false,
        var time : Int? = 30
)