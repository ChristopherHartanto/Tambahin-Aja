package com.ta.tambahinaja.presenter

import com.ta.tambahinaja.view.MainView
import com.ta.tambahinaja.model.Inviter
import com.ta.tambahinaja.rank.Balance
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Presenter(private val view: MainView, private val database: DatabaseReference) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun fetchTournament(){
        GlobalScope.launch {
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    view.loadData(p0,"fetchTournament")
                }

            }
            database.child("tournament").addListenerForSingleValueEvent(postListener)
        }
    }





    fun dismissListener(){
        database.removeEventListener(postListener)
    }
}
