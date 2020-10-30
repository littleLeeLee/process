package com.kintex.check.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elvishew.xlog.XLog
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
      //  holder.setIsRecyclable(false)
        holder.name.text = testPlan.planName
        holder.des.text = testPlan.planDescription

        when(testPlan.planResult){

            //未测试
            0->{
                holder.notice.setImageResource(R.mipmap.notice)
                holder.ryItemList.visibility = View.GONE
            }

            //通过
            1->{
                holder.des.visibility = View.VISIBLE
                holder.ryItemList.visibility = View.GONE
                holder.notice.setImageResource(R.mipmap.pass)
            }
            //失败
            2->{
                holder.notice.setImageResource(R.mipmap.fail)
                holder.des.visibility = View.GONE
                holder.ryItemList.visibility = View.VISIBLE
                setFailedView(holder,testPlan)

            }

        }

        if(testPlan.clickState && testPlan.planResult != DEFAULT){
            holder.cardView.setCardBackgroundColor(mContext!!.resources.getColor(R.color.tested,null))
        }else{
            holder.cardView.setCardBackgroundColor(mContext!!.resources.getColor(R.color.white,null))
        }
        holder.itemIcon.setImageResource(testPlan.planPic)
        holder.itemView.setOnClickListener {
            val layoutPosition = holder.layoutPosition
            itemClickListener?.onItemClick(holder.itemView,layoutPosition)
        }

    }

    private fun setFailedView(
        viewHolder: MyViewHolder,
        testPlan: TestPlanBean
    ) {

        val resultItemAdapter = ResultItemAdapter(mContext!!, testPlan.resultItemList)
        viewHolder.ryItemList.layoutManager = LinearLayoutManager(
            mContext!!,
            LinearLayoutManager.VERTICAL,
            false
        )
        viewHolder.ryItemList.adapter = resultItemAdapter
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
        var ryItemList = itemView.findViewById<RecyclerView>(R.id.ry_itemList)
        var itemIcon = itemView.findViewById<ImageView>(R.id.iv_itemIcon)

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