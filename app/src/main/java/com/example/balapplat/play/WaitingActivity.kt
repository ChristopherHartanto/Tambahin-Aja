package com.example.balapplat.play

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.animation.AnimationUtils
import com.example.balapplat.CountdownActivity
import com.example.balapplat.R
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_countdown.*
import kotlinx.android.synthetic.main.activity_waiting.*
import org.jetbrains.anko.*
import java.util.HashMap

class WaitingActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)

        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance().reference



       if (intent.extras != null){
           intent.extras!!.getString("facebookId")?.let { makeInvitation(it) }
       }else{
           timer()
       }

    }

    fun timer(){
        var count = 3

        val timer = object: CountDownTimer(40000, 1000) {
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

                }
            }

        }
        database.child("invitation").child(facebookId).addValueEventListener(postListener)
    }

    fun createGame(facebookId: String){
        val values: HashMap<String, Any> = hashMapOf(
            "score1" to 0,
            "score2" to 0
        )
        database.child("onPlay").child(facebookId).setValue(values).addOnSuccessListener {
            toast("create game")
            startActivity<CountdownActivity>()

        }.addOnFailureListener {
            toast(""+ it.message)
        }
    }
}
data class Status(var status: Boolean? = false)