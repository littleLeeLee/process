package com.kintex.check.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.CaseType
import com.kintex.check.bean.CheckBean
import com.kintex.check.bean.TypeItem
import com.kintex.check.utils.ResultCode.DEFAULT
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.PASSED


class QualityListAdapter : RecyclerView.Adapter<QualityListAdapter.ItemContentHolder> {
    private var mContext : Context?= null
    private var itemClickListener : onItemClickListener ? = null
    private var itemLongClickListener : onItemLongClickListener ? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemContentHolder {

            var view = LayoutInflater.from(mContext).inflate(R.layout.item_checkbox, parent,false)
            return ItemContentHolder(view)



    }

    override fun getItemCount(): Int {
        return itemList!!.size
    }
    override fun onBindViewHolder(holder:  ItemContentHolder, position: Int) {


        holder.itemView.setOnClickListener {
            val layoutPosition = holder.layoutPosition
            itemClickListener!!.onItemClick(holder.itemView,layoutPosition)
        }

    }



    var itemList : ArrayList<CheckBean> ?= null
    constructor (context: Context, list : ArrayList<CheckBean>){
        mContext = context
        itemList = list
    }



    class ItemContentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var group= itemView.findViewById<RadioGroup>(R.id.rg_checkBox)


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