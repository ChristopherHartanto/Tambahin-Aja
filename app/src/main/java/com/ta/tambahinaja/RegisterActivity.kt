package com.ta.tambahinaja

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ta.tambahinaja.utils.showSnackBar
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity(), NetworkConnectivityListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(activity_register, "The network is back !", "LONG")
                } else {
                    showSnackBar(activity_register, "There is no more network", "INFINITE")
                }
            }
        }
    }
}
