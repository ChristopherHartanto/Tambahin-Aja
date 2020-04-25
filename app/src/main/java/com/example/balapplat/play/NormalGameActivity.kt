package com.example.balapplat.play

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AnimationUtils
import com.example.balapplat.MainActivity
import com.example.balapplat.view.MainView
import com.example.balapplat.presenter.Presenter
import com.example.balapplat.R
import com.example.balapplat.model.Inviter
import com.example.balapplat.model.NormalMatch
import com.example.balapplat.presenter.MatchPresenter
import com.example.balapplat.view.MatchView
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_normal_game.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.util.*

class NormalGameActivity : AppCompatActivity(),
    MainView, MatchView {

    private lateinit var database: DatabaseReference
    private lateinit var databaseFetchPoint: DatabaseReference
    private lateinit var auth: FirebaseAuth
    lateinit var matchPresenter: MatchPresenter
    lateinit var data: Inviter
    lateinit var presenter: Presenter
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
        databaseFetchPoint = FirebaseDatabase.getInstance().reference
        matchPresenter = MatchPresenter(this,database)
        auth = FirebaseAuth.getInstance()
        presenter = Presenter(this, database)

        if (intent.extras != null){
            if(intent.extras!!.getString("facebookId").equals(null)){ // jika kamu diinvite main
                player = 2
                facebookId = Profile.getCurrentProfile().id
                matchPresenter.fetchOpponent(false,facebookId)
                fetchProfile(false)
            }
            else{ // kamu yang tukang invite
                player = 1
                facebookId = intent.extras!!.getString("facebookId")!!
                matchPresenter.fetchOpponent(true,facebookId) // true kalau kamu tukang invite
                fetchProfile(true)
            }
            tvPoint.visibility = View.INVISIBLE

        }else{
            if (auth.currentUser != null)
            {
                // jika main sendiri
                getHighScore()
                layoutMultipleGame.visibility = View.INVISIBLE
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
            generate()
        }else{
            if(point != 0)
                point -= 5
        }
        if (player == 1)
            matchPresenter.updateValue(true,point,opponentPoint,intent.extras!!.getString("facebookId")!!)
        else if(player == 2)
            matchPresenter.updateValue(true,point,opponentPoint,intent.extras!!.getString("facebookId")!!)

        val animationBounce = AnimationUtils.loadAnimation(ctx, R.anim.bounce)

        if(player == 0){
            tvPoint.text = "" + point
            tvPoint.startAnimation(animationBounce)
        }else{
            tvPlayerPoint.text = ""+point
            tvPlayerPoint.startAnimation(animationBounce)
        }


    }

    private fun control(status : Boolean){
        val countDownTimer = object: CountDownTimer(timer.toLong()*1000, 1000) {
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
                            finish()
                            startActivity<MainActivity>()
                        }
                    }else{
                        toast("your point : "+ point + "opponent point : " + opponentPoint)
                        var text = ""
                        text = when {
                            point > opponentPoint -> {
                                matchPresenter.getStats(auth,true)
                                "win"
                            }
                            point < opponentPoint -> {
                                matchPresenter.getStats(auth,false)
                                "lose"
                            }
                            point == opponentPoint -> {
                                toast("1.  your point : "+ point + "opponent point : " + opponentPoint)
                                "draw"
                            }
                            else -> "error"
                        }

                        alert ("You $text"){
                            title = "End"
                            okButton {
                                finish()
                                startActivity<MainActivity>()
                            }
                        }.show()
                    }

                }


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


    fun fetchProfile(inviter: Boolean){
        if (!inviter){
            Picasso.get().load(getFacebookProfilePicture(intent.extras!!.getString("inviterFacebookId")!!)).fit().into(ivOpponentImage)
            tvOpponentName.text = "" + intent.extras!!.getString("inviterName")
        }else{
            Picasso.get().load(getFacebookProfilePicture(facebookId)).fit().into(ivOpponentImage)
            tvOpponentName.text = "" + intent.extras!!.getString("name")
        }
        Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivPlayerImage)
        tvPlayerName.text = "" + Profile.getCurrentProfile().name
    }


    override fun loadData(dataSnapshot: DataSnapshot) {
        presenter.replyInvitation(false)
    }

    override fun response(message: String) {

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
//        database.child("onPlay").child(facebookId).child("pause").setValue(true)
        control(false)

        alert {
            title = "Exit"
            yesButton {
                // di isi
                finish()
            }
            noButton {
                control(true)
            }
        }.show()

    }

    override fun onResume() {
       // database.child("onPlay").child(facebookId).child("pause").setValue(false)
        control(true)

        super.onResume()
    }

    override fun fetchOpponentData(dataSnapshot: DataSnapshot, inviter: Boolean) {
        val check = opponentPoint

        opponentPoint = if (inviter)
            dataSnapshot.getValue(Play::class.java)?.player2!!
        else
            dataSnapshot.getValue(Play::class.java)?.player1!!

        if (check != opponentPoint){ // jika value tidak sama dengan sebelumnya maka update data
            val animationBounce = AnimationUtils.loadAnimation(ctx, R.anim.bounce)
            tvOpponentPoint.text = "" + opponentPoint
            tvOpponentPoint.startAnimation(animationBounce)
        }
    }
}

data class Play(
    var player1: Int? = 0,
    var player2: Int? = 0
)