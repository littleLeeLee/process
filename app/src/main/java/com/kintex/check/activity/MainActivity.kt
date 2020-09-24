package com.kintex.check.activity

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.adapter.MainListAdapter
import com.kintex.check.bean.TestPlanBean
import com.kintex.check.bean.TestResultBean
import com.kintex.check.utils.CheckWifiManager
import com.kintex.check.utils.ResultCode.ACCELEROMETER_POSITION
import com.kintex.check.utils.ResultCode.BLUETOOTH_POSITION
import com.kintex.check.utils.ResultCode.BUTTON_POSITION
import com.kintex.check.utils.ResultCode.DEFAULT
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.FRONT_CAM_POSITION
import com.kintex.check.utils.ResultCode.GPS_POSITION
import com.kintex.check.utils.ResultCode.MIC_LOUD_POSITION
import com.kintex.check.utils.ResultCode.PASSED
import com.kintex.check.utils.ResultCode.PROXIMITY_POSITION
import com.kintex.check.utils.ResultCode.RESET
import com.kintex.check.utils.ResultCode.VIBRATION_POSITION
import com.kintex.check.utils.ResultCode.WIFI_POSITION
import com.kintex.check.view.SmoothLinearLayoutManager
import com.kintex.check.view.WaveView
import com.kongzue.dialog.v2.SelectDialog
import com.tbruyelle.rxpermissions2.RxPermissions
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : BaseActivity(), View.OnClickListener {

    private var testPlanList = arrayListOf<TestPlanBean>(
        TestPlanBean("WiFi","Please connect to a WiFi network.",R.mipmap.wifi,0),
        TestPlanBean("Bluetooth","Turn on your device bluetooth.",R.mipmap.wifi,0),
        TestPlanBean("GPS","Enable location services to take this test.",R.mipmap.wifi,0),
        TestPlanBean("Proximity Sensor","Once activated，wave your hand in front of the proximity sensor，" +
                "It is located near the earpiece.",R.mipmap.wifi,0),
        TestPlanBean("Buttons","Test the hardware buttons on your device.",R.mipmap.wifi,0),
        TestPlanBean("Vibration","Device vibration test",R.mipmap.wifi,0),
        TestPlanBean("Accelerometer","Make the soccer ball touch all the corner of the display by titling the device.",R.mipmap.wifi,0),
        TestPlanBean("Front Camera","Take a picture using front facing camera.Test Front Flash.",R.mipmap.wifi,0),
        TestPlanBean("Rear Camera","Take a picture using rear facing cameraDevice. Test Rear Flash.",R.mipmap.wifi,0),
        TestPlanBean("Mic Loud Speaker","Test the mic of this devices with loud speaker.",R.mipmap.wifi,0),
        TestPlanBean("Mic Ear Speaker","Test the mic of this device with ear speaker.",R.mipmap.wifi,0),
        TestPlanBean("HeadSet Test","Test the headSet of this device.",R.mipmap.wifi,0),
        TestPlanBean("LCD","Test The LCD Display and Digitizer of the screen.",R.mipmap.wifi,0),
        TestPlanBean("Digitizer","Test the Digitizer of your device.",R.mipmap.wifi,0),
        TestPlanBean("Test Call","Voice call and network connectivity test.",R.mipmap.wifi,0),
        TestPlanBean("Device Lock","FRP And Reactivation Test.",R.mipmap.wifi,0))

    private var ryMainList : RecyclerView ?= null
    private var adapter: MainListAdapter?=null
    private var reset : TextView ?= null
    private var done : TextView ?=null
    private var isAutoMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setContentView(R.layout.activity_main)
        ryMainList = findViewById(R.id.ry_mainList)
        reset = findViewById(R.id.tv_titleReset)
        done = findViewById(R.id.tv_titleDone)
        reset!!.setOnClickListener(this)
        done!!.setOnClickListener(this)
        findViewById<WaveView>(R.id.view_testStart).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_titleName).setOnClickListener(this)
        setView()

    }

    private fun setView() {


        adapter = MainListAdapter(this, testPlanList)

        val smoothLinearLayoutManager =
            SmoothLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        ryMainList!!.layoutManager = smoothLinearLayoutManager
        adapter!!.setOnItemClickListener(object : MainListAdapter.onItemClickListener{
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

                    PROXIMITY_POSITION->{

                        checkProximity()

                    }

                    BUTTON_POSITION->{

                        checkButton()
                    }

                    VIBRATION_POSITION->{

                        checkVibration()

                    }

                    ACCELEROMETER_POSITION->{

                        checkAccelerometer()
                    }

                    FRONT_CAM_POSITION->{
                        checkCamera()
                    }

                    MIC_LOUD_POSITION->{

                        checkMic()

                    }
                }
            }
        })
        ryMainList!!.adapter = adapter

        checkPermission()

    }

    private fun checkMic() {


        SpeakerActivity.start(this)

    }

    private fun checkCamera() {


        CameraActivity.start(this)


    }

    private fun checkAccelerometer() {


        AccelerometerActivity.start(this)
      //  AccelerometerPlayActivity.start(this)


    }

    private fun checkProximity() {

        ProximityActivity.start(this@MainActivity)

    }

    private var vibrator: Vibrator?=null
    private fun checkVibration() {


        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        try {
            vibrator!!.vibrate(1000000)
            val dialog = SelectDialog.build(this, "震动提醒", "请确认手机是否有震动",
                "是", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        vibrator!!.cancel()
                        dialog!!.dismiss()
                        updateUI(TestResultBean(VIBRATION_POSITION, PASSED))
                    }
                }, "否", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        vibrator!!.cancel()
                        dialog!!.dismiss()
                        updateUI(TestResultBean(VIBRATION_POSITION, FAILED))
                    }
                })
            dialog.setCanCancel(false)
            dialog.showDialog()
        }catch (e :Exception){
            XLog.d(e)

        }

    }


    override fun onClick(v: View?) {

        when(v!!.id){

            //adb
            R.id.tv_titleName->{

                    TestAdbActivity.start(this)

            }

            R.id.tv_titleReset -> {

                for ( k in testPlanList.indices) {
                    testPlanList[k].planResult = DEFAULT
                    testPlanList[k].clickState = false
                }

                updateUI(TestResultBean(RESET, DEFAULT))
            }

            R.id.tv_titleDone -> {



            }

            R.id.view_testStart ->{

                isAutoMode = true
                startAutoTest(0)

            }

        }


    }



    //检测GPS
    private fun checkGPS() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val netLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if(locationEnabled || netLocationEnabled){
            updateUI(TestResultBean(GPS_POSITION, PASSED))
        }else{
            updateUI(TestResultBean(GPS_POSITION,FAILED))
        }

    }


    //检测wifi
    private fun checkWifi() {


        if(NetworkUtils.isWifiAvailable()){
        }else{
            NetworkUtils.setWifiEnabled(true)
        }
        val checkWifiIsConnect = CheckWifiManager.checkWifiIsConnect()
        XLog.d(checkWifiIsConnect)
            if(checkWifiIsConnect){
                updateUI(TestResultBean(WIFI_POSITION,PASSED))
            }else{
                updateUI(TestResultBean(WIFI_POSITION,FAILED))
            }

    }

    //检测蓝牙
    private fun checkBlueTooth() {

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
        if(adapter.isEnabled){
            updateUI(TestResultBean(BLUETOOTH_POSITION,PASSED))
        }else{
            val enable = adapter.enable()
            if(enable){
                updateUI(TestResultBean(BLUETOOTH_POSITION,PASSED))
            }else{
                updateUI(TestResultBean(BLUETOOTH_POSITION,FAILED))
            }
        }

    }




    //更新UI 界面
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateUI(resultBean: TestResultBean){
        var position = resultBean.position
        var result = resultBean.result
        //-1 是一个特殊的参数  用来显示重置状态的list
        if(resultBean.position == RESET){
            adapter!!.notifyDataSetChanged()
            ryMainList!!.smoothScrollToPosition(DEFAULT)
            return
        }
        testPlanList[position].planResult = result
        testPlanList[position].clickState = true
        adapter!!.notifyItemChanged(position)
        //测试异常不会滑动
        if(result == FAILED && !isAutoMode){

            return
        }
        ryMainList!!.postDelayed(
            Runnable {
                runOnUiThread(Runnable {
                    //滑动item到顶部

                    if(position < testPlanList.size-1){
                        ryMainList!!.smoothScrollToPosition(position+1)
                    }else{
                        ryMainList!!.smoothScrollToPosition(position)
                    }

                    if(isAutoMode){

                        if(position < testPlanList.size-1 ){
                            startAutoTest(position + 1)
                        }

                    }
                })

            },500
        )



    }

    private fun startAutoTest(position: Int) {

        when(position){

            WIFI_POSITION->{
                checkWifi()
            }

            BLUETOOTH_POSITION->{
                checkBlueTooth()
            }

            GPS_POSITION->{

                checkGPS()

            }
            PROXIMITY_POSITION->{

                checkProximity()

            }

            BUTTON_POSITION->{

                checkButton()

            }
            VIBRATION_POSITION->{
                checkVibration()
            }

            else->{

                isAutoMode = false

            }


        }


    }

    private fun checkButton() {


        ButtonActivity.start(this)

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

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

    }

    companion object{
       fun start(context: Context){
           context.startActivity(Intent(context,MainActivity::class.java))
       }
   }

}
