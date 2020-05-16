package com.example.tambahinaja.presenter

import com.example.tambahinaja.view.MainView
import com.example.tambahinaja.rank.Balance
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
            }

        }
        database.child("users").child(auth.currentUser!!.uid).child("history").orderByKey().addListenerForSingleValueEvent(postListener)

    }

    fun fetchStats(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    view.loadData(p0,"fetchStats")
                }
            }

        }
        database.child("users").child(auth.currentUser!!.uid).child("stats").addListenerForSingleValueEvent(postListener)

    }

    fun saveProfile(name: String, email: String, noHandphone: String){
        database.child("users").child(auth.currentUser!!.uid).child("name").setValue(name).addOnFailureListener {
            view.response(it.message.toString())
        }
        database.child("users").child(auth.currentUser!!.uid).child("email").setValue(email).addOnFailureListener {
            view.response(it.message.toString())
        }
        database.child("users").child(auth.currentUser!!.uid).child("noHandphone").setValue(noHandphone).addOnFailureListener {
            view.response(it.message.toString())
        }

    }

    fun fetchUserProfile(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if (dataSnapshot.exists()){
                    view.loadData(dataSnapshot,"fetchUserProfile")
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        database.child("users").child(auth.currentUser!!.uid).addValueEventListener(postListener)
    }

    fun dismissListener(){
        database.removeEventListener(postListener)
    }
}
