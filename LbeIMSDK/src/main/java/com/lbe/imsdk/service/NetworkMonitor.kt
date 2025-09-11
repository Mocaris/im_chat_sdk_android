package com.lbe.imsdk.service

import android.*
import android.content.*
import android.net.*
import android.os.*
import androidx.annotation.*
import kotlinx.coroutines.flow.*
import java.io.Closeable

class NetworkMonitor(context: Context) : Closeable {
    private val connectivityManager by lazy { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

    val isConnected = MutableStateFlow(false)

    init {
        isConnected.value = connectivityManager.isDefaultNetworkActive
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            isConnected.value = true
        }

        override fun onUnavailable() {
            isConnected.value = false
        }

        override fun onLost(network: Network) {
            isConnected.value = false
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            isConnected.value = false
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun startMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder().apply {
                    addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                }.build(),
                networkCallback
            )
        }
    }

    fun stopMonitoring() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun close() {
        stopMonitoring()
    }
}
