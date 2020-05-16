package com.example.tambahinaja

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class TutorialImageSliderAdapter(private val context: Context) : PagerAdapter() {
    private var inflater: LayoutInflater? = null
    private val images = arrayOf(R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher)

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return images.count()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val view : View = inflater!!.inflate(R.layout.tutorial_image_slider_image, null)
        val imageView = view.findViewById<ImageView>(R.id.imageView)
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