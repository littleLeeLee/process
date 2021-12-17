package com.kintex.check.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothManager
import android.content.*
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.*
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kintex.check.R
import com.kintex.check.adapter.NewMainListAdapter
import com.kintex.check.adapter.StickHeaderDecoration
import com.kintex.check.bean.*
import com.kintex.check.other.AdbTestActivity
import com.kintex.check.service.AdbConnectService
import com.kintex.check.utils.*
import com.kintex.check.utils.ResultCode.AUTO
import com.kintex.check.utils.ResultCode.DEFAULT
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.PASSED
import com.oubowu.stickyitemdecoration.DividerHelper
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.newmain.*
import kotlinx.android.synthetic.main.title_include.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setContentView(R.layout.newmain)
        tv_version.text = "K-Check ${AppUtils.getAppVersionName()}"
        startAdbService()
        btn_reset.setOnClickListener(this)
        btn_done.setOnClickListener(this)
        tv_start.setOnClickListener(this)
        checkPermission()
        var strFromAssets = SPUtils.getInstance().getString("json")
        if(TextUtils.isEmpty(strFromAssets)){
            strFromAssets = MyUtils.getStrFromAssets(this, "start.json")
        }
        XLog.d("json = $strFromAssets")
        val gson = Gson()
        val testData = gson.fromJson(strFromAssets, NewTestPlanBean::class.java)
        getJsonData(testData)
    }

    private var autoTestCount = 0
    private var manualTestCount = 0
    private var totalCount = 0

    private var caseList =  ArrayList<CaseType>()
    private fun getJsonData(testData : NewTestPlanBean) {

        totalCount = 0
        caseList = ArrayList()
        val operations = testData.action.operations

        SPUtils.getInstance().put("UUID",testData.action.udid)
        if(operations.isNotEmpty()){
            for ( k in  operations.indices ){
                val resId = IDUtils.getResId(operations[k].testTypeName, R.string::class.java)
               XLog.d("operations[k].testTypeName:${operations[k].testTypeName}")
                 val autoCaseType = CaseType(0-1-k,resources.getString(resId) , ArrayList(), DEFAULT)
                 caseList.add(autoCaseType)
                for (type in  operations[k].types){
                    var typeId =  IDUtils.getResId(type.name,R.string::class.java)
                    if(typeId != -1){
                        type.name = getString(typeId)
                    }

                    for (case in type.typeItems){
                        case.desName = case.caseName
                        var caseId =  IDUtils.getResId(case.caseName,R.string::class.java)
                        if(caseId != -1){
                            case.caseName = getString(caseId)
                        }

                    }
                }
                //自动测试列表
                 if(operations[k].testTypeName == "AutomaticTesting"){
                     autoTestCount = operations[k].types.size
                     caseList.addAll(operations[k].types)
                 }else{
                     //手动测试列表
                     manualTestCount += operations[k].types.size
                     caseList.addAll(operations[k].types)
                 }
                //总数量
                 for (j in operations[k].types.indices){
                     XLog.d("size: ${operations[k].types[j].typeItems.size}")
                     totalCount += operations[k].types[j].typeItems.size
                 }
            }
        }

        XLog.d("totalCount : $totalCount autoCount : $autoTestCount caselist:${caseList.size}")
     //   ToastUtils.showShort("totalCount : $totalCount caselist:${caseList.size}")
        setDataToView(caseList)

    }

    private fun showTestingDialog(plan: NewTestPlanBean) {

        AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("提示").setCancelable(false).setMessage("本地已有测试记录，是否覆盖")
                .setPositiveButton("是") { dialog, which ->
                    SPUtils.getInstance().clear()
                    getJsonData(plan)
                }.setNegativeButton("否") { dialog, which ->

                }.show()

    }

    private var mainListAdapter: NewMainListAdapter ?=null
    private fun setDataToView(caseList: java.util.ArrayList<CaseType>) {
        if(mainListAdapter != null){
            mainListAdapter = null
          //  ry_mainTestList.removeAllViews()
        }
        for (case in caseList){
            for (item in case.typeItems){
                when(item.caseId){

                    1024,1025,1026,1027,1028,1029->{
                        item.description =  SPUtils.getInstance().getString(""+item.caseId)
                    }
                    else ->{
                        item.result =  SPUtils.getInstance().getInt(""+item.caseId,2)
                    }

                }

            //    XLog.d("result:"+ item.result)
            }
        }
        mainListAdapter = NewMainListAdapter(this, caseList)
        val linearLayoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        ry_mainTestList.setItemViewCacheSize(40)
        ry_mainTestList.layoutManager = linearLayoutManager
        ry_mainTestList.adapter = mainListAdapter
        mainListAdapter!!.setOnItemClickListener(object : NewMainListAdapter.onItemClickListener {
            override fun onItemClick(view: View, position: Int) {
              //  ToastUtils.showShort("position:$position"+caseList[position].name)
                getTestItem(position)
            }
        })
        ry_mainTestList.addItemDecoration(StickHeaderDecoration(ry_mainTestList))

    }

    @Synchronized
    private fun getTestItem(position: Int) {
        XLog.d("position:$position")
        runOnUiThread {
            ry_mainTestList.scrollToPosition(position)
        }
        val typeItems = caseList[position].typeItems
        XLog.d("nextName = ${caseList[position].name}")
        when(caseList[position].name){
            getString(R.string.Connection)->{
                for (caseItem in typeItems!!){
                    when(caseItem.caseId){
                        CaseId.WIFI.id->{
                            checkWifi()
                        }
                        CaseId.BlueTooth.id->{
                            checkBlueTooth()
                        }
                        CaseId.GPS.id->{
                            checkGPS()
                        }
                        CaseId.SimReader.id->{
                            checkSim()
                        }
                    }
                }
            }
            getString(R.string.Sensor)->{
                getAllSensor()
            //    checkNFC()
            }
            "Secure"->{

            }

            getString(R.string.ScreenTest)->{
               LCDActivity.start(this,caseList[position])
            }
            getString(R.string.Button)->{
                ButtonActivity.start(this)
            }
            getString(R.string.CameraTest)->{
                CameraActivity.start(this)
            }
            getString(R.string.AudioTest)->{
               AudioTestActivity.start(this )
              //  SpeakerActivity.start(this)
            }
            "Spen Test"->{
                if(isAutoTest){
                    testNext()
                }
            }

            getString(R.string.Battery)->{
                BatteryActivity.start(this,caseList[position])
            }

            getString(R.string.Headset)->{
                HeadSetActivity.start(this)
            }

            else->{
                XLog.d("else: ${caseList[position].name}")
                if(isAutoTest){
                    testNext()
                }
            }
        }


    }


    override fun onClick(v: View?) {

        when (v) {
            //adb
            tv_titleName -> {
                startActivity(Intent(this, AdbTestActivity::class.java))
            }

            btn_reset -> {
                resetTest()
            //    mBinder
             //   mBinder!!.testProcess("{\"action\":{\"name\":\"print_label\",\"udid\":\"xxxxx\",\"print\":\"0\"}}")
            }


            tv_start->{
                resetData()
                myProcessView.setProcess(0f)
                startBackgroundTest()
             //   AudioActivity.start(this)
            }

            btn_done->{

             //  TestAdbActivity.start(this)
                sendFinishData()
                stopTimer()
            }


        }
    }

    private var currentAutoTestPosition = 1
    //开始后台测试
    private fun startBackgroundTest() {
        tv_start.text = "Stop"
        isAutoTest = true
        currentAutoTestPosition = autoTestCount+2
        getTestItem(currentAutoTestPosition)
        Thread{
            kotlin.run {
                for (k in 1 .. autoTestCount){
                    getTestItem(k)
                }
            }
        }.start()
        startTimer()
    }
    //ADB 通信方法
    @Synchronized
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun connectService(receive: ReceiveAdbBean) {
        when(receive.name){
            "negotiation"->{
                var plan =  Gson().fromJson(receive.contentStr,NewTestPlanBean::class.java)
                if(isAutoTest){
                    showTestingDialog(plan)
                }else{
                    if(SPUtils.getInstance().getInt("result",0) == 1024){
                        showTestingDialog(plan)
                    }else{
                        SPUtils.getInstance().put("json",receive.contentStr)
                        getJsonData(plan)
                    }

                }

            }

            "get_result"->{
                sendFinishData()
            }

        }
    }



    //更新UI 界面
    @Synchronized
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateCaseResult(caseResultBean: CaseResultBean) {
        var find : TypeItem? = null
        for (caseType in caseList){
            find = caseType.typeItems!!.find {
                it.caseId == caseResultBean.caseId
            }
            if(find != null){
                break
            }
        }
        if(find == null){
            ToastUtils.showShort("can not find caseId: ${caseResultBean.caseId}")
        }else{

            if(find.caseId != 1024 && find.caseId !=1025&& find.caseId !=1026&& find.caseId !=1027 && find.caseId !=1028 && find.caseId !=1029){
                find.result = caseResultBean.result
                runOnUiThread {
                    mainListAdapter!!.notifyDataSetChanged()
                }
                SPUtils.getInstance().put(""+caseResultBean.caseId,caseResultBean.result)
            }else{
                XLog.d("caseResultBeanID${caseResultBean.dis}")
                find.description = caseResultBean.dis
                SPUtils.getInstance().put(""+caseResultBean.caseId,caseResultBean.dis)
            }

            XLog.d(" ID:${find.caseId}")
            saveResult()
            checkResult()


        }


    }
    //保存数据到本地
    private fun saveResult() {
        SPUtils.getInstance().put("result",1024)
    }

    @Synchronized
    private fun checkResult() {
        var testCount =0
        for ( k in  caseList.indices ){

            for (find in caseList[k].typeItems){
                if(find.caseId == 1024 || find.caseId ==1025|| find.caseId ==1026|| find.caseId ==1027 || find.caseId ==1028 || find.caseId ==1029){

                    if(!TextUtils.isEmpty(find.description) ){
                        testCount++
                   }

                }else{
                 //   XLog.d("result:${find.result}")
                    if(find.result == PASSED || find.result == FAILED){
                        testCount++
                    }

                }

            }
        }
        XLog.d("total:$testCount ")
    //    ToastUtils.showShort("count: $totalCount total:$testCount ")
        if(testCount == totalCount){
            XLog.d("total test finish")
            runOnUiThread {
                ToastUtils.showShort("测试完成")
                tv_start.text = "Start"
                sendFinishData()
                stopTimer()
                if(isFirst){
                   showChoosePrint()
                }

            }

        }
    }

    private var  isFirst = true
    var isAutoTest = false
    //自动跳转下一个测试
    @Synchronized
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun doNextTest(checkBean: CheckBean) {
        if(isAutoTest){
            currentAutoTestPosition ++
            XLog.d("currentAutoTestPosition:$currentAutoTestPosition")
            if( currentAutoTestPosition<caseList.size){
                getTestItem(currentAutoTestPosition)
            }else{
                XLog.d("autoTest test finish")
                runOnUiThread {
                    ry_mainTestList.scrollToPosition(caseList.size-1)
                }
            }
        }



    }

    private fun sendFinishData() {
        val arrayList = ArrayList<TestCase>()
        for (case in caseList){

            if(case.typeItems!= null){
                for (item in case.typeItems){
                    var testCase =   TestCase(item.caseId,item.desName,item.description ?: "",item.enable,item.result ?: 2)
                    arrayList.add(testCase)
                    //   Log.d("111", item.toString())
                }
            }


        }
        val testSummaryBean = TestSummaryBean(Action("test_inprogress",SPUtils.getInstance().getString("UUID")),arrayList)
        val toJson = Gson().toJson(
                testSummaryBean,
                object : TypeToken<TestSummaryBean>() {
                }.type
        )
        mBinder?.testFinish(toJson)
     //   ToastUtils.showShort("已发送完成json")
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


        }


    var timeLong = 0L
    @SuppressLint("HandlerLeak")
    private val mCalHandler = object : Handler(){

        override fun handleMessage(msg: Message) {
            if(msg.what == 1024){
                timeLong+=1
                tv_time.text = formatTime2(timeLong)
                myProcessView.updateProcess()
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
        timeLong = 0
        mCalHandler.removeMessages(1024)
    }

    private fun formatTime2(seconds: Long): String {
        return String.format("%02d:%02d", seconds / 60, seconds % 60)
    }


    private var mySensorListener :MySensorListener?=null

    //环境光
 //   private var lightSensor: Sensor? = null

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
        //旋转矢量传感器  磁力计
        rotationSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        gyroscopeSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        if (accelerometerSensor != null) {
            XLog.d("accelerometerSensor")
            sensorManager!!.registerListener(mySensorListener, accelerometerSensor, 1000000)
        }

        if (gyroscopeSensor != null) {
            XLog.d("gyroscopeSensor")
            sensorManager!!.registerListener(mySensorListener, gyroscopeSensor, 50000)
        }

/*        if (lightSensor != null) {
            sensorManager!!.registerListener(mySensorListener, lightSensor, 100000)
        }*/
        if (rotationSensor != null) {
            XLog.d("rotationSensor")
            sensorManager!!.registerListener(mySensorListener, rotationSensor, 100000)
        }

    }

    private fun showChoosePrint() {
        AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("提示").setCancelable(false).setMessage("是否打印标签")
                .setPositiveButton("打印") { dialog, which ->
                    var print = PrintBean(PrintAction("print_label","1",SPUtils.getInstance().getString("UUID")))
                    val toJson = Gson().toJson(
                            print,
                            object : TypeToken<PrintBean>() {
                            }.type
                    )
                mBinder?.testFinish(toJson)
                    isAutoTest = false
                //    ToastUtils.showShort("已发送打印json 1")
                }.setNegativeButton("取消") { dialog, which ->
                    var print = PrintBean(PrintAction("print_label","0",SPUtils.getInstance().getString("UUID")))
                    val toJson = Gson().toJson(
                            print,
                            object : TypeToken<PrintBean>() {
                            }.type
                    )
                    mBinder?.testFinish(toJson)
                    isAutoTest = false
            //        ToastUtils.showShort("已发送打印json 0")
                }
                .setOnDismissListener (object : DialogInterface.OnDismissListener{
                    override fun onDismiss(dialog: DialogInterface?) {
                    }
                })
                .show()
                isFirst = false
    }


    private var isAccPassed = false
    private var isGyrPassed = false
    private var isScrPassed = false
    private var isAccFirst = true
    private var isScrFirst = true
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


    fun resetData(){
         isAccPassed = false
         isGyrPassed = false
         isScrPassed = false
         isAccFirst = true
         isScrFirst = true
         isGyrX = false
         isGyrY = false
         isGyrZ = false
         accFirstX = 0f
         accFirstY = 0f
         accFirstZ = 0f
         isAccX = false
         isAccY = false
         isAccZ = false
         scrFirstX = 0f
         scrFirstY = 0f
         scrFirstZ = 0f
         isScrX = false
         isScrY = false
         isScrZ = false
        isFirst = true
    }
    inner class MySensorListener : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
        override fun onSensorChanged(event: SensorEvent?) {
            when(event!!.sensor.type){
                //陀螺仪
                Sensor.TYPE_GYROSCOPE->{
                    val X_lateral: Float = event.values[0]
                    val Y_longitudinal: Float = event.values[1]
                    val Z_vertical: Float = event.values[2]
                    if(isGyrPassed){
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
                        if (isGyrPassed) {
                            sensorManager?.unregisterListener(this,gyroscopeSensor)
                            updateCaseResult(CaseResultBean(CaseId.Gyroscope.id, PASSED, AUTO))
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
                            sensorManager?.unregisterListener(this,accelerometerSensor)
                            updateCaseResult(CaseResultBean(CaseId.Accelerometer.id, PASSED,AUTO))
                        }
                    }

                }
/*                Sensor.TYPE_LIGHT -> {
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
                }*/

                //磁力计
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
                            sensorManager?.unregisterListener(this,rotationSensor)
                            updateCaseResult(CaseResultBean(CaseId.Magnetometer.id, PASSED,AUTO))
                        }

                    }

                }

            }
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
            stopTimer()
            sensorManager?.unregisterListener(mySensorListener)
            //     System.exit(0)
            unbindService(myConnection!!)
        }

        companion object {
            fun start(context: Context) {
                context.startActivity(Intent(context, MainActivity::class.java))
            }
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


    private var currentPosition = 0

    private var gyrFirstX = 0f
    private var gyrFirstY = 0f
    private var gyrFirstZ = 0f
    private var isGyrFirst = true


    private var maxX = 0f
    private var maxY = 0f
    private var maxZ = 0f


    private fun resetTest() {
        //stopTimer()
        for (caseType in caseList){
            for (case in caseType.typeItems!!){
                when(case.caseId){
                    1024,1025,1026,1027,1028,1029->{
                        case.description =  ""
                    }
                    else ->{
                        case.result = DEFAULT
                    }
                }

            }
        }
        SPUtils.getInstance().clear()
        myProcessView.setProcess(0f)
        stopTimer()
        mainListAdapter?.notifyDataSetChanged()
        tv_start.text = "Start"
        tv_time.text = "00:00"
        if(sensorManager!=null){
            if(mySensorListener !=null){
                XLog.d("unregisterListener")
                sensorManager!!.unregisterListener(mySensorListener)
                mySensorListener = null
            }
            sensorManager = null
        }
        resetData()
    }


    //检测GPS
    private fun checkGPS() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val netLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        var result = if (locationEnabled || netLocationEnabled) {
            PASSED
        } else {
            FAILED
        }
        updateCaseResult(CaseResultBean(CaseId.GPS.id,result,AUTO))
        XLog.d("gps")
    }


    //检测wifi
    private fun checkWifi() {
        if (NetworkUtils.isWifiAvailable()) {
        } else {
            NetworkUtils.setWifiEnabled(true)
        }
        val checkWifiIsConnect = CheckWifiManager.checkWifiIsConnect()
        XLog.d(checkWifiIsConnect)
        var result = 0
        result = if (checkWifiIsConnect) {
            PASSED
        } else {
            FAILED
        }
        updateCaseResult(CaseResultBean(CaseId.WIFI.id, result,AUTO))
        XLog.d("WIFI")
    }


    //检测蓝牙
    private fun checkBlueTooth() {

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
        var result = 0
        result = if (adapter.isEnabled) {
            PASSED
        } else {
            val enable = adapter.enable()
            if (enable) {
                PASSED
            } else {
                FAILED
            }
        }
        updateCaseResult(CaseResultBean(CaseId.BlueTooth.id,result,AUTO))
        XLog.d("checkBlueTooth")
    }

    //检测SIM卡
    private fun checkSim(){

        var teleManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simState = teleManager.simState
        var result = if(simState == TelephonyManager.SIM_STATE_ABSENT || simState == TelephonyManager.SIM_STATE_UNKNOWN){
            FAILED
        }else{
            PASSED
        }
        updateCaseResult(CaseResultBean(CaseId.SimReader.id,result,AUTO))
    }

    class SpaceItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
        private val ATTRS = intArrayOf(android.R.attr.listDivider)
        private val mDivider: Drawable?
        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            val childCount = parent.childCount
            for (i in 0 until childCount) {
                val child = parent.getChildAt(i)
                val params = child.layoutParams as RecyclerView.LayoutParams
                DividerHelper.drawBottomAlignItem(c, mDivider, child, params)
            }
        }

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val type = parent.adapter!!.getItemViewType(parent.getChildAdapterPosition(view))
            XLog.d("type:$type")
            if (type != 1) {
                outRect.set(0, 0, 0, 0)
            } else {
                outRect.set(0, 0, 0, mDivider!!.getIntrinsicHeight())
            }
        }

        init {
            val a = context.obtainStyledAttributes(ATTRS)
            mDivider = a.getDrawable(0)
            a.recycle()
        }
    }

    }
