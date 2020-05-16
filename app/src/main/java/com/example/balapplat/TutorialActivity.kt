package com.example.balapplat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_tutorial.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class TutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        supportActionBar?.hide()

        btnExitTutorial.onClick {
            finish()
        }

        val sliderDotsPanel = findViewById<LinearLayout>(R.id.sliderDots)
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        val viewPagerAdapter = TutorialImageSliderAdapter(this)
        viewPager.adapter = viewPagerAdapter
        val dotsCount = viewPagerAdapter.count
        var dots: Array<ImageView?> = arrayOfNulls(dotsCount)
        for(i in 0 until dotsCount){
            dots[i] = ImageView(this)
            dots[i]?.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.tutorial_image_slider_non_active_dot))
            val params : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(8,0,8,0)
            sliderDotsPanel.addView(dots[i], params)
        }
        dots[0]?.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.tutorial_image_slider_active_dot))
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                for (i in 0 until dotsCount){
                    dots[i]?.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.tutorial_image_slider_non_active_dot))
                }
                dots[position]?.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.tutorial_image_slider_active_dot))
            }

        })
    }
}
