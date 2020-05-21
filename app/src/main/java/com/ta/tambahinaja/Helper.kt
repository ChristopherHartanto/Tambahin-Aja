package com.ta.tambahinaja

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Helper {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun userActive(status : Boolean) {
        if (auth.currentUser != null){
            database.child("users").child(auth.currentUser!!.uid).child("active").setValue(status)
        }
    }

}