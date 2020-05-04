package com.example.balapplat.presenter

import com.example.balapplat.view.MainView
import com.example.balapplat.model.Inviter
import com.example.balapplat.rank.Balance
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Presenter(private val view: MainView, private val database: DatabaseReference) {

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
                        view.loadData(p0)

                    database.removeEventListener(this)
                }
            }

        }
        database.child("invitation").child(Profile.getCurrentProfile().id).addValueEventListener(postListener)
    }

    fun replyInvitation(status: Boolean){
        if (status)
            database.child("invitation").child(Profile.getCurrentProfile().id).child("status").setValue(true).addOnSuccessListener {
                view.response("acceptedGame")
            }
        else
            database.child("invitation").child(Profile.getCurrentProfile().id).removeValue().addOnSuccessListener {
                view.response("rejectedGame")
            }
    }

    fun userActive(status : Boolean) {

        if (auth.currentUser != null){
            database.child("users").child(auth.currentUser!!.uid).child("active").setValue(status)
        }
    }

    fun fetchCredit(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                        view.loadData(p0)
                }
                database.removeEventListener(this)
            }

        }
        database.child("users").child(auth.currentUser!!.uid).child("balance").addListenerForSingleValueEvent(postListener)

    }

    fun dismissListener(){
        database.removeEventListener(postListener)
    }
}
