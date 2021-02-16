package com.greenhub.counter.backend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class NetworkChangeReceiver : BroadcastReceiver() {

    companion object{
       public val NETWORK_AVAILABLE_ACTION = "com.gdm.retailalfageek.NetworkAvailable"
       public val IS_NETWORK_AVAILABLE = "isNetworkAvailable"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val networkStateIntent = Intent(NETWORK_AVAILABLE_ACTION)
        networkStateIntent.putExtra(IS_NETWORK_AVAILABLE, isConnectedToInternet(context))
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(networkStateIntent)
        Log.e("Network Available ", "On receive called")
    }

    private fun isConnectedToInternet(context: Context?): Boolean {
        return try {
            if (context != null) {
                val connectivityManager =
                    context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = connectivityManager.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected
            }
            false
        } catch (e: Exception) {
            Log.e(NetworkChangeReceiver::class.java.getName(), e.message)
            false
        }
    }
}