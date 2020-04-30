package com.quantumhiggs.network.core

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import com.quantumhiggs.network.NetworkEvents
import com.quantumhiggs.network.NetworkState

internal object Constants {
    const val ID_KEY = "network.monitoring.previousState"
}

internal fun NetworkConnectivityListener.onListenerCreated() {

    NetworkEvents.observe(this as LifecycleOwner, Observer {
        if (previousState != null)
            networkConnectivityChanged(it)
    })

}

internal fun NetworkConnectivityListener.onListenerResume(networkState: NetworkState) {
    if (!shouldBeCalled || !checkOnResume) return

    val previousState = previousState
    val isConnected = networkState.isConnected

    this.previousState = isConnected

    val connectionLost = (previousState == null || previousState == true) && !isConnected
    val connectionBack = previousState == false && isConnected

    if (connectionLost || connectionBack) {
        networkConnectivityChanged(Event.ConnectivityEvent(networkState))
    }

}

/**
 * This property serves as a flag to detect if this activity lost network
 */
internal var NetworkConnectivityListener.previousState: Boolean?
    get() {
        return when (this) {
            is Fragment -> this.arguments?.previousState
            is Activity -> this.intent.extras?.previousState
            else -> null
        }
    }
    set(value) {
        when (this) {
            is Fragment -> {
                val a = this.arguments ?: android.os.Bundle()
                a.previousState = value
                this.arguments = a
            }
            is Activity -> {
                val a = this.intent.extras ?: android.os.Bundle()
                a.previousState = value
                this.intent.replaceExtras(a)
            }
        }
    }
internal var Bundle.previousState: Boolean?
    get() = when (getInt(Constants.ID_KEY, -1)) {
        -1 -> null
        0 -> false
        else -> true
    }
    set(value) {
        putInt(Constants.ID_KEY, if (value == true) 1 else 0)
    }