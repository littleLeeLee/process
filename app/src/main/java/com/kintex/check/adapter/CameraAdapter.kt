package com.kintex.check.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.kintex.check.R
import com.kintex.check.bean.TestPlanBean


class CameraAdapter : RecyclerView.Adapter<CameraAdapter.MyViewHolder> {
    private var mContext : Context?= null
    private var planList : ArrayList<String> ?= null
    private var itemClickListener : onItemClickListener ? = null
    private var itemLongClickListener : onItemLongClickListener ? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view = LayoutInflater.from(mContext).inflate(R.layout.item_camera, parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return planList!!.size
    }
    override fun onBindViewHolder(holder: CameraAdapter.MyViewHolder, position: Int) {
        val deviceId = planList!![position]
        if(position == planList!!.size-1){
            holder.name.text = "闪光灯"
        }else{
            holder.name.text = deviceId
        }


        holder.itemView.setOnClickListener {
            val layoutPosition = holder.layoutPosition
            itemClickListener!!.onItemClick(holder.itemView,layoutPosition)
        }

    }

    constructor (context: Context, list : ArrayList<String>){
        mContext = context
        planList = ArrayList()
        planList!!.addAll(list)

    }



    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name= itemView.findViewById<TextView>(R.id.tv_CameraName)

    }


    interface onItemClickListener  {
        fun onItemClick(view: View,position: Int)
    }
    interface onItemLongClickListener  {
        fun onItemLongClick(view: View,position: Int)
    }
    fun setOnItemClickListener(item:onItemClickListener){
        itemClickListener  = item
    }

    fun setOnItemLongClickListener(item:onItemLongClickListener){
        itemLongClickListener  = item
    }
}