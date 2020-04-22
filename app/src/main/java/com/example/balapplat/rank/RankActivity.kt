package com.example.balapplat.rank

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.balapplat.CountdownActivity
import com.example.balapplat.R
import com.example.balapplat.friends.FriendsRecyclerViewAdapter
import com.example.balapplat.model.NormalMatch
import com.example.balapplat.play.NormalGameActivity
import com.example.balapplat.play.WaitingActivity
import kotlinx.android.synthetic.main.activity_rank.*
import kotlinx.android.synthetic.main.fragment_list_friends.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.ctx

class RankActivity : AppCompatActivity() {


    private lateinit var adapter: RankRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rank)

        supportActionBar?.hide()

        val items : MutableList<String> = mutableListOf("Normal", "Odd Even", "Rush", "AlphaNum")
        adapter = RankRecyclerViewAdapter(this,items){
            finish()
            startActivity<CountdownActivity>()
        }

        rvRank.layoutManager = GridLayoutManager(this,2)
        rvRank.adapter = adapter
    }
}
