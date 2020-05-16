package com.example.tambahinaja.presenter

import android.annotation.SuppressLint
import com.example.tambahinaja.play.GameType
import com.example.tambahinaja.view.MatchView
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class MatchPresenter (private val view: MatchView, private val database: DatabaseReference){

    var postListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    fun getHighScore(auth: FirebaseAuth, type: GameType){
        val gameType  = when(type){
            GameType.Normal -> "normal"
            GameType.OddEven -> "oddEven"
            GameType.Rush -> "rush"
            GameType.AlphaNum -> "alpaNum"
            GameType.DoubleAttack -> "doubleAttack"
            GameType.Mix -> "mix"
        }
        postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if (dataSnapshot.exists())
                    view.loadHighScore(dataSnapshot.value as Long)
                else
                    view.loadHighScore(0)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                view.response(databaseError.message)
            }
        }
        database.child("leaderboards").child(auth.currentUser!!.uid).child(gameType).addListenerForSingleValueEvent(postListener)
    }

    fun sumHighScore(auth: FirebaseAuth, type: GameType, highScore: Int, oldScore: Int){
        val gameType  = when(type){
            GameType.Normal -> "normal"
            GameType.OddEven -> "oddEven"
            GameType.Rush -> "rush"
            GameType.AlphaNum -> "alpaNum"
            GameType.DoubleAttack -> "doubleAttack"
            GameType.Mix -> "mix"
        }
        postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if (dataSnapshot.exists())
                    newHighScore(auth,dataSnapshot.value as Long - oldScore.toLong(),gameType,highScore)
                else
                    newHighScore(auth,0,gameType,highScore)

            }

            override fun onCancelled(databaseError: DatabaseError) {
                view.response(databaseError.message)
            }
        }
        database.child("leaderboards").child(auth.currentUser!!.uid).child("total").addListenerForSingleValueEvent(postListener)
    }

    fun newHighScore(auth: FirebaseAuth,total: Long, type: String, highScore: Int){
        database.child("leaderboards").child(auth.currentUser!!.uid).child("total").setValue(total+highScore).addOnFailureListener {
            view.response(it.message!!)
        }
        database.child("leaderboards").child(auth.currentUser!!.uid).child(type).setValue(highScore).addOnFailureListener {
            view.response(it.message!!)
        }
    }

    fun updateValue(inviter: Boolean,playerPoint: Int, opponentPoint: Int,facebookId: String){
        val values: HashMap<String, Any>

        if (!inviter){
            values  = hashMapOf(
                "player1" to opponentPoint,
                "player2" to playerPoint
            )
            database.child("onPlay").child(Profile.getCurrentProfile().id).setValue(values).addOnSuccessListener {

            }.addOnFailureListener {
            }
        }
        else{
            values  = hashMapOf(
                "player1" to playerPoint,
                "player2" to opponentPoint
            )

            database.child("onPlay").child(facebookId).setValue(values).addOnSuccessListener {

            }.addOnFailureListener {

            }
        }

    }

    fun getStats(auth: FirebaseAuth,winStatus : Boolean){
        var temp = 0
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                view.response(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (temp == 0){
                    if(p0.exists())
                        updateStats(winStatus, p0.value as Long,auth)
                    else
                        updateStats(winStatus, 0,auth)
                    temp = 1
                }


            }

        }
        if (winStatus)
            database.child("users").child(auth.currentUser!!.uid).child("stats").child("win").addListenerForSingleValueEvent(postListener)
        else
            database.child("users").child(auth.currentUser!!.uid).child("stats").child("lose").addListenerForSingleValueEvent(postListener)

    }

    fun updateStats(winStatus: Boolean, value: Long, auth: FirebaseAuth) {
        if (winStatus) {
            database.child(auth.currentUser!!.uid).child("stats").child("win").setValue(value + 1)
        } else {
            database.child(auth.currentUser!!.uid).child("stats").child("lose").setValue(value + 1)
        }
    }

    fun updatePoint(point: Long,auth: FirebaseAuth){

        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                view.response(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    val totalPoint = p0.value.toString().toInt() + point

                    database.child("users").child(auth.currentUser!!.uid).child("balance").child("point").setValue(totalPoint)

                }
            }
        }
        database.child("users").child(auth.currentUser!!.uid).child("balance").child("point").addListenerForSingleValueEvent(postListener)

    }

    fun getTournamentType(){
        postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) { //get date first
                if (dataSnapshot.exists()){
                    for ((index,data) in dataSnapshot.children.withIndex()){
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        val currentDate = Date().time
                        val tournamentDate = sdf.parse(data.key.toString()).time
                        val diff: Long = tournamentDate - currentDate

                        if(diff > 0){
                            view.loadData(dataSnapshot,"getTournamentEndDate")
                            return
                        }
                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                view.response(databaseError.message)
            }
        }
        database.child("tournament").addListenerForSingleValueEvent(postListener)
    }

    fun loadTournamentType(tournamentEndDate: String){
        postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) { //get date first
                if (dataSnapshot.exists()){
                    view.loadData(dataSnapshot,"fetchTournamentType")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                view.response(databaseError.message)
            }
        }
        database.child("tournament").child(tournamentEndDate).addListenerForSingleValueEvent(postListener)
    }


    fun updateTournament(auth: FirebaseAuth, point: Long){
        postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) { //get date first
                if (dataSnapshot.exists()){
                    for ((index,data) in dataSnapshot.children.withIndex()){
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        val currentDate = Date().time
                        val tournamentDate = sdf.parse(data.key.toString()).time
                        val diff: Long = tournamentDate - currentDate

                        if(diff > 0){
                            updateTournamentPoint(auth,data.key.toString(), point)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                view.response(databaseError.message)
            }
        }
        database.child("tournament").addListenerForSingleValueEvent(postListener)

    }

    fun updateTournamentPoint(auth: FirebaseAuth,tournamentEndDate: String, point: Long){
        postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    database.child("tournament").child(tournamentEndDate).child("participants")
                            .child(auth.currentUser!!.uid).child("point").setValue(dataSnapshot.value.toString().toLong() + point)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                view.response(databaseError.message)
            }
        }
        database.child("tournament").child(tournamentEndDate).child("participants")
                .child(auth.currentUser!!.uid).child("point").addListenerForSingleValueEvent(postListener)
    }

    fun updateCredit(credit:Long,auth: FirebaseAuth){
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss")
        val currentDate = sdf.format(Date())


        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                view.response(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    val values: HashMap<String, Any>
                    val totalCredit = p0.value.toString().toInt() + credit
                    values  = hashMapOf(
                            "info" to "-",
                            "credit" to credit
                    )

                    database.child("users").child(auth.currentUser!!.uid)
                            .child("balance").child("credit").setValue(totalCredit)
                    database.child("users").child(auth.currentUser!!.uid).child("creditHistory")
                            .child(currentDate).setValue(values)
                }
            }
        }
        database.child("users").child(auth.currentUser!!.uid).child("balance").child("credit").addListenerForSingleValueEvent(postListener)

    }

    @SuppressLint("SimpleDateFormat")
    fun addToHistory(auth: FirebaseAuth, playerPoint: Int, opponentPoint: Int, opponentFacebookId: String, opponentName: String, type: String){
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss")
        val currentDate = sdf.format(Date())

        val status = when {
            playerPoint > opponentPoint -> {
                "win"
            }
            playerPoint < opponentPoint -> {
                "lose"
            }
            else -> "draw"
        }

        val values  = hashMapOf(
            "status" to status,
            "point" to playerPoint,
            "opponentFacebookId" to opponentFacebookId,
            "opponentName" to opponentName,
            "opponentPoint" to opponentPoint,
            "type" to type
        )
        database.child("users").child(auth.currentUser!!.uid).child("history").child("" +currentDate).setValue(values)
    }

    fun removeOnPlay(){
        database.child("onPlay").child(Profile.getCurrentProfile().id).removeValue()
    }

    fun fetchOpponent(facebookId: String){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                view.response(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    view.fetchOpponentData(p0,true) // parameter kedua ga perlu
                }
            }

        }
        database.child("onPlay").child(facebookId).addValueEventListener(postListener)

    }

    fun dismissListener(){
        database.removeEventListener(postListener)
    }
}