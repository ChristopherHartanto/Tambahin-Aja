package com.example.balapplat.friends

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager

import com.example.balapplat.R
import com.example.balapplat.home.CustomGameRecyclerViewAdapter
import com.example.balapplat.model.Friend
import com.example.balapplat.model.User
import com.example.balapplat.play.CountdownActivity
import com.example.balapplat.play.GameType
import com.example.balapplat.play.StatusPlayer
import com.example.balapplat.play.WaitingActivity
import com.example.balapplat.presenter.FriendPresenter
import com.example.balapplat.presenter.HomePresenter
import com.example.balapplat.rank.AvailableGame
import com.example.balapplat.utils.getFacebookProfilePicture
import com.example.balapplat.utils.showSnackBar
import com.example.balapplat.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_list_friends.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.ctx
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.*
import org.jetbrains.anko.yesButton

class ListFriendsFragment : Fragment(), NetworkConnectivityListener, MainView {

    private lateinit var callback: FriendsAddListener
    private lateinit var auth: FirebaseAuth
    private var items: MutableList<Friend> = mutableListOf()
    private var ProfileItems: MutableList<User> = mutableListOf()
    private lateinit var friendPresenter: FriendPresenter
    private lateinit var popupWindow : PopupWindow
    private var index = 0
    private var timer = 0
    private lateinit var database: DatabaseReference
    private lateinit var adapter: FriendsRecyclerViewAdapter
    private val availableGameList : MutableList<Boolean> = mutableListOf()
    private lateinit var customGameAdapter: CustomGameRecyclerViewAdapter
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)

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
            index = it
            if (ProfileItems[it].online == null){
                popUpMessage(Message.ReadOnly,ProfileItems[it].name + " is Offline")

            }else{
                popUpMessage(Message.Reply,"Play with " + ProfileItems[it].name + " ?")

            }

        }

        srFriendsList.onRefresh {
            ProfileItems.clear()
            friendPresenter.retrieve()
        }

        rvListFriends.layoutManager = LinearLayoutManager(ctx)
        rvListFriends.adapter = adapter

        super.onActivityCreated(savedInstanceState)
    }


    fun fetchProfileFriends(dataSnapshot: DataSnapshot){

        srFriendsList.isRefreshing = false
        toast(" "+dataSnapshot)
        val item = dataSnapshot.getValue(User::class.java)!!
        ProfileItems.add(item)
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

    private fun popUpCustomGame(){
        val inflater:LayoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.pop_up_custom_game,null)
        val main_view = inflater.inflate(R.layout.activity_main,null)


        // Initialize a new instance of popup window
        popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
                LinearLayout.LayoutParams.MATCH_PARENT// Window height
        )

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }
        var position = -1
        val main_activity = main_view.findViewById<RelativeLayout>(R.id.activity_main)
        val rvCustomGame = view.findViewById<RecyclerView>(R.id.rvCustomGame)
        val ivCustomGame = view.findViewById<ImageView>(R.id.ivCustomGame)
        val tvCustomGameName = view.findViewById<TextView>(R.id.tvCustomGameName)
        val tvChooseGame = view.findViewById<TextView>(R.id.tvClickToChooseGame)
        val tvCustomGameTitle = view.findViewById<TextView>(R.id.tvCustomGameTitle)
        val tvCustomGameTime = view.findViewById<TextView>(R.id.tvCustomGameTime)
        val sbTime = view.findViewById<SeekBar>(R.id.sbTime)
        val btnPlay = view.findViewById<Button>(R.id.btnStartCustomGame)
        val btnClose = view.findViewById<Button>(R.id.btnClose)
        val layoutCustomGame = view.findViewById<LinearLayout>(R.id.layout_custom_game)

        val typeface : Typeface? = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
        tvCustomGameName.typeface = typeface
        tvCustomGameTitle.typeface = typeface
        tvCustomGameTime.typeface = typeface

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sbTime.min = 30
        }
        timer = 0
        sbTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                if(progress < 30) {
                    timer = progress
                    tvCustomGameTime.text = "Time : 30"
                }
                else {
                    timer = progress
                    tvCustomGameTime.text = "Time : $progress"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    timer = seekBar.progress
                }
            }

        })

        customGameAdapter = CustomGameRecyclerViewAdapter(ctx,availableGameList){
            if (!availableGameList[it]){
                toast("Not Available")
            }else{
                position = it
                when(position){
                    0 -> {
                        tvCustomGameName.text = "Normal Game"
                        ivCustomGame.setImageResource(R.drawable.normal_game)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sbTime.min = 30
                            sbTime.max = 90
                        }
                    }
                    1 -> {
                        tvCustomGameName.text = "Odd Even"
                        ivCustomGame.setImageResource(R.drawable.odd_even_game)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sbTime.min = 30
                            sbTime.max = 90
                        }
                    }
                    2 -> {
                        tvCustomGameName.text = "Rush"
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sbTime.min = 3
                            sbTime.max = 6
                        }
                        ivCustomGame.setImageResource(R.drawable.rush_game)
                    }
                    3 -> {
                        tvCustomGameName.text = "Alpha Num"
                        ivCustomGame.setImageResource(R.drawable.alpha_num_game)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sbTime.min = 30
                            sbTime.max = 90
                        }
                    }
                }
                rvCustomGame.visibility = View.GONE
                tvCustomGameName.visibility = View.VISIBLE
                ivCustomGame.visibility = View.VISIBLE
                tvChooseGame.visibility = View.VISIBLE
                tvChooseGame.text = "Click to Choose Other Games"
            }

        }
        rvCustomGame.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL,false)

        ivCustomGame.onClick {
            tvCustomGameName.visibility = View.GONE
            tvChooseGame.visibility = View.GONE
            ivCustomGame.visibility = View.GONE
            rvCustomGame.visibility = View.VISIBLE
            position = -1
        }

        btnClose.onClick {
            btnClose.startAnimation(clickAnimation)
            fragment_list_friends.alpha = 1F
            popupWindow.dismiss()
        }

        btnPlay.onClick {
            if (position == -1)
                toast("Choose game First")
            else{
                fragment_list_friends.alpha = 1F
                popupWindow.dismiss()
                when (position) {
                    0 -> {
                        ctx.startActivity(intentFor<WaitingActivity>("joinFriendFacebookId" to ProfileItems[index].facebookId,
                                "joinFriendName" to ProfileItems[index].name,"type" to GameType.Normal, "timer" to timer))
                    }
                    1 -> {
                        ctx.startActivity(intentFor<WaitingActivity>("joinFriendFacebookId" to ProfileItems[index].facebookId,
                            "joinFriendName" to ProfileItems[index].name,"type" to GameType.OddEven, "timer" to timer))
                    }
                    2 -> {
                        ctx.startActivity(intentFor<WaitingActivity>("joinFriendFacebookId" to ProfileItems[index].facebookId,
                            "joinFriendName" to ProfileItems[index].name,"type" to GameType.Rush, "timer" to timer))
                    }
                    3 -> {
                        ctx.startActivity(intentFor<WaitingActivity>("joinFriendFacebookId" to ProfileItems[index].facebookId,
                            "joinFriendName" to ProfileItems[index].name,"type" to GameType.AlphaNum, "timer" to timer))
                    }
                    4 ->{
                        ctx.startActivity(intentFor<WaitingActivity>("joinFriendFacebookId" to ProfileItems[index].facebookId,
                            "joinFriendName" to ProfileItems[index].name,"type" to GameType.Normal, "timer" to timer))
                    }
                    5 -> {
                        ctx.startActivity(intentFor<WaitingActivity>("joinFriendFacebookId" to ProfileItems[index].facebookId,
                                "joinFriendName" to ProfileItems[index].name, "type" to GameType.DoubleAttack, "timer" to position))
                    }

                }
            }

        }

        rvCustomGame.adapter = customGameAdapter
        fragment_list_friends.alpha = 0.1F

        TransitionManager.beginDelayedTransition(fragment_list_friends)
        popupWindow.showAtLocation(
                fragment_list_friends, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if(response == "availableGame"){
            if (dataSnapshot.exists()){
                availableGameList.clear()
                availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.normal!!)
                availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.oddEven!!)
                availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.rush!!)
                availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.alphaNum!!)
                availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.mix!!)
                availableGameList.add(dataSnapshot.getValue(AvailableGame::class.java)?.doubleAttack!!)
            }
            else{
                availableGameList.clear()
                availableGameList.add(true)
                availableGameList.add(false)
                availableGameList.add(false)
                availableGameList.add(false)
                availableGameList.add(false)
                availableGameList.add(false)
            }
            customGameAdapter.notifyDataSetChanged()

        }else if(response == "retriveProfileFriends"){
            fetchProfileFriends(dataSnapshot)
        }

    }

    private fun popUpMessage(type: Message, message: String){
        val inflater: LayoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.pop_up_message,null)

        // Initialize a new instance of popup window
        popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
                LinearLayout.LayoutParams.MATCH_PARENT// Window height
        )

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }
        val typeface : Typeface? = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)

        val layoutMessageInvitation = view.findViewById<LinearLayout>(R.id.layout_message_invitation)
        val layoutMessageBasic = view.findViewById<LinearLayout>(R.id.layout_message_basic)
        val layoutMessageReward = view.findViewById<LinearLayout>(R.id.layout_message_reward)
        val btnClose = view.findViewById<Button>(R.id.btnMessageClose)
        val btnReject = view.findViewById<Button>(R.id.btnMessageReject)
        val tvMessageTitle = view.findViewById<TextView>(R.id.tvMessageTitle)
        val ivInviter = view.findViewById<CircleImageView>(R.id.ivInviter)
        val tvMessageInviter = view.findViewById<TextView>(R.id.tvMessageInviter)

        tvMessageTitle.text = "Play with Friend"
        tvMessageInviter.typeface = typeface
        tvMessageInviter.typeface = typeface

        if (type == Message.Reply){
            Picasso.get().load(getFacebookProfilePicture(ProfileItems[index].facebookId.toString())).fit().into(ivInviter)
            tvMessageInviter.text = message
            btnReject.text = "No"
            btnClose.text = "yes"
            btnClose.onClick {
                btnClose.startAnimation(clickAnimation)
                fragment_list_friends.alpha = 1F
                popupWindow.dismiss()
                availableGameList.clear()
                friendPresenter.fetchAvailableGame()
                popUpCustomGame()

            }

            btnReject.onClick {
                btnReject.startAnimation(clickAnimation)
                fragment_list_friends.alpha = 1F
                popupWindow.dismiss()
            }
        }
        else if(type == Message.ReadOnly){
            ivInviter.visibility = View.GONE
            tvMessageInviter.text = message
            btnReject.visibility = View.GONE

            btnClose.onClick {
                btnClose.startAnimation(clickAnimation)
                fragment_list_friends.alpha = 1F
                popupWindow.dismiss()
            }
        }

        tvMessageTitle.typeface = typeface

        fragment_list_friends.alpha = 0.1F

        TransitionManager.beginDelayedTransition(fragment_list_friends)
        popupWindow.showAtLocation(
                fragment_list_friends, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

    }

    override fun response(message: String) {

    }

    override fun onStart() {
        friendPresenter = FriendPresenter(this,database)
        ProfileItems.clear()
        friendPresenter.retrieve()
        callback = activity as FriendsActivity
        friendPresenter = FriendPresenter(this,database)
        super.onStart()
    }

    override fun onPause() {
        friendPresenter.dismissListener()
        super.onPause()
    }
}

enum class Message{
    ReadOnly,
    Reply
}

interface FriendsAddListener{
    fun removeFragment()
}