package com.example.balapplat.play

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.ContactsContract
import android.util.Log
import android.view.animation.AnimationUtils
import com.example.balapplat.MainActivity
import com.example.balapplat.R
import com.example.balapplat.model.NormalMatch
import com.facebook.Profile
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
    var status = "custom"
    var opponentPoint = 0
    var player = 1 // 1 yang ajak, 2 yang diajak, 0 main sendiri
    var facebookId = ""

    private var numberArr : MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_game)

        supportActionBar?.hide()

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()


        if (intent.extras != null){
            if(intent.extras!!.getString("facebookId").equals(null)){ // jika kamu diinvite main
                player = 2
                facebookId = Profile.getCurrentProfile().id
                fetchOpponentPoints(false)
            }
            else{ // kamu yang tukang invite
                player = 1
                facebookId = intent.extras!!.getString("facebookId")!!
                fetchOpponentPoints(true) // true kalau kamu tukang invite
            }
        }else{
            if (auth.currentUser != null)
            {
                // jika main sendiri
                getHighScore()
                player = 0
            }

        }

        keyboard()
        generate()
        control(false)
        
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
            if (player == 1)
                updateValue(true)
            else if(player == 2)
                updateValue(false)
            generate()
        }else{
            if(point != 0)
                point -= 5
        }
        tvPoint.text = "" + point
        val animationBounce = AnimationUtils.loadAnimation(ctx, R.anim.bounce)
        tvPoint.startAnimation(animationBounce)


    }

    private fun control(status : Boolean){
        val countDownTimer = object: CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timer--
                tvTimer.text = "" + timer

            }
            override fun onFinish() {

                if (auth.currentUser != null){
                    if (player == 0){
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
                    }

                }

                finish()
                startActivity<MainActivity>()
            }
        }
        if(status)
            countDownTimer.start()
        else
            countDownTimer.cancel()
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
        control(false)
        super.onDestroy()
    }

    override fun onPause() {
        control(false)
        super.onPause()
    }

    override fun onBackPressed() {
        control(false)
        super.onBackPressed()
    }

    override fun onResume() {
        control(true)
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

    fun updateValue(inviter: Boolean){
        var values: HashMap<String, Any>

        if (!inviter){
            values  = hashMapOf(
                "player1" to opponentPoint,
                "player2" to point
            )
            database.child("onPlay").child(Profile.getCurrentProfile().id).setValue(values).addOnSuccessListener {
                toast("save")

            }.addOnFailureListener {
                toast(""+ it.message)
            }
        }
        else{
            values  = hashMapOf(
                "player1" to point,
                "player2" to opponentPoint
            )

            database.child("onPlay").child(intent.extras!!.getString("facebookId")!!).setValue(values).addOnSuccessListener {
                toast("save")

            }.addOnFailureListener {
                toast(""+ it.message)
            }
        }

    }

    fun fetchOpponentPoints(inviter: Boolean){
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    opponentPoint = if (inviter)
                        p0.getValue(Play::class.java)?.player2!!
                    else
                        p0.getValue(Play::class.java)?.player1!!
                    toast("opponentPoint : " + opponentPoint)
                }
            }

        }
        database.child("invitation").child(facebookId).addValueEventListener(postListener)
    }
}

data class Play(
    var player1: Int? = 0,
    var player2: Int? = 0
)