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
import com.kintex.check.utils.ResultCode.DEFAULT


class MainListAdapter : RecyclerView.Adapter<MainListAdapter.MyViewHolder> {
    private var mContext : Context?= null
    private var planList : ArrayList<TestPlanBean> ?= null
    private var itemClickListener : onItemClickListener ? = null
    private var itemLongClickListener : onItemLongClickListener ? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view = LayoutInflater.from(mContext).inflate(R.layout.item_main_list, parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return planList!!.size
    }
    override fun onBindViewHolder(holder: MainListAdapter.MyViewHolder, position: Int) {
        val testPlan = planList!![position]

        holder.name.text = testPlan.planName
        holder.des.text = testPlan.planDescription

        when(testPlan.planResult){

            //未测试
            0->{
                holder.notice.setImageResource(R.mipmap.notice)
            }

            //通过
            1->{
                holder.notice.setImageResource(R.mipmap.pass)
            }
            //失败
            2->{
                holder.notice.setImageResource(R.mipmap.failed)
            }

        }

        if(testPlan.clickState && testPlan.planResult != DEFAULT){
            holder.cardView.setCardBackgroundColor(mContext!!.resources.getColor(R.color.littleblue,null))
        }else{
            holder.cardView.setCardBackgroundColor(mContext!!.resources.getColor(R.color.white,null))
        }

        holder.itemView.setOnClickListener {
            val layoutPosition = holder.layoutPosition
            itemClickListener!!.onItemClick(holder.itemView,layoutPosition)
        }
/*
        holder.itemView.setOnLongClickListener {
            val layoutPosition = holder.layoutPosition
            itemLongClickListener!!.onItemLongClick(holder.itemView,layoutPosition)
            return@setOnLongClickListener true
        }*/
    }

    constructor (context: Context, list : ArrayList<TestPlanBean>){
        mContext = context
        planList = ArrayList()
        planList!!.addAll(list)

    }



    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name= itemView.findViewById<TextView>(R.id.tv_itemMainTitle)
        var des= itemView.findViewById<TextView>(R.id.tv_itemMainDes)
        var notice= itemView.findViewById<ImageView>(R.id.iv_itemMainNotice)
        var cardView= itemView.findViewById<CardView>(R.id.item_cardView)
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