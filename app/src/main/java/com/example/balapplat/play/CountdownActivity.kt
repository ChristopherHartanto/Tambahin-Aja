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

                val mode = intent.extras!!.getString("mode")
                val faceBookId = intent.extras!!.getString("facebookId")
                val inviterFacebookId = intent.extras!!.getString("inviterFacebookId")
                val inviterName = intent.extras!!.getString("inviterName")
                val type = intent.extras!!.getString("type")
                val name = intent.extras!!.getString("name")
                val rank = intent.extras!!.getBoolean("rank")
                val playOnline = intent.extras!!.getBoolean("playOnline")
                val creator = intent.extras!!.getBoolean("creator")

                if (!mode.equals("single")){
                    if(faceBookId.equals(null)){
                        if (inviterFacebookId != null){
                            startActivity(intentFor<NormalGameActivity>("inviterFacebookId" to inviterFacebookId, "inviterName" to inviterName))
                        }
                        else{
                            startActivity(intentFor<NormalGameActivity>("facebookId" to faceBookId,
                                "name" to name
                                ,"playOnline" to playOnline
                                ,"creator" to creator))
                        }
                    }
                    else
                        startActivity(intentFor<NormalGameActivity>("facebookId" to faceBookId,
                            "name" to name))
                }else
                    startActivity(intentFor<NormalGameActivity>("type" to type,
                        "mode" to mode,"rank" to rank))
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