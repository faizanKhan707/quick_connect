package com.example.quickconnect.broadcast

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quickconnect.MainActivity
import com.example.quickconnect.R
import com.example.quickconnect.adapter.myAdapter_peers

class my_broadcast(button: Button,var manager: WifiP2pManager,var channel:WifiP2pManager.Channel,var mainActivity: MainActivity): BroadcastReceiver() {

    var button : Button
    var manager_: WifiP2pManager
    var channel_:WifiP2pManager.Channel
    var mainActivity_:MainActivity

    init {
        this.button = button
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
                        button.text="WIFI ON"

                        button.setBackgroundResource(R.color.green)
                    }
                    else{
                        button.text="WIFI OFF"
                        button.setBackgroundResource(R.color.red)

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


                }

                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
//                    Toast.makeText(context,"WIFI_P2P_THIS_DEVICE_CHANGED_ACTION",Toast.LENGTH_LONG).show()

                }

            }
        }
    }
}