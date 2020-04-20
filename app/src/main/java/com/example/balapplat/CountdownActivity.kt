package com.example.balapplat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AnimationUtils
import com.example.balapplat.play.NormalGameActivity
import kotlinx.android.synthetic.main.activity_countdown.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.ctx
import org.jetbrains.anko.intentFor

class CountdownActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown)

        supportActionBar?.hide()

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
                startActivity(intentFor<NormalGameActivity>().clearTask())
            }
        }
        timer.start()
    }

}
