package com.kintex.check.activity

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.*
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import com.kintex.check.R
import com.kintex.check.adapter.MainListAdapter
import com.kintex.check.bean.*
import com.kintex.check.other.AdbTestActivity
import com.kintex.check.service.AdbConnectService
import com.kintex.check.utils.CheckWifiManager
import com.kintex.check.utils.ResultCode.ACCELEROMETER_POSITION
import com.kintex.check.utils.ResultCode.BATTERY_POSITION
import com.kintex.check.utils.ResultCode.BLUETOOTH_POSITION
import com.kintex.check.utils.ResultCode.BUTTON_POSITION
import com.kintex.check.utils.ResultCode.CAM_POSITION
import com.kintex.check.utils.ResultCode.DEFAULT
import com.kintex.check.utils.ResultCode.DIGITIZER_POSITION
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.FINGER_POSITION
import com.kintex.check.utils.ResultCode.GPS_POSITION
import com.kintex.check.utils.ResultCode.LCD_POSITION
import com.kintex.check.utils.ResultCode.MIC_LOUD_POSITION
import com.kintex.check.utils.ResultCode.NFC_POSITION
import com.kintex.check.utils.ResultCode.PASSED
import com.kintex.check.utils.ResultCode.PROXIMITY_POSITION
import com.kintex.check.utils.ResultCode.RESET
import com.kintex.check.utils.ResultCode.SENDSUMMARY
import com.kintex.check.utils.ResultCode.START
import com.kintex.check.utils.ResultCode.STOP
import com.kintex.check.utils.ResultCode.TEST_CALL_POSITION
import com.kintex.check.utils.ResultCode.TOUCH_POSITION
import com.kintex.check.utils.ResultCode.VIBRATION_POSITION
import com.kintex.check.utils.ResultCode.WIFI_POSITION
import com.kintex.check.utils.ResultCode.currentActivity
import com.kintex.check.utils.TestPlanUtils
import com.kintex.check.view.SmoothLinearLayoutManager
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener
import com.kongzue.dialog.util.BaseDialog
import com.kongzue.dialog.v3.MessageDialog
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.title_include.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class MainActivity : BaseActivity(), View.OnClickListener {

    private var testPlanList = arrayListOf(
        TestPlanBean("WiFi", "Please connect to a WiFi network.", R.mipmap.wifi, 0),
        TestPlanBean("Bluetooth", "Turn on your device bluetooth.", R.mipmap.bluetooth, 0),
        TestPlanBean("GPS", "Enable location services to take this test.", R.mipmap.gps, 0),
        TestPlanBean("Vibration", "Device vibration test", R.mipmap.vibration, 0),
        TestPlanBean("Audio Test","Test the mic of this devices with loud speaker.",R.mipmap.micloud, 0),
        //   TestPlanBean("Mic Ear Speaker","Test the mic of this device with ear speaker.", R.mipmap.micear, 0),
        //   TestPlanBean("HeadSet Test", "Test the headSet of this device.", R.mipmap.headset, 0),
        TestPlanBean(
            "Proximity Sensor", "Once activated，wave your hand in front of the proximity sensor，" +
                    "It is located near the earpiece.", R.mipmap.proximity, 0
        ),
        TestPlanBean("Buttons", "Test the hardware buttons on your device.", R.mipmap.button, 0),

        TestPlanBean(
            "Accelerometer",
            "Make the soccer ball touch all the corner of the display by titling the device.",
            R.mipmap.accelerometer,
            0
        ),
        TestPlanBean(
            "CameraTest",
            "Take a picture using front facing camera.Test Front Flash.",
            R.mipmap.camera,
            0
        ),
        //   TestPlanBean("Rear Camera","Take a picture using rear facing cameraDevice. Test Rear Flash.",R.mipmap.wifi,0),
        TestPlanBean("LCD", "Test The LCD Display and Digitizer of the screen.", R.mipmap.lcd, 0),
      //  TestPlanBean("Digitizer", "Test the Digitizer of your device.", R.mipmap.digitizer, 0),
        TestPlanBean(
            "Test Call",
            "Voice call and network connectivity test.",
            R.mipmap.testcall,
            0
        ),
        //   TestPlanBean("Device Lock","FRP And Reactivation Test.",R.mipmap.wifi,0),
        TestPlanBean("Battery", "Battery related information.", R.mipmap.battery, 0),
        TestPlanBean("NFC", "TestNFC.", R.mipmap.nfc, 0),
     //   TestPlanBean("TouchCount", "Test TouchCount.", R.mipmap.touchcount, 0),
        TestPlanBean("FingerPrint", "Test Fingerprint.", R.mipmap.fingerprint, 0)
    )

    private var ryMainList: RecyclerView? = null
    private var adapter: MainListAdapter? = null
    private var reset: TextView? = null
    private var done: TextView? = null
    private var isAutoMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        XLog.d("onCreate")
        startAdbService()
        testPlanList = TestPlanUtils.handleTestPlan(testPlanList)
        setContentView(R.layout.activity_main)
        ryMainList = findViewById(R.id.ry_mainList)
        reset = findViewById(R.id.tv_titleReset)
        done = findViewById(R.id.tv_titleDone)
        reset!!.setOnClickListener(this)
        done!!.setOnClickListener(this)
        //findViewById<WaveView>(R.id.view_testStart).setOnClickListener(this)
        iv_start.setOnClickListener(this)
        findViewById<TextView>(R.id.tv_titleName).setOnClickListener(this)
        setView()
        //    startAutoTest(0)
    }

    private var myConnection: MyConnection? = null
    private fun startAdbService() {
        if (myConnection == null) {
            myConnection = MyConnection()
        }
        bindService(
            Intent(this, AdbConnectService::class.java),
            myConnection!!,
            Context.BIND_AUTO_CREATE
        )
    }

    private var mBinder: AdbConnectService.AdbBinder? = null

    inner class MyConnection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            XLog.d("onServiceDisconnected")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            XLog.d("onServiceConnected")
            mBinder = service as AdbConnectService.AdbBinder

        }

    }

    private fun setView() {
        tv_titleReset.text = "Reset"
        adapter = MainListAdapter(this, testPlanList)

        val smoothLinearLayoutManager =
            SmoothLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        ryMainList!!.setItemViewCacheSize(20)
        ryMainList!!.layoutManager = smoothLinearLayoutManager
        adapter!!.setOnItemClickListener(object : MainListAdapter.onItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                ToastUtils.showShort(testPlanList[position].planName)

                testPlanList[position].clickState = true
                when (position) {
                    WIFI_POSITION -> {
                        checkWifi()
                    }
                    BLUETOOTH_POSITION -> {
                        checkBlueTooth()
                    }
                    GPS_POSITION -> {
                        checkGPS()
                    }

                    PROXIMITY_POSITION -> {

                        checkProximity()

                    }

                    BUTTON_POSITION -> {

                        checkButton()
                    }

                    VIBRATION_POSITION -> {

                        checkVibration()

                    }

                    ACCELEROMETER_POSITION -> {

                        checkAccelerometer()
                    }

                    CAM_POSITION -> {
                        checkCamera()
                    }

                    MIC_LOUD_POSITION -> {

                        checkMic()

                    }

/*                    MIC_EAR_POSITION -> {

                        checkEar()

                    }*/
/*                    HEADSET_POSITION -> {

                        checkHeadSet()

                    }*/

                    LCD_POSITION -> {
                        checkLCD()
                    }

                    DIGITIZER_POSITION -> {

                        checkDigitizer()

                    }

                    TEST_CALL_POSITION -> {

                        testCall()

                    }

                    /*  DEVICE_LOCK_POSITION->{

                        checkDevice()

                    }*/
                    BATTERY_POSITION -> {
                        checkBattery()
                    }

                    NFC_POSITION -> {
                        checkNFC()
                    }

                    TOUCH_POSITION -> {
                        checkTouchCount()

                    }

                    FINGER_POSITION -> {
                        checkFingerPrint()
                    }
                }
            }
        })
        ryMainList!!.adapter = adapter

        checkPermission()

    }


    private fun checkFingerPrint() {

        FingerPrintActivity.start(this)

    }

    private fun checkTouchCount() {

        TouchCountActivity.start(this)

    }

    private fun checkNFC() {

        NFCActivity.start(this)

    }

    private fun checkBattery() {

        BatteryActivity.start(this)

    }

    private fun checkDevice() {

    }

    private fun testCall() {

        CallPhoneActivity.start(this)

    }

    private fun checkHeadSet() {

        HeadSetActivity.start(this)

    }

    private fun checkEar() {

        MicEarActivity.start(this)

    }

    private fun checkLCD() {

        LCDActivity.start(this)

    }

    private fun checkDigitizer() {


        DigitizerActivity.start(this)

    }

    private fun checkMic() {
        SpeakerActivity.start(this)
    }

    private fun checkCamera() {
        XLog.d("checkCamera")
        CameraActivity.start(this)
    }

    private fun checkAccelerometer() {


        AccelerometerActivity.start(this)
        //  AccelerometerPlayActivity.start(this)


    }

    private var currentPosition = 0
    private fun checkProximity() {
        currentPosition = PROXIMITY_POSITION
        ProximityActivity.start(this@MainActivity)

    }

    private var vibratorCaseList = arrayListOf<TestCase>(
        TestCase("Vibration", 45, "Vibration", "", 1, 0)
    )

    private var vibrator: Vibrator? = null
    private var isVibPass = false
    private fun checkVibration() {
        isCheckVivrate = true
        isGyrFirst = true
        isVibPass = false
        maxX = 0f
        maxY = 0f
        maxZ = 0f
        var sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        var gyroscopeSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (gyroscopeSensor != null) {
            sensorManager!!.registerListener(mySensorListener, gyroscopeSensor, 50000)
        }
        try {
            val patter = longArrayOf(200, 600, 200, 600,200, 600,200, 600,200, 600)
            vibrator!!.vibrate(patter,-1)
            tv_titleName.postDelayed(Runnable {
              //  sensorManager.unregisterListener(mySensorListener)
                if(isVibPass){
                    isCheckVivrate = false
                    vibratorCaseList[0].result = 1
                    updateUI(TestResultBean(VIBRATION_POSITION, PASSED, vibratorCaseList))
                }else{
                    showVibratorDialog()
                }
            }, 4500)
        } catch (e: Exception) {
            XLog.d(e)
        }
    }

    private var gyrFirstX = 0f
    private var gyrFirstY = 0f
    private var gyrFirstZ = 0f
    private var isGyrFirst = true


    private var maxX = 0f
    private var maxY = 0f
    private var maxZ = 0f



    private fun showVibratorDialog(){
        isCheckVivrate = false
        val dialog = MessageDialog.build(this)
            .setTitle("震动提醒")
            .setMessage("请确认手机是否有震动")

        dialog.setOkButton("是", object : OnDialogButtonClickListener {
            override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                dialog!!.doDismiss()
                vibratorCaseList[0].result = 1
                updateUI(TestResultBean(VIBRATION_POSITION, PASSED, vibratorCaseList))
                return false
            }
        })
        dialog.cancelable = false
        dialog.setCancelButton("否", object : OnDialogButtonClickListener {
            override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                dialog!!.doDismiss()
                vibratorCaseList[0].result = 0
                updateUI(TestResultBean(VIBRATION_POSITION, FAILED, vibratorCaseList))
                return false

            }
        })
        dialog.show()

    }

        override fun onClick(v: View?) {

            when (v!!.id) {
                //adb
                R.id.tv_titleName -> {

                    //  TestAdbActivity.start(this)
                    startActivity(Intent(this, AdbTestActivity::class.java))

                }

                R.id.tv_titleReset -> {

                    resetTest()
                }

                R.id.tv_titleDone -> {
                    showTestSummary()
                }

                /*R.id.view_testStart ->{
                resetTest()
                startAutoTest(0)

            }*/
                R.id.iv_start -> {

                    resetTest()
                    getAllSensor()
                    startAutoTest(0)
                    startTimer()
                }

            }
        }


        private fun resetTest() {
            stopTimer()
            sensorManager?.unregisterListener(mySensorListener)
            mySensorListener = null
            sensorManager=null
            for (k in testPlanList.indices) {
                testPlanList[k].planResult = DEFAULT
                testPlanList[k].clickState = false
            }
            TestPlanUtils.handleTestPlan(testPlanList)
            updateUI(TestResultBean(RESET, DEFAULT, null))
       //     getAllSensor()
        }

        private var gpsCaseList = arrayListOf<TestCase>(
            TestCase("GPS", 4, "GPS", "", 1, 0)
        )

        //检测GPS
        private fun checkGPS() {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val netLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (locationEnabled || netLocationEnabled) {
                gpsCaseList[0].result = 1
                updateUI(TestResultBean(GPS_POSITION, PASSED, gpsCaseList))
            } else {
                gpsCaseList[0].result = 0
                updateUI(TestResultBean(GPS_POSITION, FAILED, gpsCaseList))
            }

        }

        private var wifiCaseList = arrayListOf<TestCase>(
            TestCase("WiFi", 3, "WiFi", "", 1, 0)
        )

        //检测wifi
        private fun checkWifi() {


            if (NetworkUtils.isWifiAvailable()) {
            } else {
                NetworkUtils.setWifiEnabled(true)
            }
            val checkWifiIsConnect = CheckWifiManager.checkWifiIsConnect()
            XLog.d(checkWifiIsConnect)
            if (checkWifiIsConnect) {
                wifiCaseList[0].result = 1
                updateUI(TestResultBean(WIFI_POSITION, PASSED, wifiCaseList))
            } else {
                wifiCaseList[0].result = 0
                updateUI(TestResultBean(WIFI_POSITION, FAILED, wifiCaseList))
            }

        }


        private var blueToothCaseList = arrayListOf<TestCase>(
            TestCase("BlueTooth", 5, "BlueTooth", "", 1, 0)
        )

        //检测蓝牙
        private fun checkBlueTooth() {

            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter
            if (adapter.isEnabled) {
                blueToothCaseList[0].result = 1
                updateUI(TestResultBean(BLUETOOTH_POSITION, PASSED, blueToothCaseList))
            } else {
                val enable = adapter.enable()
                if (enable) {
                    blueToothCaseList[0].result = 1
                    updateUI(TestResultBean(BLUETOOTH_POSITION, PASSED, blueToothCaseList))
                } else {
                    blueToothCaseList[0].result = 0
                    updateUI(TestResultBean(BLUETOOTH_POSITION, FAILED, blueToothCaseList))
                }
            }

        }


        //更新UI 界面
        @Subscribe(threadMode = ThreadMode.MAIN)
        fun updateUI(resultBean: TestResultBean) {
            XLog.d("updateUI:${resultBean.position}")
            var position = resultBean.position
            var result = resultBean.result
            //START 是开始PC端发来的自动测试开始
            if (resultBean.position == START) {

                if (!isMainResume) {
                    isAutoMode = false
                    currentActivity?.finish()
                    ShowResultActivity.instance?.finish()
                    //  XLog.d("isMainResume:$isMainResume")
                }
                resetTest()
                startAutoTest(WIFI_POSITION)
                return
            }
            //STOP 是停止当前测试 跳转到summary 页面
            if (resultBean.position == STOP) {
                if (isMainResume) {
                    isAutoMode = false
                } else {
                    isAutoMode = false
                    currentActivity?.finish()
                }
                showTestSummary()
                return
            }

            //发送测试总结果给client
            if (resultBean.position == SENDSUMMARY) {
                val description = resultBean.description
                mBinder?.testFinish(description)
                return
            }


            //-1 是一个特殊的参数  用来显示重置状态的list
            if (resultBean.position == RESET) {
                adapter!!.notifyDataSetChanged()
                ryMainList!!.smoothScrollToPosition(DEFAULT)
                return
            }
            testPlanList[position].planResult = result
            testPlanList[position].clickState = true

            if (resultBean.itemCaseList != null) {
                testPlanList[position].resultItemList = resultBean.itemCaseList!!
                sendProcessData(resultBean.itemCaseList!!)
            }

            //测试异常不会滑动
            if (result == FAILED && !isAutoMode) {
                adapter!!.notifyItemChanged(position)
                return
            }

            //传感器测试不移动
            if(position == ACCELEROMETER_POSITION){
                adapter!!.notifyItemChanged(position)
                return
            }
            adapter!!.notifyItemChanged(position)
            ryMainList!!.postDelayed(
                Runnable {
                    runOnUiThread(Runnable {
                        //滑动item到顶部

                        if (position < testPlanList.size - 1) {
                            ryMainList!!.smoothScrollToPosition(position + 1)
                        } else {
                            ryMainList!!.smoothScrollToPosition(position)
                        }

                        if (isAutoMode) {

                            if (position < testPlanList.size - 1) {
                                if(position == BUTTON_POSITION){
                                    startAutoTest(position + 2)
                                }else{
                                    startAutoTest(position + 1)
                                }
                                XLog.d(" startAutoTest: ${position + 1}")
                            } else if (position == testPlanList.size - 1) {
                                showTestSummary()
                            }

                        }
                    })

                }, 500
            )


        }

        //发送进度信息
        private fun sendProcessData(itemCaseList: ArrayList<TestCase>) {
            val arrayList = ArrayList<Param>()
            for (testCase in itemCaseList) {
                arrayList.add(Param(testCase.caseId, testCase.caseName, testCase.result))
            }
            val singleTestCaseBean = SingleTestCaseBean(
                SingleAction(
                    "test_inprogress",
                    SPUtils.getInstance().getString("UUID")
                ), arrayList
            )
            try {
                val toJson = Gson().toJson(singleTestCaseBean)
                if (!TextUtils.isEmpty(toJson)) {
                    mBinder?.testProcess(toJson)
                } else {
                    XLog.d("toJson is null")
                }
            } catch (e: java.lang.Exception) {
                XLog.d(e)
            }


        }

        private fun showTestSummary() {

            stopTimer()
            ShowResultActivity.start(this, testPlanList)

        }

        private fun startAutoTest(position: Int) {
            isAutoMode = true
            when (position) {

                WIFI_POSITION -> {
                    checkWifi()
                }

                BLUETOOTH_POSITION -> {
                    checkBlueTooth()
                }

                GPS_POSITION -> {

                    checkGPS()

                }
                PROXIMITY_POSITION -> {

                    checkProximity()

                }

                BUTTON_POSITION -> {

                    checkButton()

                }
                VIBRATION_POSITION -> {
                    checkVibration()
                }

                ACCELEROMETER_POSITION -> {
                    checkAccelerometer()
                }

                CAM_POSITION -> {
                    checkCamera()
                }

                MIC_LOUD_POSITION -> {
                    checkMic()
                }
/*
                MIC_EAR_POSITION -> {
                    checkEar()
                }
*/

/*                HEADSET_POSITION -> {
                    checkHeadSet()
                }*/

                LCD_POSITION -> {
                    checkLCD()
                }

                DIGITIZER_POSITION -> {
                    checkDigitizer()
                }

                TEST_CALL_POSITION -> {
                    testCall()
                }

                BATTERY_POSITION -> {
                    checkBattery()
                }

                NFC_POSITION -> {
                    checkNFC()
                }

                TOUCH_POSITION -> {
                    checkTouchCount()
                }

                FINGER_POSITION -> {
                    checkFingerPrint()
                }


                else -> {


                    isAutoMode = false

                }
            }
        }

        private fun checkButton() {
            ButtonActivity.start(this)
        }

    var timeLong = 0L
    @SuppressLint("HandlerLeak")
    private val mCalHandler = object : Handler(){

        override fun handleMessage(msg: Message) {
            if(msg.what == 1024){
                timeLong+=1
                tv_currRunTime.text = formatTime2(timeLong)
                if(timeLong>240){
                    ToastUtils.showLong("您已超时")
                }
                sendEmptyMessageDelayed(1024,1000)
            }

        }

    }


    private fun startTimer(){
        timeLong = 0
        mCalHandler.sendEmptyMessageDelayed(1024,1000)
    }
    private fun stopTimer(){
        mCalHandler.removeMessages(1024)
    }

    private fun formatTime2(seconds: Long): String {
        return String.format("%02d:%02d", seconds / 60, seconds % 60)
    }

    private var sensorTestCaseList = arrayListOf<TestCase>(
        TestCase("Accelerometer", 9, "Accelerometer", "", 1, 0),
        TestCase("Accelerometer", 50, "Light sensor", "", 1, 0),
        TestCase("Accelerometer", 6, "Gyroscope", "", 1, 0),
        TestCase("Accelerometer", 51, "Screen Rotation", "", 1, 0)
    )

    private var mySensorListener :MySensorListener?=null

    //环境光
    private var lightSensor: Sensor? = null

    //旋转
    private var rotationSensor: Sensor? = null
    //加速度传感器
    private var accelerometerSensor: Sensor? = null

    //陀螺仪
    private var gyroscopeSensor: Sensor? = null
    private var sensorManager: SensorManager? = null

    private fun getAllSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mySensorListener = MySensorListener()
        val sensorList = sensorManager!!.getSensorList(Sensor.TYPE_ALL)
        XLog.d("sensor list :" + sensorList.size)
        accelerometerSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        //旋转矢量传感器
        rotationSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        gyroscopeSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)


        if (accelerometerSensor != null) {
            sensorManager!!.registerListener(mySensorListener, accelerometerSensor, 1000000)
        }

        if (gyroscopeSensor != null) {
            sensorManager!!.registerListener(mySensorListener, gyroscopeSensor, 50000)
        }

        if (lightSensor != null) {
            sensorManager!!.registerListener(mySensorListener, lightSensor, 100000)
        }
        if (rotationSensor != null) {
            sensorManager!!.registerListener(mySensorListener, rotationSensor, 100000)
        }

    }


    private var isAccPassed = false
    private var isGyrPassed = false
    private var isScrPassed = false
    private var isLightPassed = false
    private var isAccFirst = true
    private var isScrFirst = true
    private var isLightFirst = true
    private var isGyrX = false
    private var isGyrY = false
    private var isGyrZ = false
    private var accFirstX = 0f
    private var accFirstY = 0f
    private var accFirstZ = 0f
    private var isAccX = false
    private var isAccY = false
    private var isAccZ = false
    private var scrFirstX = 0f
    private var scrFirstY = 0f
    private var scrFirstZ = 0f
    private var isScrX = false
    private var isScrY = false
    private var isScrZ = false
    private var ligFirstX = 0f

    private var isCheckVivrate = false
    inner class MySensorListener : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
        override fun onSensorChanged(event: SensorEvent?) {
            when(event!!.sensor.type){
                Sensor.TYPE_GYROSCOPE->{
                    val X_lateral: Float = event.values[0]
                    val Y_longitudinal: Float = event.values[1]
                    val Z_vertical: Float = event.values[2]
                    if(!isCheckVivrate && isGyrPassed){
                            return
                    }

                    if (isGyrFirst) {
                        isGyrFirst = false
                        gyrFirstX = X_lateral
                        gyrFirstY = Y_longitudinal
                        gyrFirstZ = Z_vertical
                    } else {

                        val absx = kotlin.math.abs(X_lateral - gyrFirstX)
                        val absy = kotlin.math.abs(Y_longitudinal - gyrFirstY)
                        val absz = kotlin.math.abs(Z_vertical - gyrFirstZ)

                        if(absx>maxX){
                            maxX = absx
                        }
                        if(absy>maxY){
                            maxY = absy
                        }
                        if(absz>maxZ){
                            maxZ = absz
                        }

                        if(isCheckVivrate && maxX > 0.001f && maxX<1f && maxY > 0.0015f &&maxY < 1f&& maxZ > 0.005f && maxZ < 1.2f){
                            isVibPass = true
                            XLog.d("maxValueX:$maxX maxValueY:$maxY maxValueZ:$maxZ")
                        }else{
                            isVibPass = false
                        }

                        if (absx > 1) {
                            isGyrX = true
                        }
                        if (absy > 1) {
                            isGyrY = true
                        }
                        if (absz > 1) {
                            isGyrZ = true
                        }
                        isGyrPassed = isGyrX && isGyrY && isGyrZ
                        XLog.d("isGyrPassed:$isGyrPassed")
                        if (isGyrPassed&&!isCheckVivrate) {
                            sensorTestCaseList[2].result = 1
                            checkPassed()
                        }

                    }
                }

                Sensor.TYPE_ACCELEROMETER -> {
                    val X_lateral: Float = event.values[0]
                    val Y_longitudinal: Float = event.values[1]
                    val Z_vertical: Float = event.values[2]
                    if (isAccPassed) {
                        return
                    }
                    if (isAccFirst) {
                        isAccFirst = false
                        accFirstX = X_lateral
                        accFirstY = Y_longitudinal
                        accFirstZ = Z_vertical
                    } else {

                        if (kotlin.math.abs(X_lateral - accFirstX) > 1) {
                            isAccX = true
                        }
                        if (kotlin.math.abs(Y_longitudinal - accFirstY) > 1) {
                            isAccY = true
                        }
                        if (kotlin.math.abs(Z_vertical - accFirstZ) > 1) {
                            isAccZ = true
                        }
                        isAccPassed = isAccX && isAccY && isAccZ
                        XLog.d("isAccPassed:$isAccPassed")
                        if (isAccPassed) {
                            sensorTestCaseList[0].result = 1
                            sensorManager?.unregisterListener(this,accelerometerSensor)
                            checkPassed()
                        }
                    }

                }
                Sensor.TYPE_LIGHT -> {
                    val light: Float = event.values[0]
                    if (isLightPassed) {
                        return
                    }
                    if (isLightFirst) {
                        isLightFirst = false
                        ligFirstX = light
                    } else {
                        if (kotlin.math.abs(light - ligFirstX) > 60) {
                            isLightPassed = true
                        }
                        XLog.d("isLightPassed:$isLightPassed")
                        if (isLightPassed) {
                            sensorTestCaseList[1].result=1
                            sensorManager?.unregisterListener(this,lightSensor)
                            checkPassed()
                        }
                    }
                }

                Sensor.TYPE_ROTATION_VECTOR->{

                    val X_lateral: Float = event.values[0]
                    val Y_longitudinal: Float = event.values[1]
                    val Z_vertical: Float = event.values[2]

                    if (isScrPassed) {
                        return
                    }
                    if (isScrFirst) {
                        isScrFirst = false
                        scrFirstX = X_lateral
                        scrFirstY = Y_longitudinal
                        scrFirstZ = Z_vertical
                    } else {

                        if (kotlin.math.abs(X_lateral - scrFirstX) > 0.1) {
                            isScrX = true
                        }
                        if (kotlin.math.abs(Y_longitudinal - scrFirstY) > 0.1) {
                            isScrY = true
                        }
                        if (kotlin.math.abs(Z_vertical - scrFirstZ) > 0.1) {
                            isScrZ = true
                        }
                        isScrPassed = isScrX && isScrY && isScrZ
                        XLog.d("isScrPassed:$isScrPassed")
                        if (isScrPassed) {
                            sensorTestCaseList[3].result = 1
                            sensorManager?.unregisterListener(this,rotationSensor)
                            checkPassed()
                        }

                    }

                }

            }
        }
    }

    private fun checkPassed() {

        if(isGyrPassed && isAccPassed && isLightPassed && isScrPassed){
          //  sendResult(ACCELEROMETER_POSITION,PASSED,sensorTestCaseList)
            EventBus.getDefault().post(TestResultBean(ACCELEROMETER_POSITION,PASSED,sensorTestCaseList))
        }

    }


        @SuppressLint("CheckResult")
        private fun checkPermission() {

            val rxPermissions = RxPermissions(this)
            rxPermissions.requestEach(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.VIBRATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.GET_ACCOUNTS
            )
                .subscribe {
                    when {
                        it.granted -> {
                            XLog.d("权限以获取")
                        }
                        it.shouldShowRequestPermissionRationale -> {
                            XLog.d("权限获取失败")
                        }
                        else -> {
                            XLog.d("权限获取失败${it.name}")

                        }
                    }

                }

        }
        private var isMainResume = false
        override fun onResume() {
            super.onResume()
            isMainResume = true

        }

        override fun onPause() {
            super.onPause()
            isMainResume = false
        }

        override fun onDestroy() {
            super.onDestroy()
            EventBus.getDefault().unregister(this)
            sensorManager?.unregisterListener(mySensorListener)
            //     System.exit(0)
            unbindService(myConnection!!)
        }

        companion object {
            fun start(context: Context) {
                context.startActivity(Intent(context, MainActivity::class.java))
            }
        }

    }
