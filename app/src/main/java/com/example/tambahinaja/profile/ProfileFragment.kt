package com.example.tambahinaja.profile

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import com.example.tambahinaja.R
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.example.tambahinaja.main.LoginActivity
import com.example.tambahinaja.main.MainActivity
import com.example.tambahinaja.main.Reward
import com.example.tambahinaja.model.Inviter
import com.example.tambahinaja.model.User
import com.example.tambahinaja.play.CountdownActivity
import com.example.tambahinaja.presenter.ProfilePresenter
import com.example.tambahinaja.tournament.FragmentListener
import com.example.tambahinaja.utils.showSnackBar
import com.example.tambahinaja.view.MainView
import com.facebook.AccessToken
import com.facebook.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.ivProfile
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import java.util.regex.Pattern

class ProfileFragment : Fragment(), NetworkConnectivityListener, MainView {

    private lateinit var auth: FirebaseAuth
    private lateinit var historyAdapter: HistoryRecyclerViewAdapter
    private lateinit var database: DatabaseReference
    private lateinit var callback: FragmentListener
    private lateinit var profilePresenter: ProfilePresenter
    private lateinit var popupWindow : PopupWindow
    private lateinit var user: User
    var name = ""
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)
    private var historyItems: MutableList<History> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        callback = activity as MainActivity

        profilePresenter = ProfilePresenter(this,database)
        if(AccessToken.getCurrentAccessToken() == null || auth.currentUser == null || Profile.getCurrentProfile() == null){
            callback.backToHome()
        }
        else{
            profilePresenter.fetchHistory()
            profilePresenter.fetchStats()
        }

        ivSetting.onClick {
            startActivity<SettingActivity>()
        }
        updateUI()
    }

    private fun updateUI(){
        if (context != null){
            val typeface = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
            tvProfileName.typeface = typeface
            tvHistoryTitle.typeface = typeface
            tvWinTournament.typeface = typeface
            tvWin.typeface = typeface

            if (auth.currentUser != null){

                profilePresenter.fetchName()

                Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivProfile)

                layout_edit_profile.onClick {
                    popUpEditProfile()
                }
            }else{
                tvProfileName.text = "Unknown"
            }
        }
    }



    fun getFacebookProfilePicture(userID: String): String {
        return "https://graph.facebook.com/$userID/picture?type=large"
    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(fragment_profile, "The network is back !", "LONG")
                } else {
                    showSnackBar(fragment_profile, "There is no more network", "INFINITE")
                }
            }
        }
    }

    private fun popUpEditProfile(){
        val inflater:LayoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.pop_up_edit_profile,null)
        val main_view = inflater.inflate(R.layout.activity_main,null)
        // Initialize a new instance of popup window
        popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
                LinearLayout.LayoutParams.MATCH_PARENT,// Window height
                true
        )

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }
        val main_activity = main_view.findViewById<RelativeLayout>(R.id.activity_main)
        val btnClose = view.findViewById<Button>(R.id.btnClose)
        val btnSave = view.findViewById<Button>(R.id.btnEditProfileSave)
        val tvEdiProfileTitle = view.findViewById<TextView>(R.id.tvEditProfileTitle)
        val tvEditProfileName = view.findViewById<TextView>(R.id.tvEditProfileName)
        val tvEditProfileEmail = view.findViewById<TextView>(R.id.tvEditProfileEmail)
        val tvEditProfileHandphone = view.findViewById<TextView>(R.id.tvEditProfileHandphone)
        val etEditProfileName = view.findViewById<EditText>(R.id.etEditProfileName)
        val etEditProfileEmail = view.findViewById<EditText>(R.id.etEditProfileEmail)
        val etEditProfileHandphone = view.findViewById<EditText>(R.id.etEditProfileHandphone)

        val typeface : Typeface? = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)
        btnClose.typeface = typeface
        btnSave.typeface = typeface
        tvEdiProfileTitle.typeface = typeface
        tvEditProfileName.typeface = typeface
        tvEditProfileEmail.typeface = typeface
        tvEditProfileHandphone.typeface = typeface
        etEditProfileEmail.typeface = typeface
        etEditProfileHandphone.typeface = typeface
        etEditProfileName.typeface = typeface

        etEditProfileName.setText("${user.name}")
        etEditProfileEmail.setText("${user.email}")
        etEditProfileHandphone.setText("${user.noHandphone}")

        btnClose.onClick {
            btnClose.startAnimation(clickAnimation)
            fragment_profile.alpha = 1F
            main_activity.alpha = 1F
            popupWindow.dismiss()
        }

        btnSave.onClick {
            if (!isValidEmail(etEditProfileEmail.text.toString()))
                toast("Input Valid Email Address")
            else if(!isValidMobile(etEditProfileHandphone.text.toString()))
                toast("Input Valid No Handphone")
            else {
                profilePresenter.saveProfile(etEditProfileName.text.toString(),etEditProfileEmail.text.toString(),etEditProfileHandphone.text.toString())
                tvProfileName.text = etEditProfileName.text.toString()
                btnClose.startAnimation(clickAnimation)
                fragment_profile.alpha = 1F
                main_activity.alpha = 1F
                popupWindow.dismiss()
            }
        }
        main_activity.alpha = 0.1F
        fragment_profile.alpha = 0.1F

        TransitionManager.beginDelayedTransition(fragment_profile)
        popupWindow.showAtLocation(
                fragment_profile, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

    }

    fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }
    fun isValidMobile(phone: String) : Boolean {
        if(!Pattern.matches("[a-zA-Z]+", phone)) {
            return phone.length in 7..13;
        }
        return false;
    }


    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == "fetchHistory") {
            historyItems.clear()
            for (data in dataSnapshot.children) {
                val item = data.getValue(History::class.java)
                historyItems.add(item!!)
            }
            profilePresenter.fetchUserProfile()
        }else if(response == "fetchName"){
            tvProfileName.text = dataSnapshot.value.toString()
        }else if(response == "fetchStats"){
            val data = dataSnapshot.getValue(Stats::class.java)
            if (data != null) {
                tvWin.text = data.win.toString()
                tvWinTournament.text = data.tournamentWin.toString()
            }
        }else if(response == "fetchUserProfile"){
            name = dataSnapshot.getValue(User::class.java)!!.name.toString()
            user = User(dataSnapshot.getValue(User::class.java)!!.name,
                    "", dataSnapshot.getValue(User::class.java)?.email,
                    dataSnapshot.getValue(User::class.java)?.noHandphone,null)
            initRv()
        }
    }

    fun initRv(){
        historyAdapter = HistoryRecyclerViewAdapter(ctx,historyItems, User(name,Profile.getCurrentProfile().id,"",""))
        val linearLayoutManager = LinearLayoutManager(ctx)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        rvHistory.layoutManager = linearLayoutManager
        rvHistory.adapter = historyAdapter
    }

    override fun response(message: String) {

    }

    override fun onPause() {
        profilePresenter.dismissListener()
        super.onPause()
    }


}

data class History(
        var opponentName: String? = "",
        var opponentFacebookId: String? = "",
        var opponentPoint: Long? = 0,
        var point: Long? = 0,
        var status: String? = ""
)

data class Stats(
    var win: Int? = 0,
    var lose: Int? = 0,
    var tournamentWin: Int? = 0
)