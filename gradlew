package com.ta.tambahinaja.rank

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Message
import android.text.Layout
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.ta.tambahinaja.play.CountdownActivity
import com.ta.tambahinaja.view.MainView
import com.ta.tambahinaja.presenter.Presenter
import com.ta.tambahinaja.R
import com.ta.tambahinaja.home.MarketActivity
import com.ta.tambahinaja.main.MainActivity
import com.ta.tambahinaja.model.Inviter
import com.ta.tambahinaja.play.GameType
import com.ta.tambahinaja.play.StatusPlayer
import com.ta.tambahinaja.presenter.RankPresenter
import com.facebook.Profile
import com.ta.tambahinaja.utils.showSnackBar
import com.ta.tambahinaja.view.RankView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_market.*
import kotlinx.android.synthetic.main.activity_rank.*
import kotlinx.android.synthetic.main.activity_rank.ivProfile
import kotlinx.android.synthetic.main.activity_rank.tvEnergy
import kotlinx.android.synthetic.main.activity_rank.tvPoint
import kotlinx.android.synthetic.main.activity_waiting.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.layout_loading.*
import kotlinx.android.synthetic.main.pop_up_task.*
import kotlinx.android.synthetic.main.row_choose_game.*
import kotlinx.android.synthetic.main.row_rank.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import java.text.SimpleDateFormat
import java.util.*

class RankActivity : AppCompatActivity(), NetworkConnectivityListener,RankView {

    private lateinit var sharedPreference: SharedPreferences
    private lateinit var adapter: RankRecyclerViewAdapter
    private lateinit var taskAdapter: TaskRecyclerViewAdapter
    private lateinit var rankDetailAdapter: RankDetailRecyclerViewAdapter
    private lateinit var countDownTimer : CountDownTimer
    private lateinit var database: DatabaseReference
    lateinit var rankPresenter: RankPresenter
    private lateinit var auth: FirebaseAuth
    lateinit var data: Inviter
    private var loadingCount = 4
    private lateinit var loadingTimer : CountDownTimer
    private lateinit var typeface: Typeface
    var energy = 0
    var energyLimit = 100
    var point = 0
    private var remainTime = 0
    private var counted = 0
    var position = 0
    var currentRank = "Toddler"
    var levelUp = false
    private var diff: Long = 0
    private var checkUpdateEnergy = false
    lateinit var editor: SharedPreferences.Editor
    private lateinit var popupWindow : PopupWindow
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)
    private val items : MutableList<ChooseGame> = mutableListOf()
    private val taskList : MutableList<String> = mutableListOf()
    private val taskProgressList : MutableList<String> = mutableListOf()
    private val availableGameList : MutableList<Boolean> = mutableListOf()
    private val rankDetailItems : MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rank)

        supportActionBar?.hide()

        typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)!!
        tvRank.typeface = typeface
        tvPoint.typeface = typeface
        tvEnergy.typeface = typeface
        tvTotalScore.typeface = typeface

        val clickAnimation = AlphaAnimation(1.2F,0.6F)

        loadingTimer()

        ivTask.onClick {
            ivTask.startAnimation(clickAnimation)
            popUpTask()
        }

        layout_rank_detail.onClick {
            popUpRankDetail()
        }

        layout_point_energy.onClick {
            startActiv