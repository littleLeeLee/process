package com.kintex.check.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kintex.check.R
import com.kintex.check.bean.TestCase


class ResultItemAdapter : RecyclerView.Adapter<ResultItemAdapter.MyViewHolder> {
    private var mContext : Context?= null
    private var planList : ArrayList<TestCase> ?= null
    private var itemClickListener : onItemClickListener ? = null
    private var itemLongClickListener : onItemLongClickListener ? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view = LayoutInflater.from(mContext).inflate(R.layout.item_result, parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return planList!!.size
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val testPlan = planList!![position]

        holder.name.text = testPlan.caseName
        if(testPlan.result == 1){
            holder.des.text = "Passed"
        }else{
            holder.des.text = "Failed"
        }


    }


    constructor (context: Context, list : ArrayList<TestCase>){
        mContext = context
        planList = ArrayList()
        planList!!.addAll(list)

    }



    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name= itemView.findViewById<TextView>(R.id.tv_itemResultName)
        var des= itemView.findViewById<TextView>(R.id.tv_itemResultDisc)

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