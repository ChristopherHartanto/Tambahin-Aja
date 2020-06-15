package com.ta.tambahinaja.presenter

import android.util.Log
import com.ta.tambahinaja.rank.Balance
import com.ta.tambahinaja.rank.Rank
import com.ta.tambahinaja.view.RankView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ta.tambahinaja.view.MainView

class PracticePresenter(private val view: MainView, private val database: DatabaseReference) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun fetchProfile(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if (dataSnapshot.exists()){
                    view.loadData(dataSnapshot,"fetchProfile")
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        database.child("users").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(postListener)
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

    fun fetchLevel(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                view.loadData(dataSnapshot,"fetchLevel")
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        database.child("users").child(auth.currentUser!!.uid).child("currentLevel").addListenerForSingleValueEvent(postListener)
    }

    fun updateCoin(coin: Int){
        database.child("users").child(auth.currentUser!!.uid).child("balance").child("point")
                .setValue(coin).addOnSuccessListener {
                    view.response("updateCoin")
                }
    }


    fun dismissListener(){
        database.removeEventListener(postListener)
    }
}
