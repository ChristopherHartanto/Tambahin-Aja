package com.example.balapplat.play

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.balapplat.R
import com.example.balapplat.utils.UtilsConstants
import com.example.balapplat.utils.showSnackBar
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.activity_odd_even_game.*

class OddEvenGameActivity : AppCompatActivity(), NetworkConnectivityListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_odd_even_game)
    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(
                        activity_odd_even_game,
                        "Connection Established",
                        UtilsConstants.SNACKBAR_LONG
                    ).show()
                } else {
                    showSnackBar(
                        activity_odd_even_game,
                        "No Network !",
                        UtilsConstants.SNACKBAR_INFINITE
                    ).show()
                }
            }
        }
    }
}
