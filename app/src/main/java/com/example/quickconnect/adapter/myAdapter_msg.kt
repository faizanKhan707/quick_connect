package com.example.quickconnect.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quickconnect.R

class myAdapter_msg(val data:List<String>): RecyclerView.Adapter<myAdapter_msg.myViewHolder>() {
    class myViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val textView=view.findViewById<TextView>(R.id.peer_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val activity=LayoutInflater.from(parent.context).inflate(R.layout.message_list,parent,false)
        return myViewHolder(activity)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        holder.textView.text=data.get(position)
    }
}