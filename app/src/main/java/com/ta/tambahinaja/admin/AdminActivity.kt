package com.ta.tambahinaja.admin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ta.tambahinaja.R
import com.ta.tambahinaja.profile.HistoryRecyclerViewAdapter
import com.ta.tambahinaja.tournament.Tournament
import com.facebook.all.All
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_admin.*
import kotlinx.android.synthetic.main.activity_player_detail.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.onRefresh

class AdminActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var allPlayersRecyclerViewAdapter: AllPlayersRecyclerViewAdapter
    private var playerItems: MutableList<AllPlayer> = mutableListOf()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        supportActionBar?.hide()

        btnAdminLogin.onClick {
            if (etAdminLogin.text.toString() == "tambahin")
                llAdminLogin.visibility = View.GONE
            else
                toast("Wrong")
        }

        allPlayersRecyclerViewAdapter = AllPlayersRecyclerViewAdapter(this,playerItems){
            startActivity(intentFor<PlayerDetailActivity>("uid" to it))
        }

        rvAllPlayers.layoutManager = LinearLayoutManager(this)
        rvAllPlayers.adapter = allPlayersRecyclerViewAdapter

        srAllPlayer.onRefresh {
            allPlayers()
        }

        allPlayers()
        clearData()
        clearSpecificData()
        addTournament()
        showExchangeCredit()
        viewSpecificData()
    }

    fun clearData(){
        btnClearData.onClick {
            database.child("waitingList").removeValue().addOnSuccessListener {
                toast("Success Delete Waiting List")
            }.addOnFailureListener {
                toast(it.message.toString())
            }
            database.child("onPlay").removeValue().addOnSuccessListener {
                toast("Success Delete onPlay")
            }.addOnFailureListener {
                toast(it.message.toString())
            }
        }
    }

    fun showExchangeCredit(){
        btnExchange.onClick {
            if (tvExchangeCredit.visibility == View.GONE){
                tvExchangeCredit.visibility = View.VISIBLE

                GlobalScope.launch {
                    val  postListener = object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()){
                                tvExchangeCredit.text = p0.value.toString()
                            }
                        }

                    }
                    database.child("exchange").addListenerForSingleValueEvent(postListener)
                }

            }else
                tvExchangeCredit.visibility = View.GONE
        }
    }

    fun clearSpecificData(){
        btnDeleteSpecific.onClick {
            if (etDeleteSpecificData.visibility == View.GONE)
                etDeleteSpecificData.visibility = View.VISIBLE
            else if(etDeleteSpecificData.visibility == View.VISIBLE){
                alert ("confirm"){
                    yesButton {
                        database.child(etDeleteSpecificData.text.toString()).removeValue().addOnSuccessListener {
                            toast("Success Delete ${etDeleteSpecificData.text}")
                        }.addOnFailureListener{
                            toast(it.message.toString())
                        }
                        etDeleteSpecificData.visibility = View.GONE
                    }
                    noButton {
                        etDeleteSpecificData.visibility = View.GONE
                    }
                }.show()
            }
        }
    }

    fun viewSpecificData(){
        btnViewData.onClick {
            if (etViewData.visibility == View.GONE)
                etViewData.visibility = View.VISIBLE
            else if (tvViewData.visibility == View.VISIBLE)
                tvViewData.visibility = View.GONE
            else if(etViewData.visibility == View.VISIBLE){
                alert ("confirm"){
                    yesButton {
                        GlobalScope.launch {
                            val  postListener = object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    if (p0.exists()){
                                        tvViewData.visibility = View.VISIBLE
                                        tvViewData.text = p0.value.toString()
                                    }
                                }

                            }
                            database.child(etViewData.text.toString()).addListenerForSingleValueEvent(postListener)
                        }
                        etViewData.visibility = View.GONE
                    }
                    noButton {
                        etViewData.visibility = View.GONE
                    }
                }.show()
            }
        }
    }

    fun allPlayers(){
        playerItems.clear()
        GlobalScope.launch {
           val  postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        for (data in p0.children){

                            val  postListener = object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    if (p0.exists()){

                                        playerItems.add(AllPlayer(data.key,data.getValue(AllPlayer::class.java)!!.facebookId, data.getValue(AllPlayer::class.java)!!.name,
                                                p0.getValue(AllPlayer::class.java)!!.credit,data.getValue(AllPlayer::class.java)!!.lastOnline))
                                        allPlayersRecyclerViewAdapter.notifyDataSetChanged()
                                    }
                                }

                            }
                            database.child("users").child(data.key.toString()).child("balance").addListenerForSingleValueEvent(postListener)
                        }
                        srAllPlayer.isRefreshing = false
                    }
                }

            }
            database.child("users").addListenerForSingleValueEvent(postListener)
        }
    }

    fun addTournament(){
        btnAddTournament.onClick {
            if (llAddTournament.visibility == View.GONE)
                llAddTournament.visibility = View.VISIBLE
            else if(llAddTournament.visibility == View.VISIBLE){
                alert ("confirm"){
                    yesButton {
                        val values  = hashMapOf(
                                "description" to etTournamentDescription.text.toString(),
                                "price" to etTournamentPrice.text.toString().toLong(),
                                "reward1" to etTournamentReward1.text.toString().toLong(),
                                "reward2" to etTournamentReward2.text.toString().toLong(),
                                "reward3" to etTournamentReward3.text.toString().toLong(),
                                "title" to etTournamentTitle.text.toString(),
                                "type" to etTournamentType.text.toString()
                        )
                        database.child("tournament").child(etTournamentDate.text.toString()).setValue(values).addOnFailureListener{
                            toast(it.message.toString())
                        }.addOnSuccessListener {
                            toast("Success add Tournament")
                            llAddTournament.visibility = View.GONE
                        }
                    }
                    noButton {
                        llAddTournament.visibility = View.GONE
                    }
                }.show()
            }
        }
    }
}

data class AllPlayer(
    var uid: String? = "",
    var facebookId: String? = "",
    var name: String? = "",
    var credit: Long? = 0,
    var lastOnline: Long? = 0,
    var point: Long? = 0,
    var energy: Long? = 0,
    var energyLimit: Long? = 0,
    var currentRank: String? = "",
    var email: String? = "",
    var noHandphone: String? = "",
    var tournamentJoined: Long? = 0,
    var lose: Long? = 0,
    var win: Long? = 0,
    var tournamentWin: Long? = 0

)