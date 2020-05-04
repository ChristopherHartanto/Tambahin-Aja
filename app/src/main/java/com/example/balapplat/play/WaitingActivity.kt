package com.example.balapplat.play

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import androidx.core.content.res.ResourcesCompat
import com.example.balapplat.main.MainActivity
import com.example.balapplat.view.MainView
import com.example.balapplat.R
import com.example.balapplat.presenter.Presenter
import com.example.balapplat.presenter.WaitingPresenter
import com.example.balapplat.utils.showSnackBar
import com.example.balapplat.view.WaitingView
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_waiting.*
import org.jetbrains.anko.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast

class WaitingActivity : AppCompatActivity(), NetworkConnectivityListener, MainView,
    WaitingView {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    var inviter = false
    lateinit var presenter: Presenter
    lateinit var waitingPresenter: WaitingPresenter
    var registerWaitingList = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)

        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        presenter = Presenter(this,database)
        waitingPresenter = WaitingPresenter(this,database)
        waitingPresenter.loadTips()

        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvTips.typeface = typeface
        tvWaiting.typeface = typeface

       if (intent.extras != null){
           if (intent.extras!!.getString("joinFriendFacebookId") != null){ // jika main bareng teman
               inviter = true
               intent.extras!!.getString("joinFriendFacebookId")?.let { waitingPresenter.makeInvitation(it) }
               Picasso.get().load(getFacebookProfilePicture(intent.extras!!.getString("joinFriendFacebookId")!!)).fit().into(ivOpponentImageWaiting)
               Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivPlayerWaiting)

               timer("Waiting")

           }else if(intent.extras!!.getBoolean("playOnline")){ // jika main online
               timer("Searching")
               Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivPlayerWaiting)
               waitingPresenter.getWaitingList()
           }

       }

    }

    fun timer(text: String){
        var count = 3

        val timer = object: CountDownTimer(1000000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                when (count) {
                    3 -> tvWaiting.text = "$text ."
                    2 -> tvWaiting.text = "$text . ."
                    1 -> tvWaiting.text = "$text . . ."
                    else -> count = 4
                }
                count--
            }

            override fun onFinish() {
                finish()
                startActivity(intentFor<MainActivity>().clearTask())
            }
        }
        timer.start()

    }

    private fun getFacebookProfilePicture(userID: String): String {
        return "https://graph.facebook.com/$userID/picture?type=large"
    }

    override fun loadData(dataSnapshot: DataSnapshot, creator: Boolean) {
        timer("Preparing")
        if (creator)
            Picasso.get().load(getFacebookProfilePicture(dataSnapshot.getValue(OpponentOnline::class.java)!!.facebookId!!)).fit().into(ivOpponentImageWaiting)
        else
            Picasso.get().load(getFacebookProfilePicture(dataSnapshot.key!!)).fit().into(ivOpponentImageWaiting)


        if (creator){
            finish()

            startActivity(intentFor<CountdownActivity>("joinOnlineFacebookId" to dataSnapshot.getValue(OpponentOnline::class.java)!!.facebookId
                , "joinOnlineName" to dataSnapshot.getValue(OpponentOnline::class.java)!!.name
                , "status" to StatusPlayer.Creator, "type" to GameType.Normal))
        }else{
            finish()
            startActivity(intentFor<CountdownActivity>("creatorFacebookId" to dataSnapshot.key
                , "creatorName" to dataSnapshot.getValue(OpponentOnline::class.java)!!.name
                , "status" to StatusPlayer.JoinOnline, "type" to GameType.Normal))
        }

    }

    override fun loadData(dataSnapshot: DataSnapshot) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun response(message: String) {
        when {
            message === "accepted" -> intent.extras!!.getString("facebookId")?.let { waitingPresenter.createGame(it,false) }
            message === "createGame" -> {
                toast("create game")
                finish()
                startActivity(intentFor<CountdownActivity>("joinFriendFacebookId" to intent.extras!!.getString("joinFriendFacebookId"),
                        "joinFriendName" to intent.extras!!.getString("joinFriendName"),
                        "status" to StatusPlayer.Inviter, "type" to GameType.Normal))
            }
            message === "invitationSent" -> {
                toast("invitation sent")
                intent.extras!!.getString("joinFriendFacebookId")?.let { it1 -> waitingPresenter.getResponse(it1) }
            }
            message === "registerToWaitingList" -> {
                registerWaitingList = true
            }
            else // error message
            -> toast(""+message)
        }
    }

    override fun loadTips(tips: String) {
        tvTips.text = tips
    }

    override fun onPause() {
        dismissWaiting()

        super.onPause()
    }

    override fun onDestroy() {
        dismissWaiting()

        super.onDestroy()
    }

    override fun onBackPressed() {
        dismissWaiting()

        super.onBackPressed()
    }

    fun dismissWaiting(){
        if (intent.extras!!.getBoolean("playOnline")){
            if (registerWaitingList)
                waitingPresenter.removeWaitingList()
            waitingPresenter.dismissListenerOnline()
        }
        else if (inviter)
            waitingPresenter.removeInvitation(intent.extras!!.getString("joinFriendFacebookId")!!)
    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(activity_waiting, "The network is back !", "LONG")
                } else {
                    showSnackBar(activity_waiting, "There is no more network", "INFINITE")
                }
            }
        }
    }
}
data class Status(var status: Boolean? = false)
data class OpponentOnline(
    var name: String? = "",
    var facebookId: String? = "",
    var status: Boolean? = false
)