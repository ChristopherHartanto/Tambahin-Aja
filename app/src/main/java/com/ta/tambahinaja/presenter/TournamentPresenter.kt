package com.ta.tambahinaja.presenter

import com.ta.tambahinaja.model.User
import com.ta.tambahinaja.rank.Rank
import com.ta.tambahinaja.view.MainView
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class TournamentPresenter(private val view: MainView, private val database: DatabaseReference) {
    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun checkPoint(auth: FirebaseAuth,price: Long){
        postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) { //get date first
                if (dataSnapshot.exists()){
                    if (dataSnapshot.value.toString().toLong() < price)
                        view.response("notEnoughPoint")
                    else
                        view.response("continueJoinTournament")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                view.response(databaseError.message)
            }
        }
        database.child("users").child(auth.currentUser!!.uid).child("balance").child("point").addListenerForSingleValueEvent(postListener)
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

    fun fetchTournamentParticipants(date: String){

        GlobalScope.launch {

            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    view.loadData(p0,"fetchTournamentParticipants")
                }

            }
            database.child("tournament").child(date).child("participants").orderByChild("point").addValueEventListener(postListener)

        }
    }

    fun joinTournament(auth: FirebaseAuth, tournamentEndDate: String){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val data = p0.getValue(User::class.java)
                    val values: HashMap<String, Any>

                    if (data != null) {
                        values  = hashMapOf(
                                "name" to data.name.toString(),
                                "facebookId" to Profile.getCurrentProfile().id,
                                "point" to 0

                        )
                        updateTournamentStats(auth)
                        database.child("tournament").child(tournamentEndDate).child("participants").child(auth.currentUser!!.uid).setValue(values).addOnSuccessListener {
                            view.response("joinTournament")
                        }
                    }

                }

            }

        }
        database.child("users").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(postListener)
    }

    fun updatePoint(auth: FirebaseAuth,price: Long){
        postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) { //get date first
                if (dataSnapshot.exists()){
                    database.child("users").child(auth.currentUser!!.uid).child("balance").child("point")
                            .setValue(dataSnapshot.value.toString().toLong() - price).addOnFailureListener {
                        view.response(it.message.toString())
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                view.response(databaseError.message)
            }
        }
        database.child("users").child(auth.currentUser!!.uid).child("balance").child("point").addListenerForSingleValueEvent(postListener)

    }

    fun updateTournamentStats(auth: FirebaseAuth){

        GlobalScope.launch {

            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        database.child("users").child(auth.currentUser!!.uid)
                                .child("stats").child("tournamentJoined")
                                .setValue(p0.value.toString().toInt() + 1)
                    }
                }

            }
            database.child("users").child(auth.currentUser!!.uid).child("stats").child("tournamentJoined").addListenerForSingleValueEvent(postListener)

        }
    }

    fun dismissListener(){
        database.removeEventListener(postListener)
    }

}