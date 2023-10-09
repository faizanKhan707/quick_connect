package com.example.quickconnect

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.net.wifi.p2p.WifiP2pManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.BoringLayout
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quickconnect.adapter.myAdapter_msg
import com.example.quickconnect.adapter.myAdapter_peers
import com.example.quickconnect.broadcast.my_broadcast
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {




    private val intentFilter = IntentFilter()
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var manager: WifiP2pManager
    lateinit var mReciver:BroadcastReceiver

    private var peers:MutableList<WifiP2pDevice> = mutableListOf<WifiP2pDevice>()
    var deviceName:MutableList<String> = ArrayList<String>()
    var deviceArray:MutableList<WifiP2pDevice> = ArrayList<WifiP2pDevice>()

    var list:List<WifiP2pDevice> = ArrayList<WifiP2pDevice>()

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String  >>
    private var isLocationPermissionGranted=false
    private var isCoarsePermissionGranted=false
    lateinit var rcv_peer:RecyclerView
    lateinit var discover:Button
    lateinit var discover_status:TextView
    lateinit var wifi_button:Button
    lateinit var msg_List:MutableList<String>
    lateinit var rcv_msg:RecyclerView
    lateinit var sendButton:Button
    lateinit var msg_text:EditText



    lateinit var socket:Socket
    lateinit var serverClass: ServerClass
    lateinit var clientClass: ClientClass
    var isHost:Boolean=false

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

        rcv_peer=findViewById<RecyclerView>(R.id.rcv_peers)
        rcv_msg=findViewById<RecyclerView>(R.id.rcv_msg)
        wifi_button=findViewById<Button>(R.id.wifi_button)
        discover=findViewById<Button>(R.id.discover_peer)
        discover_status=findViewById(R.id.discover_status)
        rcv_peer= findViewById<RecyclerView>(R.id.rcv_peers)
        var dummyList= mutableListOf<String>()
        msg_List= mutableListOf<String>()
        sendButton=findViewById(R.id.send)
        msg_text=findViewById(R.id.msg_text)



        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permission ->
            isLocationPermissionGranted=permission[Manifest.permission.ACCESS_FINE_LOCATION]?:isLocationPermissionGranted
            isCoarsePermissionGranted=permission[Manifest.permission.ACCESS_COARSE_LOCATION]?:isCoarsePermissionGranted

        }
        requestPermission()

        rcv_peer.adapter=myAdapter_peers(peers)
        rcv_peer.layoutManager=LinearLayoutManager(this)
        rcv_peer.setHasFixedSize(true)

        rcv_msg.adapter=myAdapter_msg(msg_List)
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

        mReciver=my_broadcast(manager,channel,this)

        //discover button



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

        sendButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                var executor:ExecutorService=Executors.newSingleThreadExecutor()
                var msg:String=msg_text.text.toString()
                executor.execute(object : Runnable{
                    override fun run() {
                        if(msg!=null&&isHost){
                            serverClass.write(msg.toByteArray())
                        }
                        else if(msg!=null){
                            clientClass.write(msg.toByteArray())
                        }

                    }
                })
            }

        })



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
            var adapter=myAdapter_peers(peers)
            rcv_peer.adapter=adapter
            rcv_peer.layoutManager=LinearLayoutManager(this)
            rcv_peer.setHasFixedSize(true)
            val discover_status=findViewById<TextView>(R.id.discover_status)
            adapter.setOnItemClickListner(object : myAdapter_peers.onItemClickListner{
                override fun onItemClick(position: Int) {
                    Toast.makeText(applicationContext, "$position clicked", Toast.LENGTH_SHORT).show()
                    val wifiDevice:WifiP2pDevice=peers[position]
                    val config:WifiP2pConfig= WifiP2pConfig()
                    config.deviceAddress=wifiDevice.deviceAddress


                    if (ActivityCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.NEARBY_WIFI_DEVICES
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                    }


                    manager.connect(channel,config,object:WifiP2pManager.ActionListener{

                        override fun onSuccess() {
                            discover_status.text="Connected to ${wifiDevice.deviceName.toString().subSequence(0,
                                kotlin.math.min(wifiDevice.deviceName.toString().length - 1, 5)
                            )}"

                        }

                        override fun onFailure(p0: Int) {
                            discover_status.text="Not Connected"
                            discover_status.setBackgroundResource(R.color.red)
                        }

                    })



                }

            })



        }
    }
    var connectionInfoListener:WifiP2pManager.ConnectionInfoListener=object : WifiP2pManager.ConnectionInfoListener{
        override fun onConnectionInfoAvailable(p0: WifiP2pInfo?) {
            if(p0!=null) {
                val groupOwnerAddress: InetAddress? = p0.groupOwnerAddress
//                Toast.makeText(this@MainActivity, "$p0.toString()  $p0.groupOwnerAddress}", Toast.LENGTH_SHORT).show()
                if(groupOwnerAddress!=null)
                {
                    if (p0.groupFormed == true && p0.isGroupOwner) {
                        isHost = true
                        discover_status.text = "Host"
                        serverClass = ServerClass()
                        serverClass.start()
                    } else if (p0?.groupFormed == true) {
                        discover_status.text = "Client"
                        isHost = false
                        clientClass = ClientClass(groupOwnerAddress)
                        clientClass.start()
                    }
                }
            }

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


    inner class ClientClass(hostAddress: InetAddress):Thread() {

        lateinit var hostAdd:String
        private lateinit var inputStream: InputStream
        private lateinit var outputStream: OutputStream

        init {
            hostAdd=hostAddress.hostAddress
            socket = Socket()
        }

        fun write(bytes:ByteArray){
            try {
                outputStream.write(bytes)
            }
            catch (e:IOException){
                e.printStackTrace()
            }
        }

        override fun run() {
            try {
                socket.connect(InetSocketAddress(hostAdd,8888),500)
                inputStream=socket.getInputStream()
                outputStream=socket.getOutputStream()

            }catch (e:IOException){
                Toast.makeText(this@MainActivity, "${e.printStackTrace()}", Toast.LENGTH_LONG).show()
            }

            var executor:ExecutorService=Executors.newSingleThreadExecutor()
            var handler:Handler= Handler(Looper.getMainLooper())
            executor.execute(object:Runnable {
                override fun run() {
                    var buffer:ByteArray=ByteArray(1024)
                    var byte:Int=0
                    while(socket!=null){
                        try {
                            byte=inputStream.read(buffer)
                            if(byte>0){
                                var finalByte=byte
                                handler.post(object : Runnable {
                                    override fun run() {
                                        msg_List.add(String(buffer,0,finalByte))
                                        rcv_msg.adapter=myAdapter_msg(msg_List)
                                        rcv_msg.layoutManager=LinearLayoutManager(this@MainActivity)
                                        rcv_msg.setHasFixedSize(true)

                                    }

                                })
                            }
                        }
                        catch (e:IOException){
                            e.printStackTrace()
                        }

                    }
                }

            })


        }


    }

    inner class ServerClass:Thread(){
        lateinit var serverSocket: ServerSocket
        lateinit var inputStream: InputStream
        lateinit var outputStream: OutputStream

        fun write(bytes:ByteArray){
            try {
                outputStream.write(bytes)
            }
            catch (e:IOException){
                e.printStackTrace()
            }
        }
        override fun run() {
            try {
                serverSocket=ServerSocket(8888)
                socket=serverSocket.accept()
                inputStream=socket.getInputStream()
                outputStream=socket.getOutputStream()

            }catch (e:IOException){
                e.printStackTrace()
            }
            var executor:ExecutorService=Executors.newSingleThreadExecutor()
            var handler:Handler= Handler(Looper.getMainLooper())
            executor.execute(object:Runnable {
                override fun run() {
                    var buffer:ByteArray=ByteArray(1024)
                    var byte:Int=0
                    while(socket!=null){
                        try {
                            byte=inputStream.read(buffer)
                            if(byte>0){
                                var finalByte=byte
                                handler.post(object : Runnable {
                                    override fun run() {
                                        msg_List.add(String(buffer,0,finalByte))
                                        rcv_msg.adapter=myAdapter_msg(msg_List)
                                        rcv_msg.layoutManager=LinearLayoutManager(this@MainActivity)
                                        rcv_msg.setHasFixedSize(true)

                                    }

                                })
                            }
                        }
                        catch (e:IOException){
                            e.printStackTrace()
                        }

                    }
                }

            })
        }
    }


}