package com.ta.tambahinaja.presenter

import com.ta.tambahinaja.view.MainView
import com.ta.tambahinaja.model.Inviter
import com.ta.tambahinaja.rank.Balance
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





    fun dismissListener(){
        database.removeEventListener(postListener)
    }
}
