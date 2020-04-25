package com.example.balapplat

import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import java.util.HashMap

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    lateinit var helper : Helper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
//Christopher Hartanto
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        helper = Helper()

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

        helper.userActive(false)
        super.onDestroy()
    }

    override fun onStart() {
        helper.userActive(true)
        super.onStart()
    }

    fun receiveInvitation(){
        val postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val data = p0.getValue(Inviter::class.java)

                    alert(data!!.name + " invite you to play"){
                        title = "Invitation"
                        yesButton {
                            database.child("invitation").child(Profile.getCurrentProfile().id).child("status").setValue(true).addOnSuccessListener {
                                toast("accepted game")

                                startActivity(intentFor<CountdownActivity>("inviterFacebookId" to data.facebookId,
                                        "inviterName" to data.name))

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

data class Inviter(
    var facebookId: String? = "",
    var name: String? = "",
    var status: Boolean? = false
)