package com.example.balapplat.friends

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.balapplat.R
import com.example.balapplat.model.NormalMatch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_friends.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class FriendsActivity : AppCompatActivity() {

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
}
