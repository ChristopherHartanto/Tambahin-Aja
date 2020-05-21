package com.ta.tambahinaja

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_tutorial.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class TutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        supportActionBar?.hide()

        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)!!
        btnExitTutorial.typeface = typeface

        val sliderDotsPanel = findViewById<LinearLayout>(R.id.sliderDots)
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        val viewPagerAdapter = TutorialImageSliderAdapter(this)
        viewPager.adapter = viewPagerAdapter
        val dotsCount = viewPagerAdapter.count
        val dots: Array<ImageView?> = arrayOfNulls(dotsCount)

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

                if (position != viewPagerAdapter.count - 1)
                    btnExitTutorial.text = "Next"
                else
                    btnExitTutorial.text = "Done"

            }

        })

        btnExitTutorial.onClick {
            if (btnExitTutorial.text.toString() == "Next")
                viewPager.currentItem = viewPager.currentItem + 1
            else
                finish()
        }
    }
}
