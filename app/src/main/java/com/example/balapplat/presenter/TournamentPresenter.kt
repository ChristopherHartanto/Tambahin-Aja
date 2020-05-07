package com.example.balapplat.presenter

import com.example.balapplat.leaderboard.Leaderboard
import com.example.balapplat.model.HighScore
import com.example.balapplat.model.User
import com.example.balapplat.rank.Rank
import com.example.balapplat.view.MainView
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.HashMap

class TournamentPresenter(private val view: MainView, private val database: DatabaseReference) {
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

    fun fetchTournamentParticipants(){

        GlobalScope.launch {

            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    view.loadData(p0,"fetchTournamentParticipants")
                }

            }
            database.child("tournament").child("participants").orderByChild("point").addValueEventListener(postListener)

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
            database.child("users").child(auth.currentUser!!.uid).child("stats").child("tournamentJoined").addValueEventListener(postListener)

        }
    }

    fun dismissListener(){
        database.removeEventListener(postListener)
    }

}