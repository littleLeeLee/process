package com.kintex.check.adapter

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kintex.check.R


class CameraAdapter : RecyclerView.Adapter<CameraAdapter.MyViewHolder> {
    private var mContext : Context?= null
    private var cameraManager : CameraManager?= null
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
       /* if(position == planList!!.size-1){
            holder.name.text = "闪光灯"
        }else{*/
            val cameraCharacteristics = cameraManager!!.getCameraCharacteristics(deviceId)
            val facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
            if(facing == null){
                holder.name.text = deviceId
                return
            }
            when(facing){

                CameraCharacteristics.LENS_FACING_FRONT -> { // 前置摄像
                    holder.name.text = "Front Camera :$deviceId"
                     }
                CameraCharacteristics.LENS_FACING_BACK -> { // 后置摄像头
                    holder.name.text = "Rear Camera :$deviceId"
                         }
                CameraCharacteristics.LENS_FACING_EXTERNAL -> { // 外置摄像头

                    holder.name.text = "Extra Camera :$deviceId"
                    }

            }

      //  }


        holder.itemView.setOnClickListener {
            val layoutPosition = holder.layoutPosition
            itemClickListener!!.onItemClick(holder.itemView,layoutPosition)
        }

    }

    constructor (
        context: Context,
        list: ArrayList<String>,
        cameraManager: CameraManager
    ){
        mContext = context
        planList = ArrayList()
        planList!!.addAll(list)
        this.cameraManager = cameraManager
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