package com.example.balapplat.presenter

import android.annotation.SuppressLint
import android.view.animation.AnimationUtils
import com.example.balapplat.R
import com.example.balapplat.play.Play
import com.example.balapplat.view.MainView
import com.example.balapplat.view.MatchView
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_normal_game.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*

class MatchPresenter (private val view: MatchView, private val database: DatabaseReference){

    fun updateValue(inviter: Boolean,playerPoint: Int, opponentPoint: Int,facebookId: String){
        val values: HashMap<String, Any>

        if (!inviter){
            values  = hashMapOf(
                "player1" to opponentPoint,
                "player2" to playerPoint
            )
            database.child("onPlay").child(Profile.getCurrentProfile().id).setValue(values).addOnSuccessListener {

            }.addOnFailureListener {
            }
        }
        else{
            values  = hashMapOf(
                "player1" to playerPoint,
                "player2" to opponentPoint
            )

            database.child("onPlay").child(facebookId).setValue(values).addOnSuccessListener {

            }.addOnFailureListener {

            }
        }

    }

    fun getStats(auth: FirebaseAuth,winStatus : Boolean){
        var temp = 0
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (temp == 0){
                    if(p0.exists())
                        updateStats(winStatus, p0.value as Long,auth)
                    else
                        updateStats(winStatus, 0,auth)
                    temp = 1
                }


            }

        }
        if (winStatus)
            database.child("stats").child(auth.currentUser!!.uid).child("win").addValueEventListener(postListener)
        else
            database.child("stats").child(auth.currentUser!!.uid).child("lose").addValueEventListener(postListener)

    }

    fun updateStats(winStatus: Boolean, value: Long, auth: FirebaseAuth) {
        if (winStatus) {
            database.child("stats").child(auth.currentUser!!.uid).child("win").setValue(value + 1)
        } else {
            database.child("stats").child(auth.currentUser!!.uid).child("lose").setValue(value + 1)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun addToHistory(auth: FirebaseAuth, playerPoint: Int, opponentPoint: Int, opponentFacebookId: String, opponentName: String){
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())

        val status = when {
            playerPoint > opponentPoint -> {
                "win"
            }
            playerPoint == opponentPoint -> {
                "lose"
            }
            else -> "draw"
        }

        val values  = hashMapOf(
            "status" to status,
            "point" to playerPoint,
            "opponentFacebookId" to opponentFacebookId,
            "opponentName" to opponentName,
            "opponentPoint" to opponentPoint
        )
        database.child("history").child(auth.currentUser!!.uid).child("" +currentDate).setValue(values)
    }

    fun removeOnPlay(){
        database.child("history").child(Profile.getCurrentProfile().id).removeValue()
    }

    fun fetchOpponent(inviter: Boolean, facebookId: String){
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    view.fetchOpponentData(p0,inviter)
                }
            }

        }
        database.child("onPlay").child(facebookId).addValueEventListener(postListener)
//
//        val postListener1 = object : ValueEventListener {
//            override fun onCancelled(p0: DatabaseError) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//
//            override fun onDataChange(p0: DataSnapshot) {
//                if(p0.exists())
//                {
//                    if (p0.value!! == true)
//                        control(false)
//                    else
//                        control(true)
//                }
//            }
//
//        }
//        database.child("onPlay").child(facebookId).child("pause").addValueEventListener(postListener1)
    }
}