package com.ta.tambahinaja.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ta.tambahinaja.R
import com.ta.tambahinaja.utils.getFacebookProfilePicture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_admin.*
import kotlinx.android.synthetic.main.activity_player_detail.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton

class PlayerDetailActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var uid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_detail)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        supportActionBar?.hide()
        uid = intent.extras!!.getString("uid","")

        tvPlayerUid.text = uid
        sendReward()
        fetchPlayer()
        sendCreditHistory()
    }

    fun fetchPlayer(){
        GlobalScope.launch {
            val  postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        Picasso.get().load(getFacebookProfilePicture(p0.getValue(AllPlayer::class.java)!!.facebookId.toString())).into(ivPlayer)
                        tvPlayerName.text = "Name : ${p0.getValue(AllPlayer::class.java)!!.name}"
                        tvPlayerCurrentRank.text = "Rank: ${p0.getValue(AllPlayer::class.java)!!.currentRank}"
                        tvPlayerNoHandphone.text = "no hp: ${p0.getValue(AllPlayer::class.java)!!.noHandphone}"
                        tvPlayerEmail.text = "email: ${p0.getValue(AllPlayer::class.java)!!.email}"
                    }
                }

            }
            database.child("users").child(uid).addListenerForSingleValueEvent(postListener)
        }

        GlobalScope.launch {
            val  postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        tvPlayerPoint.text = "point : ${p0.getValue(AllPlayer::class.java)!!.point}"
                        tvPlayerCredit.text = "credit: ${p0.getValue(AllPlayer::class.java)!!.credit}"
                        tvPlayerEnergyLimit.text = "energy limit : ${p0.getValue(AllPlayer::class.java)!!.energyLimit}"
                        tvPlayerEnergy.text = "energy: ${p0.getValue(AllPlayer::class.java)!!.energy}"
                    }
                }

            }
            database.child("users").child(uid).child("balance").addListenerForSingleValueEvent(postListener)
        }

    }

    fun sendCreditHistory(){
        btnSendCreditHistory.onClick {
            alert("confirm") {
                yesButton {
                    val values  = hashMapOf(
                            "credit" to etCreditHistoryCredit.text.toString().toLong(),
                            "info" to etCreditHistoryInfo.text.toString()
                    )
                    database.child("users").child(uid).child("creditHistory")
                            .child(etCreditHistoryDate.text.toString()).setValue(values)
                }
                noButton {

                }
            }.show()
        }
    }

    fun sendReward(){
        btnSendReward.onClick {
            alert ("confirm"){
                yesButton {
                    val values  = hashMapOf(
                            "description" to etPopUpDescription.text.toString(),
                            "type" to etPopUpType.text.toString(),
                            "quantity" to etPopUpQuantity.text.toString().toLong()
                    )

                    database.child("users").child(uid).child("reward").setValue(values)

                    GlobalScope.launch {
                        val  postListener = object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                if (p0.exists()){
                                    database.child("users").child(uid)
                                            .child("balance")
                                            .child("credit")
                                            .setValue(p0.getValue(AllPlayer::class.java)!!.credit!!.toInt() + etPopUpQuantity.text.toString().toLong())
                                            .addOnFailureListener {
                                                toast("Failed to add credit ${it.message}")
                                            }.addOnSuccessListener {
                                                toast("Success Send Reward")
                                            }
                                }
                            }

                        }
                        database.child("users").child(uid).child("balance").addListenerForSingleValueEvent(postListener)
                    }
                }
                noButton {

                }
            }.show()
        }

    }
}
