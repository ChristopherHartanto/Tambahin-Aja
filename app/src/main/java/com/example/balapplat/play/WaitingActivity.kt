package com.example.balapplat.play

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import com.example.balapplat.view.MainView
import com.example.balapplat.R
import com.example.balapplat.presenter.Presenter
import com.example.balapplat.presenter.WaitingPresenter
import com.example.balapplat.view.WaitingView
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_waiting.*
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread

class WaitingActivity : AppCompatActivity(),
    WaitingView {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    var inviter = false
    lateinit var presenter: Presenter
    lateinit var waitingPresenter: WaitingPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)

        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
       // presenter = Presenter(this,database)
        waitingPresenter = WaitingPresenter(this,database)
        waitingPresenter.loadTips()

       if (intent.extras != null){
           if (intent.extras!!.getString("facebookId") != null){ // jika main bareng teman
               inviter = true
               intent.extras!!.getString("facebookId")?.let { waitingPresenter.makeInvitation(it) }
               Picasso.get().load(getFacebookProfilePicture(intent.extras!!.getString("facebookId")!!)).fit().into(ivOpponentImageWaiting)
               Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivPlayerWaiting)
           }else if(intent.extras!!.getBoolean("playOnline")){ // jika main online
               tvWaiting.text = "Searching . . ."
               Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivPlayerWaiting)
               waitingPresenter.getWaitingList()
           }

       }else{
           timer("Waiting")
       }

    }

    fun timer(text: String){
        var count = 3

        val timer = object: CountDownTimer(100000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (count == 3)
                    tvWaiting.text = "$text ."
                else if (count == 2)
                    tvWaiting.text = "$text . ."
                else if (count == 1)
                    tvWaiting.text = "$text . . ."
                else
                    count = 4
                count--
            }

            override fun onFinish() {
                finish()
                startActivity(intentFor<NormalGameActivity>().clearTask())
            }
        }
        timer.start()
    }

    private fun getFacebookProfilePicture(userID: String): String {
        return "https://graph.facebook.com/$userID/picture?type=large"
    }

    override fun loadData(dataSnapshot: DataSnapshot, creator: Boolean) {
        tvWaiting.text = "Preparing"
        if (creator)
            Picasso.get().load(getFacebookProfilePicture(dataSnapshot.getValue(OpponentOnline::class.java)!!.facebookId!!)).fit().into(ivPlayerWaiting)
        else
            Picasso.get().load(getFacebookProfilePicture(dataSnapshot.key!!)).fit().into(ivPlayerWaiting)

        if (creator){
            startActivity(intentFor<CountdownActivity>("facebookId" to dataSnapshot.getValue(OpponentOnline::class.java)!!.facebookId
                , "name" to dataSnapshot.getValue(OpponentOnline::class.java)!!.name
                , "playOnline" to true
                , "creator" to true))
        }else{
            startActivity(intentFor<CountdownActivity>("facebookId" to dataSnapshot.key
                , "name" to dataSnapshot.getValue(OpponentOnline::class.java)!!.name
                , "playOnline" to true
                , "creator" to false))
        }

    }

    override fun response(message: String) {
        when {
            message === "accepted" -> intent.extras!!.getString("facebookId")?.let { waitingPresenter.createGame(it,false) }
            message === "createGame" -> {
                toast("create game")
                finish()
                startActivity(intentFor<CountdownActivity>("facebookId" to intent.extras!!.getString("facebookId"), "name" to intent.extras!!.getString("name")))
            }
            message === "invitationSent" -> {
                toast("invitation sent")
                intent.extras!!.getString("facebookId")?.let { it1 -> waitingPresenter.getResponse(it1) }
            }
            else // error message
            -> toast(""+message)
        }
    }

    override fun loadTips(tips: String) {
        tvTips.text = tips
    }

    override fun onDestroy() {
        if (inviter)
            waitingPresenter.removeInvitation(intent.extras!!.getString("facebookId")!!)

        super.onDestroy()
    }

    override fun onBackPressed() {
        waitingPresenter.removeWaitingList()
        super.onBackPressed()
    }


}
data class Status(var status: Boolean? = false)
data class OpponentOnline(
    var name: String? = "",
    var facebookId: String? = "",
    var status: Boolean? = false
)