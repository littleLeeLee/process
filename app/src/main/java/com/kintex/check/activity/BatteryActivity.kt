package com.kintex.check.activity

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_CHANGED
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.CaseType
import com.kintex.check.bean.TestCase
import com.kintex.check.utils.CaseId
import com.kintex.check.utils.ResultCode
import com.kintex.check.utils.ResultCode.BATTERY_POSITION
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.PASSED
import kotlinx.android.synthetic.main.activity_battery.*
import kotlinx.android.synthetic.main.title_include.*

class BatteryActivity  : BaseActivity() {


    private var batteryReceiver : BatteryReceiver ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battery)
        val hasTypes = caseType!!.typeItems.find {
            it.caseId == CaseId.WirelessCharge.id
        }
        if(hasTypes!=null){
            hasWill = true
            tv_wrlless.text = "未测试"
        }else{
            tv_wrlless.text = "不支持"
            tv_wrlless.setTextColor(resources.getColor(R.color.red))
        }

        XLog.d("WILL =$hasWill")
        batteryReceiver = BatteryReceiver()
        val intentFilter = IntentFilter(ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver,intentFilter)
        getTotalBattery()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val locales = resources.configuration.locales
            for (i in 0 until locales.size()){
                val language = locales[i].language
                XLog.d("language:$language")
            }
        }
        setView()
    }

    private fun setView() {

        tv_titleName.text = "Battery Test"
        tv_titleReset.text = "Back"
        tv_titleReset.setOnClickListener {

            finish()

        }

        tv_titleDone.setOnClickListener {
         finish()

        }

        btn_failed.setOnClickListener {
           if(hasWill){
               if(!isUsbTest && !isWillTest){
                   sendCaseResult(CaseId.USBCharge.id, FAILED, ResultCode.MANUAL)
                   isUsbTest = true
                   isUsbPass = false
                   tv_usbresult.text = "fail"
                   tv_usbresult.setTextColor(resources.getColor(R.color.red))
               }else if(isUsbTest && !isWillTest){
                 //  ToastUtils.showShort("2fail")
                   isWillTest = true
                   tv_wrlless.text = "fail"
                   tv_wrlless.setTextColor(resources.getColor(R.color.red))
                   if(isUsbPass){
                       sendCaseResult(CaseId.USBCharge.id, PASSED, ResultCode.MANUAL)
                   }else{
                       sendCaseResult(CaseId.USBCharge.id, FAILED, ResultCode.MANUAL)
                   }
                   sendCaseResult(CaseId.WirelessCharge.id, FAILED, ResultCode.MANUAL)
                   finish()

               }else if(!isUsbTest && isWillTest){
              //     ToastUtils.showShort("1fail")
                   isUsbTest = true
                   tv_usbresult.text = "fail"
                   tv_usbresult.setTextColor(resources.getColor(R.color.red))
                   sendCaseResult(CaseId.USBCharge.id, FAILED, ResultCode.MANUAL)
                   sendCaseResult(CaseId.WirelessCharge.id, PASSED, ResultCode.MANUAL)
                   finish()
               }

           }else{
               isUsbTest = true
               tv_usbresult.text = "fail"
               tv_usbresult.setTextColor(resources.getColor(R.color.red))
               sendCaseResult(CaseId.USBCharge.id, FAILED, ResultCode.MANUAL)
               finish()
           }

        }

        btn_passed.setOnClickListener {
        }

    }

    private var isUsbPass = false
    private var isWillPass = false
    private var isWillTest = false
    private var isUsbTest = false
    private var hasWill = false

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
                        runOnUiThread {
                            isUsbTest = true
                            tv_usbresult.text = "pass"
                            tv_usbresult.setTextColor(resources.getColor(R.color.green))
                        }
                        isUsbPass = true
                        checkPass()
                    }
                    BatteryManager.BATTERY_PLUGGED_USB -> {
                        isUsbTest = true
                        tv_chargeState.text = "电源：USB"
                        isUsbPass = true
                        runOnUiThread {
                            tv_usbresult.text = "pass"
                            tv_usbresult.setTextColor(resources.getColor(R.color.green))
                        }
                        checkPass()
                    }
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> {
                        isWillTest = true
                        tv_chargeState.text = "电源：无线"
                        isWillPass = true
                        runOnUiThread {
                            if(isUsbTest){
                                tv_wrlless.text = "pass"
                                tv_wrlless.setTextColor(resources.getColor(R.color.green))
                             //   finish()
                            }else{
                                tv_wrlless.text = "pass"
                                tv_wrlless.setTextColor(resources.getColor(R.color.green))
                            }

                        }
                        checkPass()
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

    fun checkPass(){
        if(hasWill){
            if(isUsbPass && isWillPass){
                sendCaseResult(CaseId.WirelessCharge.id, PASSED, ResultCode.MANUAL)
                sendCaseResult(CaseId.USBCharge.id, PASSED, ResultCode.MANUAL)
                finish()
            }
        }else{
            if(isUsbPass){
                sendCaseResult(CaseId.USBCharge.id, PASSED, ResultCode.MANUAL)
                finish()
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
            testNext()
    }

    companion object{
        var caseType : CaseType?=null
        fun start(context: Context,caseType: CaseType?){
            this.caseType = caseType
            XLog.d("start")
            context.startActivity(Intent(context,BatteryActivity::class.java))
        }
    }

}