package com.example.balapplat.play

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.balapplat.R
import com.example.balapplat.main.MainActivity
import kotlinx.android.synthetic.main.activity_post_game.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class PostGameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_game)

        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvPostGameTitle.typeface = typeface
        tvGetEnergy.typeface = typeface
        tvBackMenu.typeface = typeface
        tvGetPoint.typeface = typeface
        tvSinglePoint.typeface = typeface
        tvOpponentName.typeface = typeface
        tvPlayerName.typeface = typeface
        tvPlayerPoint.typeface = typeface
        tvOpponentPoint.typeface = typeface

        tvBackMenu.onClick {
            startActivity<MainActivity>()
        }
    }
}
