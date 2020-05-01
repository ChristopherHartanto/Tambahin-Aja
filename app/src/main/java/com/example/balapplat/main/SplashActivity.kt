package com.example.balapplat.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import com.example.balapplat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.jetbrains.anko.intentFor

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        if (auth.currentUser != null){
            val database = FirebaseDatabase.getInstance()
            val myConnectionsRef = database.reference.child("users").child(auth.currentUser!!.uid).child("online")

// Stores the timestamp of my last disconnect (the last time I was seen online)
            val lastOnlineRef = database.reference.child(auth.currentUser!!.uid).child("lastOnline")

            val connectedRef = database.getReference(".info/connected")
            connectedRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected = snapshot.getValue(Boolean::class.java) ?: false
                    if (connected) {
                        val con = myConnectionsRef.push()

                        // When this device disconnects, remove it
                        con.onDisconnect().removeValue()

                        // When I disconnect, update the last time I was seen online
                        lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP)

                        // Add this device to my connections list
                        // this value could contain info about the device or a timestamp too
                        con.setValue(java.lang.Boolean.TRUE)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("cek", "Listener was cancelled at .info/connected")
                }
            })

        }



        val timer = object: CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                finish()
                startActivity(intentFor<MainActivity>())
                overridePendingTransition(
                    R.anim.fade_in,
                    R.anim.fade_out
                )
            }
        }
        timer.start()
    }
}
