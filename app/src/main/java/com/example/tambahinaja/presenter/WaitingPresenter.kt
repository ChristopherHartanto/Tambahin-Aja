package com.example.tambahinaja.presenter

import android.content.SharedPreferences
import android.util.Log
import com.example.tambahinaja.play.CountdownActivity
import com.example.tambahinaja.play.GameType
import com.example.tambahinaja.play.OpponentOnline
import com.example.tambahinaja.play.Status
import com.example.tambahinaja.view.WaitingView
import com.facebook.Profile
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.HashMap

class WaitingPresenter(private val view: WaitingView, private val database: DatabaseReference) {

    val postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onDataChange(p0: DataSnapshot) {

        }

    }

    fun getWaitingList(name: String){
        var count = 0
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                view.response(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    for (data in p0.children){
                        if (data.child("status").value == false && !data.key.equals(Profile.getCurrentProfile().id) && count == 0){
                            count = 1
                            view.loadData(data, false)

                            val values: HashMap<String, Any> = hashMapOf(
                                "name" to name,
                                "facebookId" to Profile.getCurrentProfile().id,
                                "status" to true
                            )
                            database.child("waitingList").child(data.key!!).setValue(values).addOnFailureListener {
                                it.message?.let { it1 -> view.response(it1) }
                            }
                            return
                        }
                        else if (data.key.equals(Profile.getCurrentProfile().id)){ // ulang bikin
                            registerToWaitingList(name)
                            return
                        }

                    }
                    registerToWaitingList(name)
                }
                else
                    registerToWaitingList(name)

                database.child("waitingList").removeEventListener(this)
            }

        }
        database.child("waitingList").addListenerForSingleValueEvent(postListener)

    }

    fun registerToWaitingList(name: String){
        val values: HashMap<String, Any> = hashMapOf(
            "name" to name,
            "facebookId" to Profile.getCurrentProfile().id,
            "status" to false
        )
        database.child("waitingList").child(Profile.getCurrentProfile().id).setValue(values).addOnSuccessListener {
            view.response("registerToWaitingList")
            getResponseOnline()
        }.addOnFailureListener {
            it.message?.let { it1 -> view.response(it1) }
        }
    }

    private fun getResponseOnline(){
        var count = 0
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                view.response(""+p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    if (p0.getValue(OpponentOnline::class.java)!!.status == true && count == 0){
                        count = 1
                        view.loadData(p0, true)
                        createGame(Profile.getCurrentProfile().id,true)
                        removeWaitingList()

                        database.child("waitingList").child(Profile.getCurrentProfile().id).removeEventListener(this)
                    }

                }
            }

        }
        database.child("waitingList").child(Profile.getCurrentProfile().id).addValueEventListener(postListener)
    }

    fun makeInvitation(facebookId : String, gameType: GameType, timer: Int,name: String){
//        val type  = when(gameType){
//            GameType.Normal -> "normal"
//            GameType.OddEven -> "oddEven"
//            GameType.Rush -> "rush"
//            GameType.AlphaNum -> "alpaNum"
//            GameType.DoubleAttack -> "doubleAttack"
//            GameType.Mix -> "mix"
//        }
        val values: HashMap<String, Any?> = hashMapOf(
            "name" to name,
            "facebookId" to Profile.getCurrentProfile().id,
            "status" to false,
            "type" to gameType,
            "timer" to timer
        )

        database.child("invitation").child(facebookId).setValue(values).addOnSuccessListener {
            view.response("invitationSent")


        }.addOnFailureListener {
            it.message?.let { it1 -> view.response(it1) }
        }
    }

    fun getResponse(facebookId : String){
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                view.response("server error")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    Log.i("cek1",""+p0)
                    if(p0.getValue(Status::class.java)!!.status!!){
                        view.response("accepted")
                        database.removeEventListener(this)
                    }

                }
                else{
                    view.response("rejected")
                    database.removeEventListener(this)
                }
            }

        }
        database.child("invitation").child(facebookId).addValueEventListener(postListener)
    }

    fun createGame(facebookId: String, playOnline: Boolean){

        val values: HashMap<String, Any> = hashMapOf(
            "player1" to 0,
            "player2" to 0
        )
        database.child("onPlay").child(facebookId).setValue(values).addOnSuccessListener {
            if (!playOnline)
            view.response("createGame")
        }.addOnFailureListener {
            it.message?.let { it1 -> view.response(it1) }
        }
    }

    fun removeInvitation(facebookId: String){
        database.child("invitation").child(facebookId).removeValue()
    }

    fun removeWaitingList(){
        database.child("waitingList").child(Profile.getCurrentProfile().id).removeValue()
    }

    fun dismissListenerOnline(){
        database.child("waitingList").child(Profile.getCurrentProfile().id).removeEventListener(postListener)
    }

    fun loadTips(){
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                view.response(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    val count = p0.childrenCount - 1
                    val rand = Random().nextInt(count.toInt()) + 1

                    view.loadTips(p0.child(rand.toString()).value as String)
                }

            }

        }
        database.child("tips").addListenerForSingleValueEvent(postListener)
    }
}