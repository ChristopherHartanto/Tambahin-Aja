package com.example.balapplat

import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.balapplat.play.NormalGameActivity
import com.example.balapplat.utils.UtilsConstants
import com.example.balapplat.utils.showSnackBar
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import com.quantumhiggs.network.NetworkConstants
import kotlinx.android.synthetic.main.activity_countdown.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity

class CountdownActivity : AppCompatActivity(), NetworkConnectivityListener {

    private var prevState = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown)

        supportActionBar?.hide()

        savedInstanceState?.let {
            prevState = it.getBoolean(NetworkConstants.LOST_CONNECTION)
        }

        countDown(true)
    }

    private fun countDown(status: Boolean) {
        var count = 3

        val timer = object: CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvCountdown.text = count.toString()
                val animationSlideDown = AnimationUtils.loadAnimation(ctx, R.anim.slide_down)
                tvCountdown.startAnimation(animationSlideDown)
                count--
            }

            override fun onFinish() {
                finish()

                if (intent.extras != null){
                    if(intent.extras!!.getString("facebookId").equals(null)){
                        if (intent.extras!!.getString("status").equals("player2")){
                            startActivity(intentFor<NormalGameActivity>("status" to "player2"))
                        }
                    }
                   else
                        startActivity(intentFor<NormalGameActivity>("facebookId" to intent.extras!!.getString("facebookId")))
                }else
                    startActivity<NormalGameActivity>()
                }
        }
        if (status)
            timer.start()
        else
            timer.cancel()
    }

    override fun onDestroy() {
        countDown(false)
        super.onDestroy()
    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(
                        activity_countdown,
                        "Connection Established",
                        UtilsConstants.SNACKBAR_LONG
                    ).show()
                } else {
                    showSnackBar(
                        activity_countdown,
                        "No Network !",
                        UtilsConstants.SNACKBAR_INFINITE
                    ).show()
                }
            }
        }
    }

}
