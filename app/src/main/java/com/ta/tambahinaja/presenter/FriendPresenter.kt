package com.ta.tambahinaja.presenter

import com.ta.tambahinaja.model.User
import com.ta.tambahinaja.view.MainView
import com.ta.tambahinaja.rank.Balance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FriendPresenter(private val view: MainView, private val database: DatabaseReference) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun retrieve(){

        GlobalScope.launch {
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    fetchDataFriends(p0)
                }

            }
            database.child("friends").child(auth.currentUser!!.uid).orderByKey().addValueEventListener(postListener)
        }
    }

    fun fetchDataFriends(dataSnapshot: DataSnapshot){
        dataSnapshot.children.sortedBy {
            it.getValue(User::class.java)!!.name
        }
        for (ds in dataSnapshot.children) {
            Thread.sleep(20)
            if (ds.key!! != auth.currentUser!!.uid){
                retrieveProfileFriends(ds.key)
            }

        }
    }

    fun retrieveProfileFriends(friendUid: String?){
        GlobalScope.launch {
            val postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists())
                        view.loadData(p0,"retrieveProfileFriends")
                }

            }
            database.child("users").child(friendUid!!).addListenerForSingleValueEvent(postListener)

        }
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

    fun dismissListener(){
        database.removeEventListener(postListener)
    }
}
