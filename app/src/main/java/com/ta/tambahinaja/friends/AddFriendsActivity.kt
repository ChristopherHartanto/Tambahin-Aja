package com.ta.tambahinaja.friends

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ta.tambahinaja.R
import com.ta.tambahinaja.model.Inviter
import com.ta.tambahinaja.model.User
import com.ta.tambahinaja.utils.getFacebookProfilePicture
import com.ta.tambahinaja.utils.showSnackBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_add_friends.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.text.SimpleDateFormat
import java.util.*

class AddFriendsActivity : AppCompatActivity(), NetworkConnectivityListener{

    private lateinit var auth: FirebaseAuth
    private var items: MutableList<User> = mutableListOf()
    private var uids: MutableList<String> = mutableListOf()
    private var statusItems: MutableList<Boolean> = mutableListOf()
    private lateinit var database: DatabaseReference
    private lateinit var popupWindow: PopupWindow
    lateinit var data: Inviter
    private var index = 0
    private val clickAnimation = AlphaAnimation(1.2F, 0.6F)

    private lateinit var adapter: AddFriendRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friends)

        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance().reference

        val typeface = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
        tvAddFriendsTitle.typeface = typeface

        adapter = AddFriendRecyclerViewAdapter(this, items, statusItems) {
            index = it
            popUpMessage(Message.Reply, "Add Friend with ${items[it].name}")
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

    private fun refreshRvAddFriend() {
        rvAddFriends.adapter?.notifyDataSetChanged()
        srAddFriend.isRefreshing = false
    }

    fun retrieve(filter: String) {
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

    fun fetchData(dataSnapshot: DataSnapshot, filter: String) {
        if (!filter.isEmpty()) {
            for (ds in dataSnapshot.children) {
                if (!ds.key.equals(auth.currentUser!!.uid)) {
                    val item = ds.getValue(User::class.java)!!
                    if (item.name?.toLowerCase()?.contains(filter.toLowerCase())!!)
                        checkStatusFriend(ds.key, item)
                }
            }
        }
        refreshRvAddFriend()
        toast("uid : " + uids)
    }

    fun checkStatusFriend(friendUid: String?, item: User) {
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
            database.child("friends").child(auth.currentUser!!.uid).child(friendUid!!)
                .addListenerForSingleValueEvent(postListener)

        }

    }

    fun addFriend(friendUid: String) {

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())

        database.child("friends").child(auth.currentUser!!.uid).child(friendUid).child("date")
            .setValue(currentDate).addOnSuccessListener {
            toast("add friend")
            retrieve(etSearch.text.toString())
            popUpMessage(Message.ReadOnly, "Success")
        }.addOnFailureListener {
            toast("" + it.message)
        }
    }

    private fun popUpMessage(type: Message, message: String) {
        val inflater: LayoutInflater =
            ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.pop_up_message, null)
        popupWindow = PopupWindow(
            view,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }
        val typeface: Typeface? = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)

        val btnClose = view.findViewById<Button>(R.id.btnMessageClose)
        val btnReject = view.findViewById<Button>(R.id.btnMessageReject)
        val tvMessageTitle = view.findViewById<TextView>(R.id.tvMessageTitle)
        val ivInviter = view.findViewById<CircleImageView>(R.id.ivInviter)
        val tvMessageInviter = view.findViewById<TextView>(R.id.tvMessageInviter)

        tvMessageTitle.text = "Add Friend"
        tvMessageInviter.typeface = typeface
        tvMessageInviter.typeface = typeface

        if (type == Message.Reply) {
            Picasso.get().load(getFacebookProfilePicture(items[index].facebookId.toString())).fit()
                .into(ivInviter)
            tvMessageInviter.text = message
            btnReject.text = "No"
            btnClose.text = "yes"
            btnClose.onClick {
                if (!statusItems[index]) {
                    addFriend(uids[index])
                    btnClose.startAnimation(clickAnimation)
                    activity_add_friends.alpha = 1F
                    popupWindow.dismiss()
                } else {
                    btnClose.startAnimation(clickAnimation)
                    activity_add_friends.alpha = 1F
                    popupWindow.dismiss()
                    popUpMessage(Message.ReadOnly, "You Already Made Friend")
                }
            }
            btnReject.onClick {
                btnReject.startAnimation(clickAnimation)
                activity_add_friends.alpha = 1F
                popupWindow.dismiss()
            }
        } else if (type == Message.ReadOnly) {
            ivInviter.visibility = View.GONE
            tvMessageInviter.text = message
            btnReject.visibility = View.GONE

            btnClose.onClick {
                btnClose.startAnimation(clickAnimation)
                activity_add_friends.alpha = 1F
                popupWindow.dismiss()
            }
        }

        tvMessageTitle.typeface = typeface
        activity_add_friends.alpha = 0.1F
        TransitionManager.beginDelayedTransition(activity_add_friends)
        popupWindow.showAtLocation(
            activity_add_friends, // Location to display popup windoW
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
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
}
