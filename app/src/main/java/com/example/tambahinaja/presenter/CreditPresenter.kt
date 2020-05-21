package com.example.tambahinaja.presenter

import com.example.tambahinaja.home.CreditShop
import com.example.tambahinaja.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class CreditPresenter(private val view: MainView, private val database: DatabaseReference) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun exchangeCredit(credit: Int,name: String){
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss")
        val currentDate = sdf.format(Date())

        val values: HashMap<String, Any>
        values  = hashMapOf(
                "credit" to credit,
                "date" to currentDate
        )
        database.child("exchange").child(auth.currentUser!!.uid).setValue(values).addOnSuccessListener {
            view.response("exchangeCredit")
        }
    }

    fun updateQuantity(key: Int){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val quantity = p0.value.toString().toInt() - 1
                    database.child("credit").child(key.toString()).child("quantity").setValue(quantity)
                }
            }

        }
        database.child("credit").child(key.toString()).child("quantity").addListenerForSingleValueEvent(postListener)
    }

    fun updateCredit(credit: Long){
        database.child("users").child(auth.currentUser!!.uid).child("balance").child("credit").setValue(credit)
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

    fun fetchUser(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    view.loadData(p0,"fetchUser")
                }
                database.removeEventListener(this)
            }

        }
        database.child("users").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(postListener)
    }

    fun dismissListener(){
        database.removeEventListener(postListener)
    }
}