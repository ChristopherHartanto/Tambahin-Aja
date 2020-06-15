package com.ta.tambahinaja.play.practice

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.transition.TransitionManager
import com.facebook.Profile
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.ta.tambahinaja.R
import com.ta.tambahinaja.SignUpActivity
import com.ta.tambahinaja.friends.Message
import com.ta.tambahinaja.home.MarketActivity
import com.ta.tambahinaja.model.User
import com.ta.tambahinaja.presenter.PracticePresenter
import com.ta.tambahinaja.presenter.RankPresenter
import com.ta.tambahinaja.rank.Balance
import com.ta.tambahinaja.utils.getFacebookProfilePicture
import com.ta.tambahinaja.view.MainView
import kotlinx.android.synthetic.main.activity_practice.*
import kotlinx.android.synthetic.main.activity_rank.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast

class PracticeActivity : AppCompatActivity(), MainView {

    private lateinit var presenter: PracticePresenter
    private var practiceItems: MutableList<Practice> = mutableListOf()
    private lateinit var practiceAdapter: PracticeRecyclerViewAdapter
    private lateinit var typeface: Typeface
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var coin = 0
    private var credit = 0
    private var creditEarnAdvanced = 0
    private var currentLevel = 0
    private var level = 0 // level yang akan di main
    private lateinit var popupWindow : PopupWindow
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice)
        supportActionBar!!.hide()

        typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)!!
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        presenter = PracticePresenter(this,database)

        practiceAdapter = PracticeRecyclerViewAdapter(this,practiceItems,currentLevel){
            if (coin < practiceItems[it].level!!)
                popUpMessage(Message.ReadOnly,"Not Enough Coins")
            else if (currentLevel > practiceItems[it].level!!)
                popUpMessage(Message.Reply,"You will Not Earn any Prize")
            else if (currentLevel < practiceItems[it].level!!)
                popUpMessage(Message.ReadOnly,"Must Reach to that Level First")
            else{
                creditEarnAdvanced = practiceItems[it].reward!!
                level = practiceItems[it].level!!
                presenter.updateCoin(coin - practiceItems[it].price!!)
            }
        }

        tvPracticeMainTitle.typeface = typeface
        tvPracticeName.typeface = typeface
        tvPracticeCoin.typeface = typeface
        tvPracticeCredit.typeface = typeface

        rvPractice.apply {
            layoutManager = StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL)
            adapter = practiceAdapter
        }

