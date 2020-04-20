package com.example.balapplat.play

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AnimationUtils
import com.example.balapplat.R
import kotlinx.android.synthetic.main.activity_countdown.*
import kotlinx.android.synthetic.main.activity_waiting.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.ctx
import org.jetbrains.anko.intentFor

class WaitingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)

        var count = 3

        val timer = object: CountDownTimer(40000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (count == 3)
                    tvWaiting.text = "Waiting ."
                else if (count == 2)
                    tvWaiting.text = "Waiting . ."
                else if (count == 1)
                    tvWaiting.text = "Waiting . . ."
                else
                    count = 4
                count--
            }

            override fun onFinish() {
                finish()
                startActivity(intentFor<NormalGameActivity>().clearTask())
            }
        }
        timer.start()
    }
}
