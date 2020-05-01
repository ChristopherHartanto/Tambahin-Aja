package com.example.balapplat.play

import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.balapplat.R
import com.example.balapplat.utils.showSnackBar
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.activity_countdown.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.intentFor

class CountdownActivity : AppCompatActivity(), NetworkConnectivityListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown)

        supportActionBar?.hide()

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

                if (!intent.extras!!.getString("mode").equals("single")){
                    if(intent.extras!!.getString("facebookId").equals(null)){
                        if (intent.extras!!.getString("inviterFacebookId") != null){
                            val inviterFacebookId = intent.extras!!.getString("inviterFacebookId")
                            val inviterName = intent.extras!!.getString("inviterName")
                            startActivity(intentFor<NormalGameActivity>("inviterFacebookId" to inviterFacebookId, "inviterName" to inviterName))
                        }
                        else{
                            startActivity(intentFor<NormalGameActivity>("facebookId" to intent.extras!!.getString("facebookId"),
                                "name" to intent.extras!!.getString("name")
                                ,"playOnline" to intent.extras!!.getString("playOnline")
                                ,"creator" to intent.extras!!.getString("creator")))
                        }
                    }
                   else
                        startActivity(intentFor<NormalGameActivity>("facebookId" to intent.extras!!.getString("facebookId"),
                            "name" to intent.extras!!.getString("name")))
                }else
                    startActivity(intentFor<NormalGameActivity>("type" to intent.extras!!.getString("type"),
                        "mode" to intent.extras!!.getString("mode"),"rank" to intent.extras!!.getBoolean("rank")))
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
