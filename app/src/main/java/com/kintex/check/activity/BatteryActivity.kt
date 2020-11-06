package com.kintex.check.activity

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_CHANGED
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.Toast
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.TestCase
import com.kintex.check.utils.ResultCode.BATTERY_POSITION
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.PASSED
import kotlinx.android.synthetic.main.activity_battery.*
import kotlinx.android.synthetic.main.title_include.*

class BatteryActivity  : BaseActivity() {

    private var resultCaseList = arrayListOf<TestCase>(
        TestCase("BatteryHealth",52,"BatteryHealth","",1,0)
    )
    private var batteryReceiver : BatteryReceiver ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battery)
        batteryReceiver = BatteryReceiver()
        val intentFilter = IntentFilter(ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver,intentFilter)
        getTotalBattery()

        setView()
    }

    private fun setView() {


        tv_titleName.text = "Battery Test"
        tv_titleReset.text = "Back"
        tv_titleReset.setOnClickListener {

            finish()

        }

        tv_titleDone.setOnClickListener {
            resultCaseList[0].result = 0
            sendResult(BATTERY_POSITION, FAILED,resultCaseList)

        }

        btn_failed.setOnClickListener {

            resultCaseList[0].result = 0
            sendResult(BATTERY_POSITION, FAILED,resultCaseList)
        }

        btn_passed.setOnClickListener {
            resultCaseList[0].result = 1
            sendResult(BATTERY_POSITION, PASSED,resultCaseList)
        }

    }


    inner class BatteryReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            val action = intent!!.action
            if(action.equals(Intent.ACTION_BATTERY_CHANGED,true) ){
                //电压
                val voltage  = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
                tv_volatage.text = "电池电压：${voltage/1000.toFloat()} v"
                // 电池的健康状态
                val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                when(health){

                    BatteryManager.BATTERY_HEALTH_GOOD->{
                        tv_health.text = "健康度：良好"
                    }

                    BatteryManager.BATTERY_HEALTH_COLD->{
                        tv_health.text = "健康度：温度过低"
                    }

                    BatteryManager.BATTERY_HEALTH_DEAD->{
                        tv_health.text = "健康度：糟糕"
                    }

                    BatteryManager.BATTERY_HEALTH_OVERHEAT->{
                        tv_health.text = "健康度：温度过热"
                    }

                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE->{
                        tv_health.text = "健康度：电压过高"
                    }

                    BatteryManager.BATTERY_HEALTH_UNKNOWN->{
                        tv_health.text = "健康度：未知"
                    }

                    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE->{
                        tv_health.text = "健康度：未知异常"
                    }
                }

                //当前电量
                val level  = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                tv_quantity.text = "当前电量：$level%"
                //最大电量
                val maxLevel = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
             //   tv_total.text = "总容量：$maxLevel"
                // 当前手机使用的是哪里的电源
                val pluged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                when (pluged) {
                    BatteryManager.BATTERY_PLUGGED_AC -> {
                        tv_chargeState.text = "电源：充电器"
                    }
                    BatteryManager.BATTERY_PLUGGED_USB -> {
                        tv_chargeState.text = "电源：USB"
                    }
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> {
                        tv_chargeState.text = "电源：无线"
                    }

                }
                //充放电状态
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
              //  ToastUtils.showShort("status$status")
                when (status) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> {
                        tv_batteryState.text = "电池状态：正在充电"
                    }
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> {
                        XLog.d("DISCHARGING")
                        tv_batteryState.text = "电池状态：正在放电"
                        tv_chargeState.text = "电源：电池"
                    }
                    BatteryManager.BATTERY_STATUS_FULL -> {
                        tv_batteryState.text = "电池状态：已充满"
                    }
                    BatteryManager.BATTERY_STATUS_NOT_CHARGING->{

                    }

                    BatteryManager.BATTERY_STATUS_UNKNOWN->{
                        tv_batteryState.text = "电池状态：未知"
                    }
                }

                // 电池使用的技术。比如，对于锂电池是Li-ion
                val technology =
                    intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
                tv_discrete.text = "电池技术：$technology"
                // 当前电池的温度
                val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                tv_temperature.text = "电池温度：${temperature/10.toFloat()} ℃"

            }

        }


    }

    fun getTotalBattery(){
        var POWER_PROFILE_CLASS ="com.android.internal.os.PowerProfile";

        try {

           var mPowerProfile = Class.forName(POWER_PROFILE_CLASS).getConstructor(Context::class.java).newInstance(this)

            var batteryCapacity =  Class.forName(POWER_PROFILE_CLASS).getMethod("getBatteryCapacity").invoke(mPowerProfile)as Double
            tv_total.text = "电池容量：$batteryCapacity mAh"
        }catch ( e :Exception) {

            XLog.d("get failed")

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)

    }

    companion object{
        fun start(context: Context){
            XLog.d("start")
            context.startActivity(Intent(context,BatteryActivity::class.java))
        }
    }

}