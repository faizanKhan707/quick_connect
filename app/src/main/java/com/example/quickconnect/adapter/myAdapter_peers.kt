package com.example.quickconnect.adapter

import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quickconnect.R

class myAdapter_peers(val data: List<WifiP2pDevice>): RecyclerView.Adapter<myAdapter_peers.myViewHolder>() {

    class myViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val textview=view.findViewById<TextView>(R.id.peer_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val activity=LayoutInflater.from(parent.context).inflate(R.layout.peer_list,parent,false)
        return myViewHolder(activity)
    }

    override fun getItemCount(): Int = data.size


    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        holder.textview.text=data[position].deviceName

    }

}