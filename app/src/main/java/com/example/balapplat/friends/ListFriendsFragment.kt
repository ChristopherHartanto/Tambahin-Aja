package com.example.balapplat.friends

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.balapplat.R
import com.example.balapplat.model.Friend
import com.example.balapplat.model.User
import com.example.balapplat.play.WaitingActivity
import com.example.balapplat.utils.showSnackBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.fragment_list_friends.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.support.v4.*
import org.jetbrains.anko.yesButton

class ListFriendsFragment : Fragment(), NetworkConnectivityListener {

    private lateinit var auth: FirebaseAuth
    private var items: MutableList<Friend> = mutableListOf()
    private var ProfileItems: MutableList<User> = mutableListOf()
    private lateinit var database: DatabaseReference
    private lateinit var adapter: FriendsRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_friends, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
//
//        val stamp = Timestamp(System.currentTimeMillis())
//        val date = Date(stamp.getTime())

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        adapter = FriendsRecyclerViewAdapter(ctx,ProfileItems){
//            if (!ProfileItems[it].active!!){
//                alert (ProfileItems[it].name + "   is Offline"){
//                    title = "Offline"
//                    okButton {
//
//                    }
//                }.show()
//            }else{
//
//            }
                val index = it
                alert ("Play with " + ProfileItems[it].name + " ?"){
                    title = "Play"
                    yesButton {
                        ctx.startActivity(intentFor<WaitingActivity>("facebookId" to ProfileItems[index].facebookId,
                            "name" to ProfileItems[index].name))
                    }
                    noButton {

                    }
                }.show()

        }

        srFriendsList.onRefresh {
            retrieve()
        }

        retrieve()
        rvListFriends.layoutManager = LinearLayoutManager(ctx)
        rvListFriends.adapter = adapter

        super.onActivityCreated(savedInstanceState)
    }

    fun retrieve(){

        GlobalScope.launch {
            val postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    fetchDataFriends(p0)
                }

            }
            database.child("friends").child(auth.currentUser!!.uid).addValueEventListener(postListener)

        }
    }

    fun fetchDataFriends(dataSnapshot: DataSnapshot){
        ProfileItems.clear()
        for (ds in dataSnapshot.children) {
            if (ds.key!! != auth.currentUser!!.uid){
                retrieveProfileFriends(ds.key)
            }

        }
    }

    fun retrieveProfileFriends(friendUid: String?){
        GlobalScope.launch {
            val postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    //toast("" + p0.children)
                    fetchProfileFriends(p0)
                }

            }
            database.child("users").child(friendUid!!).addListenerForSingleValueEvent(postListener)

        }
    }

    fun fetchProfileFriends(dataSnapshot: DataSnapshot){

        srFriendsList.isRefreshing = false
//        for (ds in dataSnapshot.children) {
//            toast("" + ds)
//            val item = ds.getValue(User::class.java)!!
//            ProfileItems.add(item)
//        }
        toast(" "+dataSnapshot)
        val item = dataSnapshot.getValue(User::class.java)!!
        ProfileItems.add(item)
        //toast("nama teman "+ dataSnapshot)
        adapter.notifyDataSetChanged()

    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(fragment_list_friends, "The network is back !", "LONG")
                } else {
                    showSnackBar(fragment_list_friends, "There is no more network", "INFINITE")
                }
            }
        }
    }

}
