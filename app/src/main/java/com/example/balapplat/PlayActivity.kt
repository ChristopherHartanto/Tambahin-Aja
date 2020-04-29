

package com.example.balapplat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.balapplat.utils.UtilsConstants
import com.example.balapplat.utils.showSnackBar
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.activity_play.*

class PlayActivity : AppCompatActivity(), NetworkConnectivityListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(
                        activity_play,
                        "Connection Established",
                        UtilsConstants.SNACKBAR_LONG
                    ).show()
                } else {
                    showSnackBar(
                        activity_play,
                        "No Network !",
                        UtilsConstants.SNACKBAR_INFINITE
                    ).show()
                }
            }
        }
    }

}
