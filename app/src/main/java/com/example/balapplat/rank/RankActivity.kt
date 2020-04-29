package com.example.balapplat.rank

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.balapplat.CountdownActivity
import com.example.balapplat.R
import com.example.balapplat.utils.UtilsConstants
import com.example.balapplat.utils.showSnackBar
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.activity_rank.*
import org.jetbrains.anko.startActivity

class RankActivity : AppCompatActivity(), NetworkConnectivityListener {


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

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(
                        activity_rank,
                        "Connection Established",
                        UtilsConstants.SNACKBAR_LONG
                    ).show()
                } else {
                    showSnackBar(
                        activity_rank,
                        "No Network !",
                        UtilsConstants.SNACKBAR_INFINITE
                    ).show()
                }
            }
        }
    }
}
