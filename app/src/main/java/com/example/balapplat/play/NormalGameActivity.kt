package com.example.balapplat.play

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AnimationUtils
import com.example.balapplat.MainActivity
import com.example.balapplat.R
import com.example.balapplat.model.NormalMatch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_normal_game.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*

class NormalGameActivity : AppCompatActivity(){

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var countDownTimer : CountDownTimer
    var count = 4
    var point = 0
    var playing = true
    var timer = 30
    var answer = 999
    var highScore = 0
    var gameType = "normal"

    private var numberArr : MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_game)

        supportActionBar?.hide()

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        getHighScore()
        keyboard()
        generate()
        control()
        
    }

    private fun generate(){
        tvQuestion.text = ""
        numberArr.clear()
        for(x in 0 until count)
        {
            val value = Random().nextInt(9)
            numberArr.add(value)

            tvQuestion.text = tvQuestion.text.toString() + value
        }
        answer = generateAnswer()
    }

    private fun generateAnswer() : Int {
        var result = 0
        val temp = numberArr
        for(x in 0 until count)
        {
            if(x != count-1) {
                temp[x+1] += temp[x]

                if(temp[x+1] / 10 == 1)
                    temp[x+1] -= 9
            }
            else
                result = temp[x]

        }
        return result
    }

    private fun checkAnswer(value : Int){
        if (answer == value){
            point += 10
            generate()
        }else{
            if(point != 0)
                point -= 5
        }
        tvPoint.text = "" + point
        val animationBounce = AnimationUtils.loadAnimation(ctx, R.anim.bounce)
        tvPoint.startAnimation(animationBounce)


    }

    private fun control(){
        val countDownTimer = object: CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timer--
                tvTimer.text = "" + timer

            }
            override fun onFinish() {
                if(highScore < point){

                    val values: HashMap<String, Any> = hashMapOf(
                        "score" to point
                    )

                    database.child("highscore").child(auth.currentUser!!.uid).setValue(values).addOnSuccessListener {
                        toast("save")
                        
                    }.addOnFailureListener {
                        toast(""+ it.message)
                    }
                }
                finish()
                startActivity<MainActivity>()
            }
        }
        countDownTimer.start()
    }
    
    private fun keyboard(){
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
        btn0.onClick {
            checkAnswer(0)
        }
    }

    override fun onDestroy() {
        countDownTimer.cancel()
        super.onDestroy()
    }

    override fun onPause() {
        playing = false
        //countDownTimer.cancel()
        super.onPause()
    }

    override fun onResume() {
        playing = true

        super.onResume()
    }

    fun getHighScore(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if (dataSnapshot.exists())
                highScore = dataSnapshot.getValue(NormalMatch::class.java)?.score!!

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        database.child("highscore").child(auth.currentUser!!.uid).addValueEventListener(postListener)
    }


    fun getFacebookProfilePicture(userID: String): String {
        return "https://graph.facebook.com/$userID/picture?type=large"
    }
}
