package com.kintex.check.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.CaseType
import com.kintex.check.bean.TypeItem
import com.kintex.check.utils.ResultCode.DEFAULT
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.PASSED


class NewMainListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>, StickHeaderDecoration.StickHeaderInterface {
    private var mContext : Context?= null
    private var planList : ArrayList<CaseType> ?= null
    private var itemClickListener : onItemClickListener ? = null
    private var itemLongClickListener : onItemLongClickListener ? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {


        if(viewType ==2){
            var view = LayoutInflater.from(mContext).inflate(R.layout.item_content_head, parent,false)
            return ItemHeadHolder(view)
        }else{
            var view = LayoutInflater.from(mContext).inflate(R.layout.item_main, parent,false)
            return ItemContentHolder(view)
        }


    }

    override fun getItemCount(): Int {
        return planList!!.size
    }
    override fun onBindViewHolder(holder:  RecyclerView.ViewHolder, position: Int) {
        val testPlan = planList!![position]
        if(holder is ItemHeadHolder){

            holder.titleName.text = testPlan.name

        }else if(holder is ItemContentHolder){
            holder.name.text = testPlan.name
            setImage(testPlan.name,holder)
            var defaultCount = 0
            var passedCount = 0
            var failedCount = 0

            for (case in testPlan.typeItems!!){
                when(case.result){

                    PASSED->{
                        passedCount++
                    }
                    FAILED->{
                        failedCount++
                    }
                    DEFAULT->{
                        defaultCount++
                    }
                    else->{
                        defaultCount++
                    }
                }

            }
         //   XLog.d("passedCount$passedCount")
         //   XLog.d("defaultCount$defaultCount")
            if(passedCount == testPlan.typeItems.size){

                holder.itemState.setImageResource(R.mipmap.passed)
            }else if(defaultCount == testPlan.typeItems.size){
                holder.itemState.setImageResource(R.mipmap.alert)
            }else{
           //     XLog.d("failedCount$failedCount")
                holder.itemState.setImageResource(R.mipmap.failed)
            }

            setItemCaseView(holder,testPlan.typeItems,position)
        }

        holder.itemView.setOnClickListener {
            val layoutPosition = holder.layoutPosition
            itemClickListener!!.onItemClick(holder.itemView,layoutPosition)
        }

    }

    private fun setImage(name: String, holder: ItemContentHolder) {

        when(name){
            mContext!!.resources.getString(R.string.Connection)->{
                holder.imageView.setImageResource(R.mipmap.connect)
            }
            mContext!!.resources.getString(R.string.Sensor)->{
                holder.imageView.setImageResource(R.mipmap.sensor)
            }
            mContext!!.resources.getString(R.string.Secure)->{
                holder.imageView.setImageResource(R.mipmap.secure)
            }
            mContext!!.resources.getString(R.string.ScreenTest)->{
                holder.imageView.setImageResource(R.mipmap.screentest)
            }
            mContext!!.resources.getString(R.string.Button)->{
                holder.imageView.setImageResource(R.mipmap.buttontest)
            }
            mContext!!.resources.getString(R.string.CameraTest)->{
                holder.imageView.setImageResource(R.mipmap.cameratest)
            }
            mContext!!.resources.getString(R.string.AudioTest)->{
                holder.imageView.setImageResource(R.mipmap.audiotest)
            }
            mContext!!.resources.getString(R.string.Battery)->{
                holder.imageView.setImageResource(R.mipmap.batterytest)
            }
            mContext!!.resources.getString(R.string.Headset)->{
                holder.imageView.setImageResource(R.mipmap.headset)
            }
            mContext!!.resources.getString(R.string.Cosmetics)->{
                holder.imageView.setImageResource(R.mipmap.cosmetics)
            }
            mContext!!.resources.getString(R.string.Notes)->{
                holder.imageView.setImageResource(R.mipmap.cosmetics)
            }

        }

    }

    private fun setItemCaseView(holder: ItemContentHolder, typeItems: List<TypeItem>?, rootPosition: Int) {

        val resultItemAdapter = ResultItemAdapter(mContext!!, typeItems!!)
        holder.itemList.layoutManager = LinearLayoutManager(
                mContext!!,
                LinearLayoutManager.VERTICAL,
                false
        )
        resultItemAdapter.setOnItemClickListener(object : ResultItemAdapter.onItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                ToastUtils.showShort(planList!![rootPosition].name)
            }
        })
        holder.itemList.adapter = resultItemAdapter

    }

    override fun getItemViewType(position: Int): Int {

        return if(planList!![position].typeId <0){
            //头
            2
        }else{
            1
        }
    }


    constructor (context: Context, list : ArrayList<CaseType>){
        mContext = context
        planList = ArrayList()
        planList!!.addAll(list)

    }



    class ItemContentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name= itemView.findViewById<TextView>(R.id.tv_testContentName)
        var imageView= itemView.findViewById<ImageView>(R.id.iv_testType)
        var itemList = itemView.findViewById<RecyclerView>(R.id.ry_itemCaseList)
        var itemState = itemView.findViewById<ImageView>(R.id.iv_testTypeResult)

    }

    class ItemHeadHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleName= itemView.findViewById<TextView>(R.id.tv_itemContentHeadName)

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


    override fun isStick(position: Int): Boolean {

        return planList!![position].typeId <0
      //  return false
    }

}