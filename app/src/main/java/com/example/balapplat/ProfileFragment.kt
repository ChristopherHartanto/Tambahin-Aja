package com.example.balapplat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.example.balapplat.model.HighScore
import com.facebook.AccessToken
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jetbrains.anko.support.v4.ctx

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    lateinit var helper : Helper


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onStart() {
        auth = FirebaseAuth.getInstance()
        helper = Helper()

        database = FirebaseDatabase.getInstance().reference

        if(AccessToken.getCurrentAccessToken() == null)
            auth.signOut()

        updateUI()
        super.onStart()
    }

        fun updateUI(){
        if (auth.currentUser != null){
            tvProfileName.text = auth.currentUser!!.displayName.toString()
            getHighScore()
            helper.userActive(true)
//            GlobalScope.launch {
//                tvHighScore.text = "" + getHighScore()
//            }
            Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivProfile)
        }else{
            tvProfileName.text = "Unknown"
        }
    }

    fun getHighScore(){
        var highScore = 0
        GlobalScope.async {
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    if(dataSnapshot.exists())
                        highScore = dataSnapshot.getValue(HighScore::class.java)?.score!!
                    tvHighScore.text = "" + highScore
                    val animationBounce = AnimationUtils.loadAnimation(ctx, R.anim.bounce)
                    tvHighScore.startAnimation(animationBounce)
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            }

            database.child("highscore").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(postListener)

        }

    }

    fun getFacebookProfilePicture(userID: String): String {
        return "https://graph.facebook.com/$userID/picture?type=large"
    }

}
