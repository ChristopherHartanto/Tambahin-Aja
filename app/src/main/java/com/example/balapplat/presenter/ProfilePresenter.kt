package com.example.balapplat.presenter

import com.example.balapplat.view.MainView
import com.example.balapplat.model.Inviter
import com.example.balapplat.rank.Balance
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfilePresenter(private val view: MainView, private val database: DatabaseReference) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun fetchName(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    view.loadData(p0,"fetchName")
                }
                database.removeEventListener(this)
            }

        }
        database.child("users").child(auth.currentUser!!.uid).child("name").addListenerForSingleValueEvent(postListener)

    }
    fun fetchHistory(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    view.loadData(p0,"fetchHistory")
                }
                database.removeEventListener(this)
            }

        }
        database.child("users").child(auth.currentUser!!.uid).child("history").orderByKey().addListenerForSingleValueEvent(postListener)

    }

    fun saveProfile(name: String, email: String, noHandphone: String){
        val values  = hashMapOf(
                "name" to name,
                "email" to email,
                "noHandphone" to noHandphone
        )
        database.child("users").child(auth.currentUser!!.uid).setValue(values).addOnSuccessListener {
            view.response("saveProfile")
        }.addOnFailureListener {
            view.response(it.message.toString())
        }

    }

    fun dismissListener(){
        database.removeEventListener(postListener)
    }
}
