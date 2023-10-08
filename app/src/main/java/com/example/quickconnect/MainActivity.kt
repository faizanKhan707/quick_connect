package com.example.quickconnect

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.net.wifi.p2p.WifiP2pManager
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.quickconnect.R.color.green
import com.example.quickconnect.adapter.myAdapter_msg
import com.example.quickconnect.adapter.myAdapter_peers
import com.example.quickconnect.broadcast.my_broadcast


class MainActivity : AppCompatActivity() {




    private val intentFilter = IntentFilter()
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var manager: WifiP2pManager
    lateinit var mReciver:BroadcastReceiver

    private var peers:List<WifiP2pDevice> = mutableListOf<WifiP2pDevice>()
    var deviceName:List<String> = ArrayList<String>()
    var deviceArray:List<WifiP2pDevice> = ArrayList<WifiP2pDevice>()
    private val REQUEST_CODE = 1

    var list:List<WifiP2pDevice> = ArrayList<WifiP2pDevice>()

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String  >>
    private var isLocationPermissionGranted=false
    private var isCoarsePermissionGranted=false
    lateinit var rcv_peer:RecyclerView



    private fun requestPermission(){
        isLocationPermissionGranted=ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED

        val permissionRequest:MutableList<String> =ArrayList()

        if(!isLocationPermissionGranted)
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)

        if(!isCoarsePermissionGranted)
            permissionRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        if(permissionRequest.isNotEmpty()){
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }

    }







    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var rcv_peer: RecyclerView =findViewById<RecyclerView>(R.id.rcv_peers)
        val rcv_msg=findViewById<RecyclerView>(R.id.rcv_msg)
        val wifi_button:Button=findViewById<Button>(R.id.wifi_button)
        val discover:Button=findViewById<Button>(R.id.discover_peer)
        rcv_peer= findViewById<RecyclerView>(R.id.rcv_peers)
        var dummyList= mutableListOf<String>()
        var dummyList2= mutableListOf<String>()




        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permission ->
            isLocationPermissionGranted=permission[Manifest.permission.ACCESS_FINE_LOCATION]?:isLocationPermissionGranted
            isCoarsePermissionGranted=permission[Manifest.permission.ACCESS_COARSE_LOCATION]?:isCoarsePermissionGranted

        }
        requestPermission()

        for(i in 1..100) {
            dummyList.add("Peer $i")
            dummyList2.add("Message $i")
        }

        rcv_peer.adapter=myAdapter_peers(peers)
        rcv_peer.layoutManager=LinearLayoutManager(this)
        rcv_peer.setHasFixedSize(true)

        rcv_msg.adapter=myAdapter_msg(dummyList2)
        rcv_msg.layoutManager=LinearLayoutManager(this)
        rcv_msg.setHasFixedSize(true)


        Toast.makeText(this,"${isCoarsePermissionGranted} ${isLocationPermissionGranted}",Toast.LENGTH_LONG).show()
        val wifi=applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        if(wifi.isWifiEnabled){
            wifi_button.text="WIFI ON"
            wifi_button.setBackgroundResource(R.color.green)
        }

        wifi_button.setOnClickListener(){
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        // Broad cast receiver
        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)


        // Indicates a change in the Wi-Fi Direct status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)

        // Indicates the state of Wi-Fi Direct connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        mReciver=my_broadcast(wifi_button,manager,channel,this)

        //discover button


        val discover_status:TextView=findViewById(R.id.discover_status)

        discover.setOnClickListener{
            Toast.makeText(this,"Discover",Toast.LENGTH_LONG).show()



            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                requestPermission()
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        // Discovery succeeded!
                        discover_status.text="Discovey Started"
                        discover_status.setBackgroundResource(R.color.green)
                        Toast.makeText(applicationContext,"Discover success",Toast.LENGTH_SHORT).show()

                    }

                    override fun onFailure(reason: Int) {
                        // Discovery failed!
                        discover_status.text="Not Connected"
                        discover_status.setBackgroundResource(R.color.red)
                        Toast.makeText(applicationContext,"Discover failure",Toast.LENGTH_SHORT).show()

                    }
                })

        }



    }



    fun updatePeers(wifiP2pDeviceList: WifiP2pDeviceList){
        Toast.makeText(this, "${wifiP2pDeviceList.deviceList.size}  ${wifiP2pDeviceList.deviceList.toString()}", Toast.LENGTH_SHORT).show()
        if (wifiP2pDeviceList != peers){
            var k= mutableListOf<WifiP2pDevice>()
            for( i in wifiP2pDeviceList.deviceList){
                k.add(i)
            }

            peers=k
            rcv_peer=findViewById(R.id.rcv_peers)
            rcv_peer.adapter=myAdapter_peers(peers)
            rcv_peer.layoutManager=LinearLayoutManager(this)
            rcv_peer.setHasFixedSize(true)

        }
    }

    protected override fun onResume():Unit{
        super.onResume()
        registerReceiver(mReciver,intentFilter)
    }

    protected override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReciver)
    }


}