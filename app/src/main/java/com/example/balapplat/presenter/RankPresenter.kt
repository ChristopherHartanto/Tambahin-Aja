package com.example.balapplat.presenter

import com.example.balapplat.view.MainView
import com.example.balapplat.model.Inviter
import com.example.balapplat.rank.Balance
import com.example.balapplat.view.RankView
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RankPresenter(private val view: RankView, private val database: DatabaseReference) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun fetchBalance(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if (dataSnapshot.exists()){
                    view.loadData(dataSnapshot,"fetchBalance")
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        database.child("users").child(auth.currentUser!!.uid).child("balance").addListenerForSingleValueEvent(postListener)
    }

    fun fetchGameAvailable(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if (dataSnapshot.exists()){
                    view.loadData(dataSnapshot,"fetchGameAvailable")
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        database.child("users").child(auth.currentUser!!.uid).child("availableGame").addListenerForSingleValueEvent(postListener)
    }

    fun buyGame(position: Int, pointRemaining: Long){
        var gameName = ""
        when(position){
            1 -> gameName = "oddEven"
            2 -> gameName = "rush"
            3 -> gameName = "alphaNum"
            4 -> gameName = "mix"
            5 -> gameName = "doubleAttack"
        }
        database.child("users").child(auth.currentUser!!.uid).child("availableGame").child(gameName).setValue(true).addOnFailureListener {
            view.response(it.message!!,"error")
        }
        database.child("users").child(auth.currentUser!!.uid).child("balance").child("point").setValue(pointRemaining).addOnFailureListener {
            view.response(it.message!!,"error")
        }.addOnSuccessListener {
            view.response("Success Buy this Game","buyGame")
            fetchBalance()
            fetchGameAvailable()
        }
    }

    fun fetchScore(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                view.loadData(dataSnapshot,"fetchScore")
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        database.child("leaderboards").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(postListener)
    }

    fun fetchRank(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if (dataSnapshot.exists())
                    view.loadData(dataSnapshot,"fetchRank")
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        database.child("users").child(auth.currentUser!!.uid).child("currentRank").addListenerForSingleValueEvent(postListener)

    }

    fun updateEnergy(energyRemaining: Long){
        database.child("users").child(auth.currentUser!!.uid).child("balance").child("energy").setValue(energyRemaining).addOnFailureListener {
            view.response(it.message!!,"error")
        }.addOnSuccessListener {
            view.response("","updateEnergy")
            fetchBalance()
        }
    }

    fun dismissListener(){
        database.removeEventListener(postListener)
    }
}