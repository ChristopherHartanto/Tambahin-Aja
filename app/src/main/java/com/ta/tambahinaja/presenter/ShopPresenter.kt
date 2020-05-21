package com.ta.tambahinaja.presenter

import android.util.Log
import com.ta.tambahinaja.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class ShopPresenter(private val view: MainView, private val database: DatabaseReference) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun fetchBalance(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if (dataSnapshot.exists()){
                    database.removeEventListener(this)
                    view.loadData(dataSnapshot,"fetchBalance")
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        if (auth.currentUser != null)
        database.child("users").child(auth.currentUser!!.uid).child("balance").addListenerForSingleValueEvent(postListener)
    }

    fun updateEnergy(energy: Long){
        Log.d("energy from shop", energy.toString())
        database.child("users").child(auth.currentUser!!.uid).child("balance")
                .child("energy").setValue(energy).addOnSuccessListener {
                    view.response("updateEnergy")
                }
    }

    fun updateEnergyLimit(energyLimit: Long){
        database.child("users").child(auth.currentUser!!.uid).child("balance")
                .child("energyLimit").setValue(energyLimit).addOnSuccessListener {
                    view.response("updateEnergyLimit")
                }
    }

    fun updatePoint(point: Long){
        database.child("users").child(auth.currentUser!!.uid).child("balance")
                .child("point").setValue(point).addOnSuccessListener {
                    view.response("updatePoint")
                }
    }
}