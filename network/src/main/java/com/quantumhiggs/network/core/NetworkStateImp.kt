package com.quantumhiggs.network.core

import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkEvents
import com.quantumhiggs.network.NetworkState

/**
 * This is a static implementation of NetworkState, it holds the network states and is editable but it's only usable from this file.
 */
internal class NetworkStateImp : NetworkState {

    override var isConnected: Boolean = false
        set(value) {
            field = value
            NetworkEvents.notify(
                Event.ConnectivityEvent(
                    this
                )
            )
        }

    override var network: Network? = null

    override var linkProperties: LinkProperties? = null
        set(value) {
            val old = field
            field = value
            NetworkEvents.notify(
                Event.LinkPropertyChangeEvent(
                    this,
                    old
                )
            )
        }

    override var networkCapabilities: NetworkCapabilities? = null
        set(value) {
            val old = field
            field = value
            NetworkEvents.notify(
                Event.NetworkCapabilityEvent(
                    this,
                    old
                )
            )

        }
}