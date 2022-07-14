package ro.westaco.carhome

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, p1: Intent?) {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
            val lbm1 = context.let { LocalBroadcastManager.getInstance(it) }
            val localIn1 = Intent("NETWORK")
            lbm1.sendBroadcast(localIn1)
        } else {
            val lbm1 = context.let { LocalBroadcastManager.getInstance(it) }
            val localIn1 = Intent("NETWORK")
            lbm1.sendBroadcast(localIn1)
        }
    }
}