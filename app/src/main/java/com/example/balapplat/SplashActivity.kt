package com.example.balapplat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import kotlinx.android.synthetic.main.activity_countdown.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()


        val timer = object: CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                finish()
                startActivity(intentFor<MainActivity>())
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
            }
        }
        timer.start()
    }
}
