package com.kintex.check.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.android.internal.telephony.ITelephony
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.CaseType

import com.kintex.check.utils.CaseId
import com.kintex.check.utils.NfcUtils
import com.kintex.check.utils.ResultCode
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.PASSED
import kotlinx.android.synthetic.main.activity_lcd.*
import kotlinx.android.synthetic.main.activity_lcd.btn_failed
import kotlinx.android.synthetic.main.activity_lcd.btn_passed
import kotlinx.android.synthetic.main.activity_lcd.tv_digitizerFailed
import kotlinx.android.synthetic.main.activity_lcd.tv_failed1
import kotlinx.android.synthetic.main.activity_lcd.tv_failed2
import kotlinx.android.synthetic.main.activity_lcd.viewGridView
import kotlinx.android.synthetic.main.title_include.*

class LCDActivity : BaseActivity(), View.OnClickListener, View.OnTouchListener {

    private var sensorManager : SensorManager? = null
    private var lightSensor : Sensor?=null
    private var  proximitySensor : Sensor?=null
    private var mySensorListener : MySensorListener?=null


    private var currentPositon = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
        setContentView(R.layout.activity_lcd)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mySensorListener = MySensorListener()
        prepareCase()
        startCaseTest(currentPositon)

    }

    private fun startCaseTest(currentPositon: Int) {
        val typeItem = caseType!!.typeItems[currentPositon]
        when(typeItem.caseId){

            CaseId.NFC.id->{
                showNFCDialog()
            }

            CaseId.MultiTouch.id->{
                showTouchCount()
            }

            CaseId.TouchPanel.id->{
                showDigitizer()
            }

            CaseId.Display.id->{
                showColorView()
            }

            CaseId.ProximitySensor.id , CaseId.LightSensor.id->{
               checkSensorPass()
            }

        }


    }

    private fun showColorView() {
        view_lcd.visibility = View.VISIBLE
    }

    private var  hasProx = false
    private var hasLight = false

    private fun prepareCase() {
        //监听电话
        teleManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        phoneListener = MyPhoneStateListener()
        teleManager!!.listen(
                phoneListener,
                PhoneStateListener.LISTEN_CALL_STATE
        )
        //NFC
        var hasNFC =  caseType!!.typeItems.find {
            it.caseId == CaseId.NFC.id
        }
        if(hasNFC != null){
            val nfcUtils = NfcUtils(this)
        }
        //距离感应器
        var hasPro = caseType!!.typeItems.find {
            it.caseId == CaseId.ProximitySensor.id
        }
        XLog.d("hasProx:$hasProx")
        if(hasPro != null){
            hasProx = true
            proximitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
            if(proximitySensor != null){
                sensorManager!!.registerListener(mySensorListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }else{
            hasProx = false
            isProFinish = true
            tv_proximityValue.visibility =View.GONE
            btn_proPass.visibility = View.GONE
            tv_proFail.visibility = View.GONE
            btn_call.visibility = View.GONE
            textView25.visibility = View.GONE
        }
        //光线感应器
        var hasLightSensor = caseType!!.typeItems.find {
            it.caseId == CaseId.LightSensor.id
        }
        val defaultSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        if(defaultSensor == null){
            ToastUtils.showLong("没有找到光线感应器")
        }
        XLog.d("hasLight:$hasLight")
        if(hasLightSensor != null){
            hasLight = true
            lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
            if (lightSensor != null) {
                sensorManager!!.registerListener(mySensorListener, lightSensor, 100000)
            }
        }else{
            hasLight = false
            isLightFinish = true
            tv_lightValue.visibility = View.GONE
            btn_lightPass.visibility = View.GONE
            tv_lightFail.visibility = View.GONE
            textView27.visibility = View.GONE
        }

        tv_lightFail.setOnClickListener {
            isLightFinish = true
            sendTestResult(FAILED,CaseId.LightSensor.id)
            if(isProFinish){
                doNext(view_sensor)
            }

        }
        btn_lightPass.setOnClickListener {

            isLightFinish = true
            sendTestResult(PASSED,CaseId.LightSensor.id)
            if(isProFinish){
                doNext(view_sensor)
            }

        }
        tv_proFail.setOnClickListener {
            isProFinish = true
            sendTestResult(FAILED,CaseId.ProximitySensor.id)
            if(isLightFinish){
                doNext(view_sensor)
            }
        }
        btn_proPass.setOnClickListener {

            isProFinish = true
            sendTestResult(PASSED,CaseId.ProximitySensor.id)
            if(isLightFinish){
                doNext(view_sensor)
            }

        }


        //红绿蓝
        var hasDisplay = caseType!!.typeItems.find {
            it.caseId == CaseId.Display.id
        }
        if(hasDisplay != null){
            tv_colorView.setOnClickListener(this)
            tv_lcdPass.setOnClickListener(this)
            tv_lcdFail.setOnClickListener(this)
        }
        //多点触控
        var hasMultiTouch = caseType!!.typeItems.find {
            it.caseId == CaseId.MultiTouch.id
        }
        //滑屏幕
        var hasTouchPanel =  caseType!!.typeItems.find {
            it.caseId == CaseId.TouchPanel.id
        }


    }

    private  var isNFCFinsh = false
    var dialog : AlertDialog ?=null
    private fun showNFCDialog() {
        dialog = AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("提示").setCancelable(false).setMessage("请把手机靠近NFC设备")
                .setPositiveButton("失败") { dialog, which ->
                    isNFCFinsh = true
                    sendTestResult(PASSED, CaseId.NFC.id)
                    doNext(null)
                }.show()
    }

    private fun doNext(view: View?) {
        view?.visibility = View.GONE
        if(view?.id == R.id.view_sensor){
            var k =0
            if(hasLight){
               k++
            }
            if(hasProx){
                k++
            }
            currentPositon+=k
        }else{
            currentPositon++
        }

        if( currentPositon== caseType!!.typeItems.size){
            finish()
        }else{
            startCaseTest(currentPositon)
        }
    }


    private var clickCount = 0

    override fun onClick(v: View?) {

        when(v!!.id){

            R.id.tv_lcdPass->{
                sendTestResult(PASSED,CaseId.Display.id)
                doNext(view_lcd)
            }

            R.id.tv_lcdFail->{
                sendTestResult(FAILED,CaseId.Display.id)
                doNext(view_lcd)
            }

            R.id.tv_colorView->{
                clickCount++
                setColor()
            }

        }
    }

    fun endCall() {
        // TODO Auto-generated method stub
        // "android.os.ServiceManager" 是系统服务 ，一般方法没法用
        try {
            // 用反射得到该类的对象
            val clazz = Class.forName("android.os.ServiceManager")
            //	System.out.println("反射对象");
            // 获得用其方法
            /**
             * getService 是要调用方法的全名 String 是要传入参数的类型
             */
            val method = clazz.getMethod("getService", String::class.java)
            //	System.out.println("得到方法");
            /**
             * 调用方法 1,是调用该方法的对象 如果方法是静态的写null 2.执行方法是 需要的真实参数
             */
            val obj = method.invoke(null, TELEPHONY_SERVICE)
            //	System.out.println("调用方法");
            /**
             * 调用远程服务 需要aidl
             */
            val ite: ITelephony = ITelephony.Stub.asInterface(obj as IBinder)
            // 挂电话
            ite.endCall()
            XLog.d("执行挂电话方法")
        } catch (e: java.lang.Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }


    private fun callPhone() {
        ToastUtils.showLong("请拨打电话，并将手机靠近脸部2秒")
        val num ="112"
        var intent: Intent? = null
        val uri: Uri = Uri.parse("tel:$num")
        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ToastUtils.showShort("请到设置中打开电话权限")
            intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
            return
        }
        intent = Intent(Intent.ACTION_CALL,uri)
        startActivity(intent)
    /*    view_sensor.postDelayed(Runnable {
            endCall()
        },3000)*/
    }

    private var lastCallState = 0
    private var currCallState = 0
    private var teleManager : TelephonyManager ?=null
    private var phoneListener : MyPhoneStateListener?=null

    inner  class MyPhoneStateListener : PhoneStateListener() {
        override fun onCallStateChanged(
                state: Int,
                incomingNumber: String
        ) {

            lastCallState = currCallState
            currCallState = state

            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {

                    if (lastCallState == TelephonyManager.CALL_STATE_OFFHOOK) {
                        XLog.d("挂电话！")
                     //   showChooseDialog()
                    }
                }
                TelephonyManager.CALL_STATE_RINGING -> {

                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {

                }
                else -> {
                }
            }
        }
    }

    private fun sendTestResult(result: Int,id: Int) {
        sendCaseResult(id,result, ResultCode.MANUAL)
    }

    private var viewList = ArrayList<TextView>()
    private fun showDigitizer() {
        view_digitizer.visibility = View.VISIBLE

        tv_digitizerFailed.setOnClickListener {
            sendTestResult(FAILED,CaseId.TouchPanel.id)
            doNext(view_digitizer)
        }

        tv_failed1.setOnClickListener {
            sendTestResult(FAILED,CaseId.TouchPanel.id)
            doNext(view_digitizer)
        }

        tv_failed2.setOnClickListener {
            sendTestResult(FAILED,CaseId.TouchPanel.id)
            doNext(view_digitizer)
        }

        for( i in 0..12){
            for(j in 0..6){
                val textView = layoutInflater.inflate(R.layout.item_textview, null) as TextView
                textView.tag =  false
                textView.setOnTouchListener(this) //每个textview都监听触摸事件
                var rowSpec = GridLayout.spec(i,1.0f) //行坐标和比重rowweight,用float表示的
                var columnSpec = GridLayout.spec(j,1.0f)//列坐标和columnweight
                var params = GridLayout.LayoutParams(rowSpec,columnSpec)
                viewList.add(textView)
                viewGridView.addView(textView,params)
            }
        }

    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        readWrite(intent)
    }
    //读取NFC卡id
    private fun readWrite(intent: Intent?) {

        try {
            // 检测卡的id
            val id = NfcUtils.readNFCId(intent)
            // NfcUtils中获取卡中数据的方法
            val result = NfcUtils.readNFCFromTag(intent)
            XLog.d( "id:$id")
            if(!TextUtils.isEmpty(id)){
                dialog?.dismiss()
                isNFCFinsh = true
                sendTestResult(PASSED,CaseId.NFC.id)
                doNext(null)
                try {
                    if (NfcUtils.mNfcAdapter != null) {
                        NfcUtils.mNfcAdapter.disableForegroundDispatch(this)
                        NfcUtils.mNfcAdapter = null
                    }
                }catch ( e :java.lang.Exception){
                    dialog?.dismiss()
                    isNFCFinsh = true
                }

            }
            // 往卡中写数据
            val data = "1这是写入的数据2"
            //   NfcUtils.writeNFCToTag(this, data, intent)
        } catch (e: Exception) {
            dialog?.dismiss()
            isNFCFinsh = true
            sendTestResult(PASSED,CaseId.NFC.id)
            doNext(null)
            e.printStackTrace()
        }

    }

    private fun showTouchCount() {
     //   view_digitizer.visibility = View.GONE
        view_touch.visibility = View.VISIBLE
        tv_titleName.text = "LCD TEST"

        btn_failed.setOnClickListener {
            sendTestResult(FAILED,CaseId.MultiTouch.id)
            doNext(view_touch)

        }

        btn_passed.setOnClickListener {
            sendTestResult(PASSED,CaseId.MultiTouch.id)
            doNext(view_touch)
        }


    }

    private fun setColor() {

        when(clickCount){

            //
            1->{
                tv_colorView.setBackgroundColor(Color.GREEN)
            }
            2->{
                tv_colorView.setBackgroundColor(Color.RED)
            }
            3->{
                tv_colorView.setBackgroundColor(Color.BLACK)
            }
            4->{
                tv_lcdPass.visibility = View.VISIBLE
                tv_lcdFail.visibility = View.VISIBLE
                tv_colorView.setBackgroundColor(Color.WHITE)
            }


        }

    }
    private var touchCount =0
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {

        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                if(!(v!!.tag as Boolean)){
                    v!!.tag = true
                    touchCount++
                    v!!.background = resources.getDrawable(R.drawable.tv_bg_blue_empty)
                    checkCount()
                }


                return true
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        return false

    }

    private var isCountPass = false

    private fun checkCount() {

        if(touchCount == 91){
            isCountPass = true
            sendTestResult(PASSED,CaseId.TouchPanel.id)
            doNext(view_digitizer)
        }else{
            isCountPass = false
        }

    }


    var pointerCount = 0
    var hasSend = false
    override fun onTouchEvent(event: MotionEvent): Boolean {

        pointerCount  = event.pointerCount
        if(event.action == MotionEvent.ACTION_UP){
            pointerCount  = 0
        }

        runOnUiThread {

            tv_fingerCount.text = "数量:$pointerCount"
            if(pointerCount>=2&&!hasSend){
                hasSend = true
                sendTestResult(PASSED,CaseId.MultiTouch.id)
                doNext(view_touch)
            }
        }
        return true
    }

    private fun checkSensorPass() {
        if(isLightFinish  && isProFinish){
           doNext(view_sensor)
        }else{
            if(!isProFinish){

            }
            view_sensor.visibility = View.VISIBLE
            btn_call.setOnClickListener {
                callPhone()
            }
        }


    }

    private var isLightFirst = true
    private var ligFirstX = 0f
    private var isLightFinish = false

    private var isProFirst = true
    private var proFirstX = 0f
    private var isProFinish = false

    inner class MySensorListener : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
        override fun onSensorChanged(event: SensorEvent?) {
            when(event!!.sensor.type){

                Sensor.TYPE_LIGHT -> {
                    val light: Float = event.values[0]
                    runOnUiThread {
                        tv_lightValue.text = "光线值：$light"
                    }
                    if (isLightFirst) {
                        isLightFirst = false
                        ligFirstX = light
                    } else {
                        if (kotlin.math.abs(light - ligFirstX) > 15) {
                            isLightFinish = true
                            sensorManager?.unregisterListener(mySensorListener,lightSensor)
                            sendTestResult(PASSED,CaseId.LightSensor.id)
                            runOnUiThread {
                                tv_lightValue.text = "光线值： 通过"
                                if(isProFinish&&isCountPass){
                                    doNext(view_sensor)
                                }
                            }
                        }
                    }
                }
               Sensor.TYPE_PROXIMITY->{
                    var result = event.values[0]
                   runOnUiThread {
                       tv_proximityValue.text = "距离值：$result"
                   }
                    if(isProFirst){
                        isProFirst = false
                        proFirstX = result
                    }
                   XLog.d("proFirstX:$proFirstX, result$result")
                    runOnUiThread {
                        if(result != proFirstX){
                            isProFinish = true
                            sendTestResult(PASSED,CaseId.ProximitySensor.id)
                            sensorManager?.unregisterListener(mySensorListener,proximitySensor)
                            runOnUiThread {
                                tv_proximityValue.text = "距离值： 通过"
                                if(isLightFinish&&isCountPass){
                                    doNext(view_sensor)
                                }
                            }
                        }
                    }

                }


            }
        }
    }


    override fun onResume() {
        super.onResume()
        //设定intentfilter和tech-list。如果两个都为null就代表优先接收任何形式的TAG action。也就是说系统会主动发TAG intent。
        if (NfcUtils.mNfcAdapter != null) {
            NfcUtils.mNfcAdapter.enableForegroundDispatch(
                    this,
                    NfcUtils.mPendingIntent,
                    NfcUtils.mIntentFilter,
                    NfcUtils.mTechList
            )
        }

    }

    override fun onPause() {
        super.onPause()
        if (NfcUtils.mNfcAdapter != null) {
            NfcUtils.mNfcAdapter.disableForegroundDispatch(this);
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        teleManager?.listen(phoneListener,
                PhoneStateListener.LISTEN_NONE)
        phoneListener = null
            testNext()
        NfcUtils.mNfcAdapter = null
        if(proximitySensor != null){
            sensorManager?.unregisterListener(mySensorListener,proximitySensor)
        }
        if(lightSensor != null){
            sensorManager?.unregisterListener(mySensorListener,lightSensor)
        }
    }


    companion object{
        var caseType : CaseType ?=null
        fun start(context: Context, caseType: CaseType?){
            this.caseType = caseType
            context.startActivity(Intent(context,LCDActivity::class.java))
        }
    }


}