package com.ta.tambahinaja

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class TutorialImageSliderAdapter(private val context: Context) : PagerAdapter() {
    private var inflater: LayoutInflater? = null
    private val images = arrayOf(R.drawable.logo_transparent, R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher)

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return images.count()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val view : View = inflater!!.inflate(R.layout.tutorial_image_slider_image, null)

        val typeface = ResourcesCompat.getFont(context, R.font.fredokaone_regular)!!
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val title = view.findViewById<TextView>(R.id.tvTutorialTitle)
        val info = view.findViewById<TextView>(R.id.tvTutorialInfo)

        title.typeface = typeface
        info.typeface = typeface
        
        when(position){
            0->{
                imageView.layoutParams.height = imageView.width
                title.text = "Welcome"
                info.text = "Welcome to Tambahin Aja, a fun challenging game which enhance your brain’s cognitive skills. Let’s find out!"
            }
            1->{
                title.text = "Rank"
                info.text = "You will have different privileges based on your current rank status. Finish the task and get more rewards!"
            }
            2->{
                title.text = "Reward"
                info.text = "You will receive rewards each time you finish a game. Use coins to unlock new games whereas credits to redeem with phone credit(pulsa)!"
            }
            3->{
                title.text = "Leaderboards"
                info.text = "The accumulated points you gathered in rank mode. The leader boards will  reset every month and the highest rank will receive a reward!"
            }
            4->{
                title.text = "Tournament"
                info.text = "Join Tournament to beats all opponents and get rewards for top 3 winners!"
            }
        }

        imageView.setImageResource(images[position])

        val viewPager = container as ViewPager
        viewPager.addView(view, 0)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val viewPager = container as ViewPager
        val view = `object` as View
        viewPager.removeView(view)
    }
}