package com.example.balapplat

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.balapplat.utils.UtilsConstants
import com.example.balapplat.utils.showSnackBar
import com.facebook.AccessToken
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var prevState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        savedInstanceState?.let {
            prevState = it.getBoolean(NetworkConstants.LOST_CONNECTION)
        }

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) handleConnectivityChange(it.state)
        })

        if(AccessToken.getCurrentAccessToken() != null)
            receiveInvitation()

        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    loadHomeFragment(savedInstanceState)
                }
                R.id.tournament -> {
                    loadTournamentFragment(savedInstanceState)
                }
                R.id.profile -> {
                    loadProfileFragment(savedInstanceState)
                }

            }
            true
        }
        bottom_navigation.selectedItemId = R.id.home
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.putBoolean(NetworkConstants.LOST_CONNECTION, prevState)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onResume() {
        super.onResume()
        handleConnectivityChange(NetworkStateHolder)
    }

    private fun handleConnectivityChange(networkState: NetworkState) {
        if (networkState.isConnected && !prevState) {
            showSnackBar(
                activity_main,
                "Connection established",
                UtilsConstants.SNACKBAR_LONG
            ).show()
        }
        if (!networkState.isConnected && prevState) {
            showSnackBar(
                activity_main,
                "No Network !",
                UtilsConstants.SNACKBAR_INFINITE
            ).show()
        }

        prevState = networkState.isConnected
    }

    private fun loadHomeFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_container, HomeFragment(), HomeFragment::class.java.simpleName)
                .commit()
        }
    }

    private fun loadTournamentFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_container, Tournament(), Tournament::class.java.simpleName)
                .commit()
        }
    }

    private fun loadProfileFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_container, ProfileFragment(), ProfileFragment::class.java.simpleName)
                .commit()
        }
    }

    override fun onDestroy() {
        Log.d("destroy","masuk")
//        val values: HashMap<String, Any> = hashMapOf(
//            "active" to 0
//        )
//
//        database.child("users").child(auth.currentUser!!.uid).setValue(values).addOnSuccessListener {
//            toast("active 0")
//
//        }.addOnFailureListener {
//            toast(""+ it.message)
//        }
        super.onDestroy()
    }

    fun receiveInvitation(){
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    alert{
                        title = "Invitation"
                        yesButton {
                            val values: HashMap<String, Any> = hashMapOf(
                                "status" to true
                            )
                            database.child("invitation").child(Profile.getCurrentProfile().id).setValue(values).addOnSuccessListener {
                                toast("accepted game")

                                startActivity(intentFor<CountdownActivity>("status" to "player2"))

                            }.addOnFailureListener {
                                toast(""+ it.message)
                            }
                        }
                        noButton {
                            database.child("invitation").child(Profile.getCurrentProfile().id).removeValue()
                        }
                    }.show()
                }
            }

        }
        database.child("invitation").child(Profile.getCurrentProfile().id).addValueEventListener(postListener)
    }
}
