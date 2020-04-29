package com.example.balapplat.play

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.balapplat.CountdownActivity
import com.example.balapplat.R
import com.example.balapplat.utils.UtilsConstants
import com.example.balapplat.utils.showSnackBar
import com.facebook.Profile
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_waiting.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*

class WaitingActivity : AppCompatActivity(), NetworkConnectivityListener {

    private lateinit var database: DatabaseReference
    var inviter = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)

        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance().reference


       if (intent.extras != null){
           inviter = true
           intent.extras!!.getString("facebookId")?.let { makeInvitation(it) }
           Picasso.get().load(getFacebookProfilePicture(intent.extras!!.getString("facebookId")!!)).fit().into(ivOpponentImageWaiting)
           Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivPlayerWaiting)
       }else{
           timer()
       }

    }

    fun timer(){
        var count = 3

        val timer = object: CountDownTimer(100000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (count == 3)
                    tvWaiting.text = "Waiting ."
                else if (count == 2)
                    tvWaiting.text = "Waiting . ."
                else if (count == 1)
                    tvWaiting.text = "Waiting . . ."
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

    fun makeInvitation(facebookId : String){
        database.child("invitation").child(facebookId).child("status").setValue(false).addOnSuccessListener {
            toast("invitation sent")
            intent.extras!!.getString("facebookId")?.let { it1 -> getResponse(it1) }

        }.addOnFailureListener {
            toast(""+ it.message)
        }
    }

    fun getResponse(facebookId : String){
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    Log.i("cek1",""+p0)
                    if(p0.getValue(Status::class.java)!!.status!!){
                        intent.extras!!.getString("facebookId")?.let { createGame(it) }
                    }

                }else{

//                           alert{
//                            title = "Reject"
//                            yesButton {
//                                startActivity(intentFor<MainActivity>())
//                            }
//                               noButton {  }
//                        }.show()
                }
            }

        }
        database.child("invitation").child(facebookId).addValueEventListener(postListener)
    }

    fun createGame(facebookId: String){
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())

        val values: HashMap<String, Any> = hashMapOf(
            "player1" to 0,
            "player2" to 0,
            "date" to currentDate,
            "pause" to false
        )
        database.child("onPlay").child(facebookId).setValue(values).addOnSuccessListener {
            toast("create game")
            finish()
            startActivity(intentFor<CountdownActivity>("facebookId" to facebookId))

        }.addOnFailureListener {
            toast(""+ it.message)
        }
    }

    override fun onDestroy() {
        if (inviter)
            database.child("invitation").child(intent.extras!!.getString("facebookId")!!).removeValue()

        super.onDestroy()
    }

    fun getFacebookProfilePicture(userID: String): String {
        return "https://graph.facebook.com/$userID/picture?type=large"
    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(
                        activity_waiting,
                        "Connection Established",
                        UtilsConstants.SNACKBAR_LONG
                    ).show()
                } else {
                    showSnackBar(
                        activity_waiting,
                        "No Network !",
                        UtilsConstants.SNACKBAR_INFINITE
                    ).show()
                }
            }
        }
    }
}
data class Status(var status: Boolean? = false)