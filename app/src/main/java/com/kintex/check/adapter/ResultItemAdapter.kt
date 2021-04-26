package com.kintex.check.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.LayoutDirection
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.kintex.check.R
import com.kintex.check.bean.CaseResultBean
import com.kintex.check.bean.CheckBean
import com.kintex.check.bean.TypeItem
import com.kintex.check.utils.ResultCode
import com.kintex.check.utils.ResultCode.MANUAL
import com.kintex.check.utils.ResultCode.PASSED
import org.greenrobot.eventbus.EventBus


class ResultItemAdapter : RecyclerView.Adapter<ResultItemAdapter.MyViewHolder> {
    private var mContext : Context?= null
    private var planList : ArrayList<TypeItem> ?= null
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
     //   XLog.d("result : ${testPlan.caseName} +: ${testPlan.result}")
        holder.name.text = testPlan.caseName
        when(testPlan.caseId){

            1024,1025,1026,1027,1028,1029->{

                holder.name.gravity = Gravity.RIGHT
                holder.quailtyList.visibility = View.VISIBLE
                holder.des.visibility = View.GONE
                setList(holder,testPlan)

            }else->{
            holder.name.gravity = Gravity.LEFT
            holder.quailtyList.visibility = View.GONE
            holder.des.visibility = View.VISIBLE
            when (testPlan.result) {
                ResultCode.PASSED -> {
                    holder.des.text = "Passed"
                    holder.des.setTextColor(mContext!!.getColor(R.color.restColor))
                }
                ResultCode.FAILED -> {
                    holder.des.text = "Failed"
                    holder.des.setTextColor(mContext!!.getColor(R.color.red))
                }
                else -> {
                    holder.des.text = ""
                }
            }

        }

        }



        holder.itemView.setOnClickListener {
            val layoutPosition = holder.layoutPosition
            itemClickListener!!.onItemClick(holder.itemView,layoutPosition)
        }
    }

    @SuppressLint("WrongConstant")
    private fun setList(holder: MyViewHolder, testPlan: TypeItem) {
        val list = ArrayList<CheckBean>()
        when(testPlan.caseId){

            1024,1025,1026,1027->{

                list.add(CheckBean("A+"))
                list.add(CheckBean("A"))
                list.add(CheckBean("B"))
                list.add(CheckBean("C"))
                list.add(CheckBean("D"))
                for( i in list.indices){
                    val radioButton = LayoutInflater.from(mContext!!).inflate(R.layout.item_radio, null) as RadioButton
                    radioButton.text = list[i].name
                    holder.quailtyList.addView(radioButton)
                }
                holder.quailtyList.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
                    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
                        val checkedRadioButtonId = group.checkedRadioButtonId
                        val findViewById = group.findViewById<RadioButton>(checkedRadioButtonId)
                        val caseResultBean = CaseResultBean(testPlan.caseId, PASSED, MANUAL)
                        caseResultBean.dis = findViewById.text.toString()
                        EventBus.getDefault().post(caseResultBean)
                      //  ToastUtils.showShort("checkedId${testPlan.caseId}  text:${findViewById.text}")
                    }
                })
            }

            1028,1029->{
                val radioButton = LayoutInflater.from(mContext!!).inflate(R.layout.item_radio, null) as RadioButton
                radioButton.text = mContext!!.resources.getString(R.string.Failed)
                holder.quailtyList.addView(radioButton)
                val radioButton1 = LayoutInflater.from(mContext!!).inflate(R.layout.item_radio, null) as RadioButton
                radioButton1.text = mContext!!.resources.getString(R.string.Passed)
                holder.quailtyList.addView(radioButton1)
                holder.quailtyList.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
                    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
                        val checkedRadioButtonId = group.checkedRadioButtonId
                        val findViewById = group.findViewById<RadioButton>(checkedRadioButtonId)
                        val caseResultBean = CaseResultBean(testPlan.caseId, PASSED, MANUAL)
                        caseResultBean.dis = findViewById.text.toString()
                        EventBus.getDefault().post(caseResultBean)
                    }
                })
            }
        }

    }


    constructor (context: Context, list : List<TypeItem>){

        mContext = context
        planList = ArrayList()
        planList!!.addAll(list)

    }



    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name= itemView.findViewById<TextView>(R.id.tv_itemResultName)
        var des= itemView.findViewById<TextView>(R.id.tv_itemResultDisc)
        var quailtyList = itemView.findViewById<RadioGroup>(R.id.group)

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