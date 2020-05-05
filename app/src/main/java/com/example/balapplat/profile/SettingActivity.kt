package com.example.balapplat.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.balapplat.R
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    private lateinit var adapter: SettingRecyclerViewAdapter
    val settingItems : MutableList<Setting> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        supportActionBar?.hide()
        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvSettingTitle.typeface = typeface
        tvSettingJoinUs.typeface = typeface
        tvSettingFacebook.typeface = typeface

        settingItems.add(Setting("Review Us!",""))
        settingItems.add(Setting("Privacy Policy",""))
        settingItems.add(Setting("About Us",""))
        settingItems.add(Setting("Version",""))

        adapter = SettingRecyclerViewAdapter(this,settingItems)
        rvSetting.layoutManager = LinearLayoutManager(this)
        rvSetting.adapter = adapter
    }
}

data class Setting(
        var title: String? = "",
        var desc: String? = ""
)
