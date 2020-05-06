package com.example.balapplat.play

import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.balapplat.R
import com.example.balapplat.utils.showSnackBar
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.activity_countdown.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast

@Suppress("NAME_SHADOWING")
class CountdownActivity : AppCompatActivity(), NetworkConnectivityListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown)

        supportActionBar?.hide()

        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvCountdown.typeface = typeface
        countDown(true)

    }

    fun countDown(status : Boolean){
        var count = 3

        val timer = object: CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvCountdown.text = count.toString()
                val animationSlideDown = AnimationUtils.loadAnimation(ctx,
                    R.anim.zoom_out
                )
                tvCountdown.startAnimation(animationSlideDown)
                count--
            }

            override fun onFinish() {
                finish()

                val status = intent.extras!!.getSerializable("status")
                var type = intent.extras!!.getSerializable("type")
                if (type == null)
                    type = GameType.Normal
                toast("status")
                when (status) {
                    StatusPlayer.JoinOnline -> {
                        val creatorFacebookId = intent.extras!!.getString("creatorFacebookId")
                        val creatorName = intent.extras!!.getString("creatorName")
                        startActivity(intentFor<NormalGameActivity>("creatorFacebookId" to creatorFacebookId, "creatorName" to creatorName,"status" to status, "type" to type))
                    }
                    StatusPlayer.Creator -> {
                        val joinOnlineFacebookId = intent.extras!!.getString("joinOnlineFacebookId")
                        val joinOnlineName = intent.extras!!.getString("joinOnlineName")
                        startActivity(intentFor<NormalGameActivity>("joinOnlineFacebookId" to joinOnlineFacebookId, "joinOnlineName" to joinOnlineName,"status" to status, "type" to type))
                    }
                    StatusPlayer.Inviter -> {
                        val joinFriendFacebookId = intent.extras!!.getString("joinFriendFacebookId")
                        val joinFriendName = intent.extras!!.getString("joinFriendName")
                        val timer = intent.extras!!.getInt("timer")
                        startActivity(intentFor<NormalGameActivity>("joinFriendFacebookId" to joinFriendFacebookId, "joinName" to joinFriendName,"status" to status, "type" to type, "timer" to timer))
                    }
                    StatusPlayer.JoinFriend -> {
                        val inviterFacebookId = intent.extras!!.getString("inviterFacebookId")
                        val inviterName = intent.extras!!.getString("inviterName")
                        val timer = intent.extras!!.getInt("timer")
                        startActivity(intentFor<NormalGameActivity>("inviterFacebookId" to inviterFacebookId, "inviterName" to inviterName,"status" to status, "type" to type, "timer" to timer))
                    }
                    StatusPlayer.Rank ->{
                        startActivity(intentFor<NormalGameActivity>("status" to status, "type" to type))
                    }
                    StatusPlayer.Single->{
                        val timer = intent.extras!!.getInt("timer")
                        startActivity(intentFor<NormalGameActivity>("status" to status, "type" to type, "timer" to timer))
                    }
                }
            }
        }
        if (status)
            timer.start()
        else
            timer.cancel()
    }

    override fun onBackPressed() {
//        countDown(false)
//        finish()
//        super.onBackPressed()
    }

    override fun onDestroy() {
        countDown(false)
        super.onDestroy()
    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(activity_countdown, "The network is back !", "LONG")
                } else {
                    showSnackBar(activity_countdown, "There is no more network", "INFINITE")
                }
            }
        }
    }

}
