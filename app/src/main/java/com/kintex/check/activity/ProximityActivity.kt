package com.kintex.check.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.TestCase
import com.kintex.check.bean.TestResultBean
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.PASSED
import com.kintex.check.utils.ResultCode.PROXIMITY_POSITION
import kotlinx.android.synthetic.main.title_include.*
import org.greenrobot.eventbus.EventBus

class ProximityActivity : BaseActivity() {

    private var resultCaseList  = arrayListOf<TestCase>(
        TestCase(8,"Proximity Sensor","",1,0)
    )
    private var firstValue = -1
    private var isFirst = true
    private var sensorManager: SensorManager?=null
    private  var proximity :TextView ?=null
    private var proximityLicenser : MySensorLicenser?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_proximity)
        proximity = findViewById<TextView>(R.id.tv_proximity)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val proximitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        proximityLicenser = MySensorLicenser()
        sensorManager!!.registerListener(proximityLicenser,proximitySensor,SensorManager.SENSOR_DELAY_NORMAL)

        tv_titleDone.setOnClickListener {
            resultCaseList[0].result = 0
            sendResult(PROXIMITY_POSITION, FAILED,resultCaseList)
        }
        tv_titleName.text = "Proximity Sensor"
        tv_titleReset.setOnClickListener {

            finish()

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager!!.unregisterListener(proximityLicenser)
    }


    /**
     * 三星s10的距离感应器有问题
     */
    inner class MySensorLicenser :  SensorEventListener {

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            XLog.d("accuracy:$accuracy")
        }

        override fun onSensorChanged(event: SensorEvent?) {

            if(event!!.sensor.type == Sensor.TYPE_PROXIMITY){
                var result = event.values[0].toInt()
                if(isFirst){
                    isFirst = false
                    firstValue = result
                }

                XLog.d("value :$result  firstValue $firstValue")
                runOnUiThread {
                    if(result != firstValue){

                        // showDialog("通过")
                        ToastUtils.showShort("通过")
                        proximity!!.postDelayed(Runnable {
                            resultCaseList[0].result = 1
                            sendResult(PROXIMITY_POSITION, PASSED,resultCaseList)
                        },200)

                    }
                    proximity!!.text = "X :$result"

                }

            }

        }
    }


    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context,ProximityActivity::class.java))
        }
    }

}