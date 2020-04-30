package com.example.balapplat.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.balapplat.R
import com.example.balapplat.utils.showSnackBar
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.fragment_no_friend.*

/**
 * A simple [Fragment] subclass.
 */
class NoFriendFragment : Fragment(), NetworkConnectivityListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_no_friend, container, false)
    }

    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(fragment_no_friend, "The network is back !", "LONG")
                } else {
                    showSnackBar(fragment_no_friend, "There is no more network", "INFINITE")
                }
            }
        }
    }

}
