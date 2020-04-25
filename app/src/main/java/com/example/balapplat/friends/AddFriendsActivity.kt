package com.example.balapplat.friends

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.balapplat.MainView
import com.example.balapplat.Presenter
import com.example.balapplat.R
import com.example.balapplat.model.Inviter
import com.example.balapplat.model.User
import com.example.balapplat.play.CountdownActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_add_friends.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import java.util.*

class AddFriendsActivity : AppCompatActivity(), MainView {

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

        database = FirebaseDatabase.getInstance().reference
        presenter = Presenter(this,database)
        presenter.receiveInvitation()

        adapter = AddFriendRecyclerViewAdapter(this,items,statusItems){
            addFriend(uids[it])
        }
        auth = FirebaseAuth.getInstance()
        retrieve()
        rvAddFriends.layoutManager = LinearLayoutManager(this)
        rvAddFriends.adapter = adapter

    }

    fun retrieve(){
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
                    fetchData(p0)
                }

            }
            database.child("users").addValueEventListener(postListener)

        }
    }

    fun fetchData(dataSnapshot: DataSnapshot){

        for (ds in dataSnapshot.children) {
            if (!ds.key.equals(auth.currentUser!!.uid)){
                val item = ds.getValue(User::class.java)!!

                checkStatusFriend(ds.key,item)

            }

        }
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

                    rvAddFriends.adapter?.notifyDataSetChanged()

                }

            }
            database.child("friends").child(auth.currentUser!!.uid).child(friendUid!!).addValueEventListener(postListener)

        }
    }

    fun addFriend(friendUid: String){

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())

        database.child("friends").child(auth.currentUser!!.uid).child(friendUid).child("date").setValue(currentDate).addOnSuccessListener {
            toast("add friend")
            retrieve()

        }.addOnFailureListener {
            toast(""+ it.message)
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot) {
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
}
