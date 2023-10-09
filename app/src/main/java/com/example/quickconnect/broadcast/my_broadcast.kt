package com.example.quickconnect.broadcast

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.example.quickconnect.MainActivity
import com.example.quickconnect.R

@Suppress("DEPRECATION")
class my_broadcast(var manager: WifiP2pManager, var channel:WifiP2pManager.Channel, var mainActivity: MainActivity): BroadcastReceiver() {

    var manager_: WifiP2pManager
    var channel_:WifiP2pManager.Channel
    var mainActivity_:MainActivity

    init {
        manager_=manager
        channel_=channel
        mainActivity_=mainActivity
    }



    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
//        Toast.makeText(context,intent?.action.toString(),Toast.LENGTH_LONG).show()


        if (intent != null) {
            when(intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    // Determine if Wi-Fi Direct mode is enabled or not, alert
                    // the Activity.
                    if(intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1)==WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                        mainActivity_.wifi_button.text="WIFI ON"

                        mainActivity_.wifi_button.setBackgroundResource(R.color.green)
                    }
                    else{
                        mainActivity_.wifi_button.text="WIFI OFF"
                        mainActivity_.wifi_button.setBackgroundResource(R.color.red)

                    }

                }

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    if(manager_!=null){

                        val peer=manager_.requestPeers(channel_,WifiP2pManager.PeerListListener(){
                            mainActivity_.updatePeers(it)

                        })
                        Toast.makeText(context,"peer changed  ${peer.toString()}",Toast.LENGTH_SHORT).show()



                    }
                    // The peer list has changed! We should probably do something about
                    // that.
//                    Toast.makeText(context,"The peer list has changed! We should probably do something about",Toast.LENGTH_LONG).show()


                }

                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {

                    // Connection state changed! We should probably do something about
                    // that.
//                    Toast.makeText(context,"Connection state changed! We should probably do something about",Toast.LENGTH_LONG).show()
                        if(manager_!=null){
                            if(context?.let { this.isNetworkAvailable(it) } == true){
                                manager_.requestConnectionInfo(channel_,mainActivity_.connectionInfoListener)
                            }
                            else{
                                mainActivity_.discover_status.text="Not Connected"
                            }
                        }

                }

                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
//                    Toast.makeText(context,"WIFI_P2P_THIS_DEVICE_CHANGED_ACTION",Toast.LENGTH_LONG).show()

                }

            }
        }
    }
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw      = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }


}