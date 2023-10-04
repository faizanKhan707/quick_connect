package com.example.quickconnect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.net.wifi.p2p.WifiP2pManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.quickconnect.adapter.myAdapter_msg
import com.example.quickconnect.adapter.myAdapter_peers


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rcv_peer=findViewById<RecyclerView>(R.id.rcv_peers)
        val rcv_msg=findViewById<RecyclerView>(R.id.rcv_msg)

        var dummyList= mutableListOf<String>()
        var dummyList2= mutableListOf<String>()

        for(i in 1..100) {
            dummyList.add("Peer $i")
            dummyList2.add("Message $i")
        }
        rcv_peer.adapter=myAdapter_peers(dummyList)
        rcv_peer.layoutManager=LinearLayoutManager(this)
        rcv_peer.setHasFixedSize(true)

        rcv_msg.adapter=myAdapter_msg(dummyList2)
        rcv_msg.layoutManager=LinearLayoutManager(this)
        rcv_msg.setHasFixedSize(true)
    }
}