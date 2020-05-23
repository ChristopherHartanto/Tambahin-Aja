package com.ta.tambahinaja

import android.app.Application
import com.quantumhiggs.network.NetworkStateHolder.registerConnectivityBroadcaster

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        registerConnectivityBroadcaster()
    }
}