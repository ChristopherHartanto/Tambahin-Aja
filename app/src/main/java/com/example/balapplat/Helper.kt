package com.example.balapplat

import com.example.balapplat.model.NormalMatch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.jetbrains.anko.toast

class Helper {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun userActive(status : Boolean) {
        if (auth.currentUser != null){
            val active = if (status) 1 else 0

            database.child("users").child(auth.currentUser!!.uid).child("active").setValue(active)
        }
    }

}