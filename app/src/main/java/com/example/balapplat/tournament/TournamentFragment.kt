package com.example.balapplat.tournament

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
import com.example.balapplat.R
import com.example.balapplat.leaderboard.LeaderBoardRecyclerViewAdapter
import com.example.balapplat.model.HighScore
import com.example.balapplat.model.User
import com.example.balapplat.presenter.TournamentPresenter
import com.example.balapplat.utils.showSnackBar
import com.example.balapplat.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.fragment_tournament.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast


class Tournament : Fragment(), NetworkConnectivityListener, MainView {

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
        val linearLayoutManager = LinearLayoutManager(ctx)
        linearLayoutManager.reverseLayout = true
        rvStanding.layoutManager = linearLayoutManager

        rvStanding.adapter = adapter

        tournamentParticipants.clear()
        tournamentPresenter.fetchTournament()
        tournamentPresenter.fetchTournamentParticipants()

        val typeface = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
        tvTournamentTitle.typeface = typeface
        tvStandingTitle.typeface = typeface

        btnInfo.onClick {
            btnInfo.startAnimation(clickAnimation)
            popUpTournamentDetail()
        }

        btnJoinTournament.onClick {
            tournamentPresenter.joinTournament(auth,tournamentEndDate)
        }

        tournamentPresenter.fetchTournament()
        super.onStart()
    }


    fun loadData(dataSnapshot: DataSnapshot, status: Boolean){

//        val data = dataSnapshot.getValue(TournamentData::class.java)
//
//        if (data != null && status) {
//            tvTournamentTitle.text = data.title
//            tvTournamentDesc.text = data.description
//            tvTournamentTimeLeft.text = data.deadLine
//
//            retrieve()
////            GlobalScope.launch {
////                val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
////                val currentDate = sdf.format(Date())
////
////                Duration.
////                val diff: Long = Date.from() - date2.getTime()
////                val seconds = diff / 1000
////                val minutes = seconds / 60
////                val hours = minutes / 60
////                val days = hours / 24
////            }
//        }
//        else{
//            tvTournamentTitle.text = "No Tournament Right Now"
//            tvTournamentDesc.text = ""
////            tvTournamentTimeLeft.text = ""
////        }
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
                for ((index,data) in dataSnapshot.children.withIndex())
                if (index == 0) {
                    dataTournament = data.getValue(TournamentData::class.java)!!
                    tvTournamentTitle.text = dataTournament.title
                    tournamentEndDate = data.key.toString()
                    tvTournamentTimeLeft.text = "End Date: ${tournamentEndDate}"
                }

            }else{
                btnJoinTournament.visibility = View.GONE
            }
        }else if(response == "fetchTournamentParticipants"){
            if (dataSnapshot.exists()){
                tournamentParticipants.clear()
                for (data in dataSnapshot.children){
                    tournamentParticipants.add(data.getValue(TournamentParticipant::class.java)!!)
                }
                adapter.notifyDataSetChanged()
            }
        }

    }

    override fun response(message: String) {
        if (message == "joinTournament"){
            toast("Join Success")
            editor = sharedPreference.edit()
            editor.putBoolean("joinTournament",true)
            editor.apply()
            tournamentPresenter.fetchTournamentParticipants()
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
    var type: String? = ""
)

data class TournamentParticipant(
        var name: String? = "",
        var facebookId: String? = "",
        var point: Long? = 0
)