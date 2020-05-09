package com.example.balapplat.presenter

import com.example.balapplat.view.MainView
import com.example.balapplat.model.Inviter
import com.example.balapplat.rank.Balance
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.jetbrains.anko.toast
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

    fun checkDailyPuzzle(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    view.loadData(p0,"dailyPuzzle")
                }
                database.removeEventListener(this)
            }

        }
        database.child("users").child(auth.currentUser!!.uid).child("dailyPuzzle").addListenerForSingleValueEvent(postListener)

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

    fun fetchCredit(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0,"fetchCredit")
                database.removeEventListener(this)
            }

        }
        database.child("users").child(auth.currentUser!!.uid).child("balance").child("credit").addListenerForSingleValueEvent(postListener)
    }

    fun rewardPuzzlePopUp(){
        val values  = hashMapOf(
                "description" to "You Got 50 Credit",
                "type" to "Credit",
                "quantity" to 50
        )

        database.child("users").child(auth.currentUser!!.uid).child("reward").setValue(values)
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
