package com.example.tambahinaja.tournament

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.example.tambahinaja.R
import com.example.tambahinaja.friends.Message
import com.example.tambahinaja.main.MainActivity
import com.example.tambahinaja.model.HighScore
import com.example.tambahinaja.presenter.TournamentPresenter
import com.example.tambahinaja.rank.Rank
import com.example.tambahinaja.utils.getFacebookProfilePicture
import com.example.tambahinaja.utils.showSnackBar
import com.example.tambahinaja.view.MainView
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_tournament.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import java.text.SimpleDateFormat
import java.util.*


class Tournament : Fragment(), NetworkConnectivityListener, MainView {

    private lateinit var callback: FragmentListener
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var auth: FirebaseAuth
    private var items: MutableList<HighScore> = mutableListOf()
    private var tournamentParticipants: MutableList<TournamentParticipant> = mutableListOf()
    private lateinit var database: DatabaseReference
    private lateinit var popupWindow : PopupWindow
    private lateinit var tournamentPresenter: TournamentPresenter
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)
    private lateinit var dataTournament: TournamentData
    private lateinit var currentRank : String
    private var diff: Long = 0
    private var price = 0
    private lateinit var adapter: TournamentRecyclerViewAdapter
    private var tournamentEndDate = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tournament, container, false)
    }

    override fun onStart() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        adapter = TournamentRecyclerViewAdapter(ctx,tournamentParticipants)
        sharedPreference =  ctx.getSharedPreferences("LOCAL_DATA",Context.MODE_PRIVATE)
        tournamentPresenter = TournamentPresenter(this,database)
        callback = activity as MainActivity
        currentRank = sharedPreference.getString("currentRank", Rank.Toddler.toString()).toString()
        val linearLayoutManager = LinearLayoutManager(ctx)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        rvStanding.layoutManager = linearLayoutManager

        rvStanding.adapter = adapter

        tournamentParticipants.clear()
        tournamentPresenter.fetchTournament()


        val typeface = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
        tvTournamentTitle.typeface = typeface
        tvStandingTitle.typeface = typeface
        tvTournamentProfile.typeface = typeface
        tvTournamentTimeLeft.typeface = typeface

        btnInfo.onClick {
            btnInfo.startAnimation(clickAnimation)
            popUpTournamentDetail()
        }

        btnJoinTournament.onClick {
            tournamentPresenter.checkPoint(auth,price.toLong())

        }

        tournamentPresenter.fetchTournament()
        super.onStart()
    }


    fun loadData(dataSnapshot: DataSnapshot, status: Boolean){

    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(fragment_tournament, "The network is back !", "LONG")
                } else {
                    showSnackBar(fragment_tournament, "There is no more network", "INFINITE")
                }
            }
        }
    }

    private fun popUpTournamentDetail(){
        val inflater:LayoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.pop_up_tournament_info,null)

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

        val btnClose = view.findViewById<Button>(R.id.btnClose)
        val tvTournamentTitle = view.findViewById<TextView>(R.id.tvTournamentTitle)
        val tvTournamentDetail = view.findViewById<TextView>(R.id.tvTournamentDetail)
        val tvTournamentFirstPosition = view.findViewById<TextView>(R.id.tvTournamentFirstPosition)
        val tvTournamentSecondPosition = view.findViewById<TextView>(R.id.tvTournamentSecondPosition)
        val tvTournamentThirdPosition = view.findViewById<TextView>(R.id.tvTournamentThirdPosition)
        val tvTournamentFirstReward = view.findViewById<TextView>(R.id.tvTournamentFirstReward)
        val tvTournamentSecondReward = view.findViewById<TextView>(R.id.tvTournamentSecondReward)
        val tvTournamentThirdReward = view.findViewById<TextView>(R.id.tvTournamentThirdReward)

        val typeface : Typeface? = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
        tvTournamentDetail.typeface = typeface
        tvTournamentFirstPosition.typeface = typeface
        tvTournamentSecondPosition.typeface = typeface
        tvTournamentThirdPosition.typeface = typeface
        tvTournamentFirstReward.typeface = typeface
        tvTournamentSecondReward.typeface = typeface
        tvTournamentThirdReward.typeface = typeface
        tvTournamentTitle.typeface = typeface


        btnClose.onClick {
            btnClose.startAnimation(clickAnimation)
            fragment_tournament.alpha = 1F
            popupWindow.dismiss()
        }

        tvTournamentTitle.text = dataTournament.title
        tvTournamentDetail.text = dataTournament.description
        tvTournamentFirstReward.text = dataTournament.reward1.toString()
        tvTournamentSecondReward.text = dataTournament.reward2.toString()
        tvTournamentThirdReward.text = dataTournament.reward3.toString()

        fragment_tournament.alpha = 0.1F

        TransitionManager.beginDelayedTransition(fragment_tournament)
        popupWindow.showAtLocation(
                fragment_tournament, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == "fetchTournament"){
            if (dataSnapshot.exists()){
                for ((index,data) in dataSnapshot.children.withIndex()){
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val currentDate = Date().time
                    val tournamentDate = sdf.parse(data.key.toString()).time
                    diff = tournamentDate - currentDate

                    if (diff > 0)
                        btnJoinTournament.visibility = View.VISIBLE
                    else
                        btnJoinTournament.visibility = View.GONE

                    val joinTournament = sharedPreference.getString("joinTournament","")
                    if (data.key.toString() == joinTournament){
                        btnJoinTournament.visibility = View.GONE
                        ivTournamentProfile.visibility = View.VISIBLE
                        tvTournamentProfile.visibility = View.VISIBLE
                    }else{
                        editor = sharedPreference.edit()
                        editor.remove("joinTournament")
                        editor.apply()

                        btnJoinTournament.visibility = View.VISIBLE
                        ivTournamentProfile.visibility = View.GONE
                        tvTournamentProfile.visibility = View.GONE
                    }
                    dataTournament = data.getValue(TournamentData::class.java)!!
                    tvTournamentTitle.text = dataTournament.title
                    tournamentEndDate = data.key.toString()
                    tournamentPresenter.fetchTournamentParticipants(tournamentEndDate)
                    val seconds = diff / 1000
                    val minutes = seconds / 60
                    val hours = minutes / 60
                    val days = hours / 24

                    if (days >= 1)
                        tvTournamentTimeLeft.text = "Ends In ${days} Days ${hours/24} Hours"
                    else if(hours in 1..23)
                        tvTournamentTimeLeft.text = "Ends In ${hours} Hours ${minutes/60} Minutes"
                    else if(minutes in 1..59)
                        tvTournamentTimeLeft.text = "Ends In ${minutes}"
                    else if(minutes >= 0)
                        tvTournamentTimeLeft.text = "Less Than 1 Minute"
                    else
                        tvTournamentTimeLeft.text = "End"
                }

            }else{
                btnJoinTournament.visibility = View.GONE
            }
        }else if(response == "fetchTournamentParticipants"){
            if (dataSnapshot.exists()){
                tournamentParticipants.clear()
                var count = dataSnapshot.childrenCount
                for (data in dataSnapshot.children){

                    if (auth.currentUser != null){
                        if (data.getValue(TournamentParticipant::class.java)!!.facebookId.equals(Profile.getCurrentProfile().id)){
                            Picasso.get().load(getFacebookProfilePicture(data.getValue(TournamentParticipant::class.java)!!.facebookId.toString()))
                                    .into(ivTournamentProfile)
                            tvTournamentProfile.text = "#$count " + auth.currentUser!!.displayName +" ${data.getValue(TournamentParticipant::class.java)!!.point}"
                        }
                        if (diff < 0 && count.toInt() == 1){ // update rank
                            if (currentRank == Rank.Master.toString()){
                                val progress1 = sharedPreference.getInt("master1",0)
                                editor = sharedPreference.edit()
                                editor.putInt("master1",progress1+1)
                                editor.apply()
                            }else if (currentRank == Rank.GrandMaster.toString()){
                                val progress2 = sharedPreference.getInt("gMaster2",0)
                                editor = sharedPreference.edit()
                                editor.putInt("gMaster2",progress2+1)
                                editor.apply()
                            }
                        }
                    }
                    count--
                    tournamentParticipants.add(data.getValue(TournamentParticipant::class.java)!!)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun popUp(type: com.example.tambahinaja.friends.Message, message: String){
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
        val btnClose = view.findViewById<Button>(R.id.btnMessageClose)
        val btnReject = view.findViewById<Button>(R.id.btnMessageReject)
        val tvMessageTitle = view.findViewById<TextView>(R.id.tvMessageTitle)
        val tvMessageInfo = view.findViewById<TextView>(R.id.tvMessageInfo)

        if (type == Message.ReadOnly){
            layoutMessageBasic.visibility = View.VISIBLE
            layoutMessageInvitation.visibility = View.GONE
            btnReject.visibility = View.GONE
            tvMessageInfo.text = message
            tvMessageTitle.text = "Message"

            btnClose.onClick {
                btnClose.startAnimation(clickAnimation)
                fragment_tournament.alpha = 1F
                popupWindow.dismiss()
            }
        }else{
            layoutMessageBasic.visibility = View.VISIBLE
            layoutMessageInvitation.visibility = View.GONE
            tvMessageInfo.text = message
            btnReject.visibility = View.VISIBLE
            btnReject.text = "No"
            btnClose.text = "Yes"

            btnReject.onClick {
                btnReject.startAnimation(clickAnimation)
                fragment_tournament.alpha = 1F
                popupWindow.dismiss()
            }

            btnClose.onClick {
                tournamentPresenter.updatePoint(auth,price.toLong())
                tournamentPresenter.joinTournament(auth,tournamentEndDate)
                btnClose.startAnimation(clickAnimation)
                fragment_tournament.alpha = 1F
                popupWindow.dismiss()
            }
        }

        tvMessageTitle.typeface = typeface
        tvMessageInfo.typeface = typeface

        fragment_tournament.alpha = 0.1F

        TransitionManager.beginDelayedTransition(fragment_tournament)
        popupWindow.showAtLocation(
                fragment_tournament, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

    }

    override fun response(message: String) {
        if (message == "joinTournament"){
            editor = sharedPreference.edit()
            editor.putString("joinTournament",tournamentEndDate)
            editor.apply()
            popUp(Message.ReadOnly,"Success Join this Tournament")
            btnJoinTournament.visibility = View.GONE
            tournamentPresenter.fetchTournamentParticipants(tournamentEndDate)
            if (currentRank == Rank.Senior.toString()){
                editor = sharedPreference.edit()
                editor.putInt("senior4",1)
                editor.apply()
            }
        }else if(message == "continueJoinTournament"){
            popUp(Message.Reply,"Do You Want to Join this Tournament?")
        }else if(message == "notEnoughPoint"){
            popUp(Message.ReadOnly,"Not Enough Point")
        }else{
            popUp(Message.ReadOnly,message)
        }

    }

    override fun onPause() {
        tournamentPresenter.dismissListener()
        super.onPause()
    }
}

data class TournamentData(
    var reward1: Long? = 0,
    var reward2: Long? = 0,
    var reward3: Long? = 0,
    var description: String? = "",
    var title: String? = "",
    var type: String? = "",
    var price: Long? = 0
)

data class TournamentParticipant(
        var name: String? = "",
        var facebookId: String? = "",
        var point: Long? = 0
)

interface FragmentListener{
    fun finishFragment()
    fun backToHome()
}