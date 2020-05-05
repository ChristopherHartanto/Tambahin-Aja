package com.example.balapplat.presenter

import com.example.balapplat.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class CreditPresenter(private val view: MainView, private val database: DatabaseReference) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

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
                database.removeEventListener(this)
            }

        }
        database.child("users").child(auth.currentUser!!.uid).child("balance").addListenerForSingleValueEvent(postListener)
    }

    fun fetchCreditShop(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    view.loadData(p0,"fetchCreditShop")
                }
                database.removeEventListener(this)
            }

        }
        database.child("credit").addListenerForSingleValueEvent(postListener)

    }

    fun fetchCreditHistory(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    view.loadData(p0,"fetchCreditHistory")
                }
                database.removeEventListener(this)
            }

        }
        database.child("users").child(auth.currentUser!!.uid).child("creditHistory").addListenerForSingleValueEvent(postListener)
    }

    fun dismissListener(){
        database.removeEventListener(postListener)
    }
}