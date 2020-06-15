package com.ta.tambahinaja

import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.pop_up_new_updates.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx

class NewUpdateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pop_up_new_updates)

        supportActionBar!!.hide()
        val typeface : Typeface? = ResourcesCompat.getFont(this, R.font.fredokaone_regular)

        tvUpdateTitle.typeface = typeface
        tvUpdateVersion.typeface = typeface
        tvUpdateDesc.typeface = typeface

        tvUpdateVersion.text = "V ${packageManager.getPackageInfo(packageName,0).versionName}"

        btnUpdateClose.onClick {
            finish()
        }
    }
}
