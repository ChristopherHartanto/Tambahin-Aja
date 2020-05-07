package com.example.balapplat.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.balapplat.R
import com.example.balapplat.main.LoginActivity
import kotlinx.android.synthetic.main.activity_setting.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

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

        btnLogOut.onClick {
            startActivity<LoginActivity>()
            finish()
        }
    }
}

data class Setting(
        var title: String? = "",
        var desc: String? = ""
)
