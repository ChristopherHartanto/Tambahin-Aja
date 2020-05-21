package com.ta.tambahinaja.presenter

import com.ta.tambahinaja.model.Inviter
import com.ta.tambahinaja.view.MainView
import com.ta.tambahinaja.rank.Balance
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class HomePresenter(private val view: MainView, private val database: DatabaseReference) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun receiveInvitation(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    if (p0.getValue(Inviter::class.java)!!.status == false)
                        view.loadData(p0,"receiveInvitation")
                }
            }

        }
        database.child("invitation").child(Profile.getCurrentProfile().id).addListenerForSingleValueEvent(postListener)
    }

    fun replyInvitation(status: Boolean){
        if (status){
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        if (status)
                            database.child("invitation").child(Profile.getCurrentProfile().id).child("status").setValue(true).addOnSuccessListener {
                                view.response("acceptedGame")
                            }
                    }else{
                        view.response("dismissInvitation")
                    }
                }

            }
            database.child("invitation").child(Profile.getCurrentProfile().id).addListenerForSingleValueEvent(postListener)
        }else
            database.child("invitation").child(Profile.getCurrentProfile().id).removeValue().addOnSuccessListener {
                view.response("rejectedGame")
            }

    }
    fun receiveReward(){
        var count = 0
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    if (count == 0){
                        count = 1
                        view.loadData(p0,"reward")
                        database.removeEventListener(this)
                    }
                }
            }

        }
        database.child("users").child(auth.currentUser!!.uid).child("reward").limitToFirst(1).addValueEventListener(postListener)

    }

    fun removePopUpReward(){
        database.child("users").child(auth.currentUser!!.uid).child("reward").removeValue()
    }


    fun checkDailyPuzzle(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    view.loadData(p0,"dailyPuzzle")
                }
            }

        }
        database.child("users").child(auth.currentUser!!.uid).child("dailyPuzzle").addListenerForSingleValueEvent(postListener)

    }

    fun fetchCredit(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    view.loadData(p0,"fetchCredit")
                }
            }

        }
        database.child("users").child(auth.currentUser!!.uid).child("balance").child("credit").addListenerForSingleValueEvent(postListener)

    }

    fun fetchAvailableGame(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0,"availableGame")
                database.removeEventListener(this)
            }

        }
        database.child("users").child(auth.currentUser!!.uid).child("availableGame").addListenerForSingleValueEvent(postListener)
    }

    fun rewardPuzzlePopUp(){
        val values  = hashMapOf(
                "description" to "You Got 20 Credit",
                "type" to "Credit",
                "quantity" to 20
        )

        database.child("users").child(auth.currentUser!!.uid).child("reward").setValue(values).addOnSuccessListener {
            view.response("rewardPuzzlePopUp")
        }
    }

    fun updateCredit(credit: Long){
        database.child("users").child(auth.currentUser!!.uid).child("balance").child("credit").setValue(credit).addOnSuccessListener {
            view.response("updateCredit")
        }
    }


    fun updatePuzzle(){
        val sdf = SimpleDateFormat("dd MMM yyyy")
        val currentDate = sdf.format(Date())

        database.child("users").child(auth.currentUser!!.uid).child("dailyPuzzle").setValue(currentDate)

    }

    fun dismissListener(){
        database.removeEventListener(postListener)
    }
}
