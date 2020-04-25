package com.example.balapplat

import android.provider.Contacts
import com.example.balapplat.model.Inviter
import com.example.balapplat.play.CountdownActivity
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.coroutines.experimental.bg

class Presenter(private val view: MainView, private val database: DatabaseReference) {

    companion object {
        var idlingResourceCounter = 1
    }

    fun receiveInvitation(){
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    if (p0.getValue(Inviter::class.java)!!.status == false)
                        view.loadData(p0)
                }
            }

        }
        database.child("invitation").child(Profile.getCurrentProfile().id).addValueEventListener(postListener)
    }

    fun replyInvitation(status: Boolean){
        if (status)
            database.child("invitation").child(Profile.getCurrentProfile().id).child("status").setValue(true).addOnSuccessListener {
                view.response("acceptedGame")
            }
        else
            database.child("invitation").child(Profile.getCurrentProfile().id).removeValue().addOnSuccessListener {
                view.response("rejectedGame")
            }
    }

    fun updateStats(winStatus: Boolean, value: Long, auth: FirebaseAuth) {
        if (winStatus) {
            database.child("stats").child(auth.currentUser!!.uid).child("win").setValue(value + 1)
        } else {
            database.child("stats").child(auth.currentUser!!.uid).child("lose").setValue(value + 1)
        }
    }
}
