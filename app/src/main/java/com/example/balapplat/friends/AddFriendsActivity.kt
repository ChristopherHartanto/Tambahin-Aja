package com.example.balapplat.friends

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.balapplat.R
import com.example.balapplat.model.Inviter
import com.example.balapplat.model.User
import com.example.balapplat.play.CountdownActivity
import com.example.balapplat.presenter.Presenter
import com.example.balapplat.utils.showSnackBar
import com.example.balapplat.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.activity_add_friends.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.onRefresh
import java.text.SimpleDateFormat
import java.util.*

class AddFriendsActivity : AppCompatActivity(), NetworkConnectivityListener,
    MainView {

    private lateinit var auth: FirebaseAuth
    private var items: MutableList<User> = mutableListOf()
    private var uids: MutableList<String> = mutableListOf()
    private var statusItems: MutableList<Boolean> = mutableListOf()
    private lateinit var database: DatabaseReference
    lateinit var presenter: Presenter
    lateinit var data: Inviter

    private lateinit var adapter: AddFriendRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friends)

        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance().reference
        presenter = Presenter(this, database)
        presenter.receiveInvitation()

        val typeface = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
        tvAddFriendsTitle.typeface = typeface

        adapter = AddFriendRecyclerViewAdapter(this,items,statusItems){
            addFriend(uids[it])
        }
        auth = FirebaseAuth.getInstance()
//        retrieve("")
        rvAddFriends.layoutManager = LinearLayoutManager(this)
        rvAddFriends.adapter = adapter

//        srAddFriend.onRefresh {
//            retrieve("")
//        }

        btnSearch.setOnClickListener {
            retrieve(etSearch.text.toString())
        }
        etSearch.setOnClickListener {
            etSearch.text.clear()
        }
    }

    private fun refreshRvAddFriend(){
        rvAddFriends.adapter?.notifyDataSetChanged()
        srAddFriend.isRefreshing = false
    }

    fun retrieve(filter : String){
        items.clear()
        uids.clear()
        statusItems.clear()

        GlobalScope.launch {
            val postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    //toast("" + p0.children)
                    fetchData(p0, filter)
                }

            }
            database.child("users").addListenerForSingleValueEvent(postListener)

        }
    }

    fun fetchData(dataSnapshot: DataSnapshot, filter : String){
        if (!filter.isEmpty()){
            for (ds in dataSnapshot.children) {
                if (!ds.key.equals(auth.currentUser!!.uid)){
                    val item = ds.getValue(User::class.java)!!
                    if (item.name?.toLowerCase()?.contains(filter.toLowerCase())!!)
                        checkStatusFriend(ds.key,item)
                }
            }
        }
        refreshRvAddFriend()
        toast("uid : " + uids)
    }
    fun checkStatusFriend(friendUid: String?, item:User){
        GlobalScope.launch {
            val postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists())
                        statusItems.add(true)
                    else
                        statusItems.add(false)

                    items.add(item)
                    uids.add(friendUid!!)
                    refreshRvAddFriend()
                }

            }
            database.child("friends").child(auth.currentUser!!.uid).child(friendUid!!).addListenerForSingleValueEvent(postListener)

        }

    }

    fun addFriend(friendUid: String){

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())

        database.child("friends").child(auth.currentUser!!.uid).child(friendUid).child("date").setValue(currentDate).addOnSuccessListener {
            toast("add friend")
            retrieve(etSearch.text.toString())

        }.addOnFailureListener {
            toast(""+ it.message)
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        data = dataSnapshot.getValue(Inviter::class.java)!!

        alert(data!!.name + " invite you to play"){
            title = "Invitation"
            yesButton {
                presenter.replyInvitation(true)
            }
            noButton {
                presenter.replyInvitation(false)
            }
        }.show()
    }

    override fun response(message: String) {
        if (message === "acceptedGame"){
            toast("acceptedGame")

            startActivity(intentFor<CountdownActivity>("inviterFacebookId" to data.facebookId,
                "inviterName" to data.name))
        }

    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(activity_add_friends, "The network is back !", "LONG")
                } else {
                    showSnackBar(activity_add_friends, "There is no more network", "INFINITE")
                }
            }
        }
    }

    override fun onPause() {
        presenter.dismissListener()
        super.onPause()
    }
}
