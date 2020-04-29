package com.example.balapplat.friends

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.balapplat.R
import com.example.balapplat.utils.UtilsConstants
import com.example.balapplat.utils.showSnackBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.activity_friends.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class FriendsActivity : AppCompatActivity(), NetworkConnectivityListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        checkFriends(savedInstanceState)

        cvAddFriends.onClick {
            startActivity<AddFriendsActivity>()
        }
    }

    private fun loadNoFriendFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_friend, NoFriendFragment(), NoFriendFragment::class.java.simpleName)
                .commit()
        }
    }

    private fun loadListFriendsFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_friend, ListFriendsFragment(), ListFriendsFragment::class.java.simpleName)
                .commit()
        }
    }

    fun checkFriends(savedInstanceState: Bundle?){
        var highScore = 0
        GlobalScope.async {
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    if(dataSnapshot.exists())
                        loadListFriendsFragment(savedInstanceState)
                    else
                        loadNoFriendFragment(savedInstanceState)
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            }

            database.child("friends").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(postListener)

        }

    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(
                        activity_friends,
                        "Connection Established",
                        UtilsConstants.SNACKBAR_LONG
                    ).show()
                } else {
                    showSnackBar(
                        activity_friends,
                        "No Network !",
                        UtilsConstants.SNACKBAR_INFINITE
                    ).show()
                }
            }
        }
    }
}
