package com.ta.tambahinaja.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ta.tambahinaja.R
import com.ta.tambahinaja.utils.showSnackBar
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.fragment_no_friend.*

/**
 * A simple [Fragment] subclass.
 */
class NoFriendFragment : Fragment(), NetworkConnectivityListener {

    private var mLayout = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mLayout = savedInstanceState?.getInt("layoutId") ?: R.layout.fragment_no_friend
        return inflater.inflate(mLayout, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("layoutId",mLayout)
        super.onSaveInstanceState(outState)
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