//        cvPracticeBalance.onClick {
//            startActivity(intentFor<MarketActivity>())
//        }

        loadPracticeItems()
    }

    override fun onStart() {
        if (auth.currentUser != null && Profile.getCurrentProfile() != null){
            presenter.fetchProfile()
            presenter.fetchBalance()
            presenter.fetchLevel()
        }
        super.onStart()
    }

    private fun popUpMessage(type: Message,message: String){
        val inflater: LayoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.pop_up_message,null)

        // Initialize a new instance of popup window
        popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }
        val typeface : Typeface? = ResourcesCompat.getFont(ctx, R.font.fredokaone_regular)

        val layoutMessageInvitation = view.findViewById<LinearLayout>(R.id.layout_message_invitation)
        val layoutMessageBasic = view.findViewById<LinearLayout>(R.id.layout_message_basic)
        val layoutMessageReward = view.findViewById<LinearLayout>(R.id.layout_message_reward)
        val tvMessageInfo = view.findViewById<TextView>(R.id.tvMessageInfo)
        val btnClose = view.findViewById<Button>(R.id.btnMessageClose)
        val btnReject = view.findViewById<Button>(R.id.btnMessageReject)
        val tvMessageTitle = view.findViewById<TextView>(R.id.tvMessageTitle)

        if (type == Message.Reply){
            layoutMessageInvitation.visibility = View.GONE
            layoutMessageBasic.visibility = View.VISIBLE
            layoutMessageReward.visibility = View.GONE

            btnReject.text = "No"
            tvMessageInfo.text = message

            btnClose.onClick {
                btnClose.startAnimation(clickAnimation)
                activity_practice.alpha = 1F
                popupWindow.dismiss()
            }

            btnReject.onClick {
                btnReject.startAnimation(clickAnimation)
                activity_practice.alpha = 1F
                popupWindow.dismiss()
            }

        }
        else if(type == Message.ReadOnly){
            layoutMessageInvitation.visibility = View.GONE
            layoutMessageBasic.visibility = View.VISIBLE
            layoutMessageReward.visibility = View.GONE

            btnReject.visibility = View.GONE
            tvMessageInfo.typeface = typeface

            tvMessageInfo.text = message

            btnClose.onClick {

                btnClose.startAnimation(clickAnimation)
                activity_practice.alpha = 1F
                popupWindow.dismiss()
            }
        }

        tvMessageTitle.typeface = typeface

        activity_practice.alpha = 0.1F

        TransitionManager.beginDelayedTransition(activity_practice)
        popupWindow.showAtLocation(
                activity_practice, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
        )

    }
    private fun loadPracticeItems(){
        practiceItems.add(Practice(1,20,50))
        practiceItems.add(Practice(2,25,50))
        practiceItems.add(Practice(3,25,55))
        practiceItems.add(Practice(4,25,55))
        practiceItems.add(Practice(5,25,55))
        practiceItems.add(Practice(6,30,60))
        practiceItems.add(Practice(7,30,60))
        practiceItems.add(Practice(8,30,60))
        practiceItems.add(Practice(9,30,65))
        practiceItems.add(Practice(10,30,65))
        practiceItems.add(Practice(11,35,65))
        practiceItems.add(Practice(12,35,65))
        practiceItems.add(Practice(13,35,70))
        practiceItems.add(Practice(14,35,70))
        practiceItems.add(Practice(15,35,70))
        practiceItems.add(Practice(16,40,75))
        practiceItems.add(Practice(17,40,75))
        practiceItems.add(Practice(18,40,75))
        practiceItems.add(Practice(19,40,80))
        practiceItems.add(Practice(20,45,80))
        practiceItems.add(Practice(21,45,80))
        practiceItems.add(Practice(22,45,80))
        practiceItems.add(Practice(23,45,85))
        practiceItems.add(Practice(24,50,85))
        practiceItems.add(Practice(25,50,85))
        practiceItems.add(Practice(26,50,90))
        practiceItems.add(Practice(27,55,90))
        practiceItems.add(Practice(28,55,90))
        practiceItems.add(Practice(29,55,100))
        practiceItems.add(Practice(30,60,100))

        practiceAdapter.notifyDataSetChanged()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == "fetchProfile"){
            Picasso.get().load(getFacebookProfilePicture(Profile.getCurrentProfile().id)).fit().into(ivPracticeProfile)
            tvPracticeName.text = dataSnapshot.getValue(User::class.java)!!.name
        }else if(response == "fetchBalance"){
            coin = dataSnapshot.getValue(Balance::class.java)!!.point!!.toInt()
            credit = dataSnapshot.getValue(Balance::class.java)!!.credit!!.toInt()

            tvPracticeCoin.text = coin.toString()
            tvPracticeCredit.text = credit.toString()
        }else if(response == "fetchLevel"){
            if (dataSnapshot.exists())
                currentLevel = dataSnapshot.value.toString().toInt()
            else
                currentLevel = 1

            practiceAdapter.notifyDataSetChanged()
        }
    }

    override fun response(message: String) {
        if (message == "updateCoin"){
            startActivity(intentFor<PrePracticePlayActivity>("level" to level, "reward" to creditEarnAdvanced))
            finish()
        }
    }
}

data class Practice(
    var level: Int? = 0,
    var price: Int? = 0,
    var reward: Int? = 0
)