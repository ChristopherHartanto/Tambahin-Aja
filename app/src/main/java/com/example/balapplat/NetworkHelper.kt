package com.example.balapplat

import android.content.Context
import android.net.ConnectivityManager

enum class Connection {
    CONNECTED, DISCONNECTED
}

fun Context.checkInternetConnection(): Connection {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    return when (connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting()) {
        true -> Connection.CONNECTED
        else -> Connection.DISCONNECTED
    }
}