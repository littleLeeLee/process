package com.kintex.check.activity

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.os.Vibrator
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.CaseType
import com.kintex.check.bean.KeyEventBean
import com.kintex.check.bean.TestCase
import com.kintex.check.recevier.KeyEventReceiver
import com.kintex.check.recevier.ScreenReceiver
import com.kintex.check.recevier.ScreenShotFileObserver
import com.kintex.check.recevier.ScreenShotFileObserverManager
import com.kintex.check.utils.CaseId
import com.kintex.check.utils.ResultCode
import com.kintex.check.utils.ResultCode.BUTTON_POSITION
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.PASSED
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener
import com.kongzue.dialog.util.BaseDialog
import com.kongzue.dialog.v3.MessageDialog
import kotlinx.android.synthetic.main.activity_button.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ButtonActivity : BaseActivity(), View.OnClickListener {

    private var btnReset : TextView ?=null

    private var titleHome : TextView? = null
    private var titleBack : TextView ?= null
    private var titleDone : TextView ?= null
    private var keyReceiver : KeyEventReceiver?=null
    private var screenReceiver : ScreenReceiver?=null
    private var vibrateReceiver : VibrateReceiver ?=null
    private var isFirst = true


    private var isVibratePass = false
    private var isVibrateKeyTestFinish = false
    private var hasVibrateKey = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        EventBus.getDefault().register(this)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_button)
        //一加手机静音震动按键
        val brand = android.os.Build.BRAND
        XLog.d("brand$brand")
        val find = caseType!!.typeItems.find {
            it.caseId == CaseId.SilenceButton.id
        }
        if(find != null && brand == "OnePlus"){
            hasVibrateKey = true
            view_one.visibility = View.VISIBLE
            XLog.d("hasVibKey")
        }

        btnReset = findViewById(R.id.tv_btnReset)
        btnReset!!.setOnClickListener(this)
        titleHome = findViewById(R.id.tv_titleName)
        titleHome!!.text = "Button Test"
        titleBack = findViewById(R.id.tv_titleReset)
        titleBack!!.setOnClickListener(this)
        titleBack!!.text = "Back"
        titleDone = findViewById(R.id.tv_titleDone)
        titleDone!!.setOnClickListener(this)

        tv_silentFail.setOnClickListener(this)
        tv_vibrateFail.setOnClickListener(this)
        tv_ringFail.setOnClickListener(this)

        keyReceiver = KeyEventReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        registerReceiver(keyReceiver,intentFilter)

        vibrateReceiver = VibrateReceiver()
        val intentFilter1 = IntentFilter("android.media.RINGER_MODE_CHANGED")
        registerReceiver(vibrateReceiver,intentFilter1)

        btn_power.setOnClickListener(this)
        btn_volunDown.setOnClickListener(this)
        btn_volunUp.setOnClickListener(this)

        screenReceiver = ScreenReceiver()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenReceiver,filter)
    }

    override fun onClick(v: View?) {

        when(v!!.id){

            R.id.tv_titleReset->{
                finish()
            }

            R.id.tv_btnReset->{
                reSetView()
            }

            R.id.tv_titleDone->{
               // finishTest()
                finish()
            }

            R.id.btn_power->{
                isPowerTest = true
                tv_power.setTextColor(resources.getColor(R.color.red))
                tv_power.text = "FAIL"
                sendCaseResult(CaseId.PowerButton.id, FAILED,ResultCode.MANUAL)
                hasFinishTest()
            }

            R.id.btn_volunDown->{
                isVolumeDownTest = true
                tv_volunDown.setTextColor(resources.getColor(R.color.red))
                tv_volunDown.text = "FAIL"
                sendCaseResult(CaseId.VolumeUpButton.id, FAILED, ResultCode.MANUAL)
                hasFinishTest()
            }

            R.id.btn_volunUp->{
                isVolumeUpTest = true
                tv_volunUp.setTextColor(resources.getColor(R.color.red))
                tv_volunUp.text = "FAIL"
                sendCaseResult(CaseId.VolumeDownButton.id, FAILED, ResultCode.MANUAL)
                hasFinishTest()
            }

            R.id.tv_silentFail->{
                goFailCase()

            }

            R.id.tv_vibrateFail->{
                goFailCase()
            }

            R.id.tv_ringFail->{
                goFailCase()
            }
        }

    }

    private fun goFailCase() {
        tv_silentResult.text = resources.getString(R.string.Failed)
        tv_silentResult.setTextColor(resources.getColor(R.color.red))
        tv_vibrateResult.text = resources.getString(R.string.Failed)
        tv_vibrateResult.setTextColor(resources.getColor(R.color.red))
        tv_ringResult.text = resources.getString(R.string.Failed)
        tv_ringResult.setTextColor(resources.getColor(R.color.red))
        isVibratePass = false
        isVibrateKeyTestFinish = true
        checkVibrateKeyPass()
    }

    //手动拨动按键有没有完成
    private fun isManualFinish(){
        if(isVibrateFinish&& isSilentFinish && isRingFinish){
            isVibratePass = true
            checkVibrateKeyPass()
        }
    }

    //判断一加静音按钮是否正常
    private fun checkVibrateKeyPass(){


        if(isVibratePass){
            sendCaseResult(CaseId.SilenceButton.id, PASSED,ResultCode.MANUAL)
        }else{
            sendCaseResult(CaseId.SilenceButton.id, PASSED,ResultCode.MANUAL)
        }
        isVibrateKeyTestFinish = true

        hasFinishTest()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        XLog.d("keyCode:$keyCode")

        when(keyCode){

            //后退
           /* KeyEvent.KEYCODE_BACK->{
                isBackPass = true
                tv_btnBack.setTextColor(resources.getColor(R.color.green))
                hasFinishTest()
                return true
            }*/
            //音量增加
            KeyEvent.KEYCODE_VOLUME_UP->{
                isVolumeUpTest = true
                tv_volunUp.setTextColor(resources.getColor(R.color.green))
                sendCaseResult(CaseId.VolumeUpButton.id, PASSED, ResultCode.MANUAL)
                tv_volunUp.text = "PASS"
                hasFinishTest()
            }
            //音量减小
            KeyEvent.KEYCODE_VOLUME_DOWN->{
                isVolumeDownTest = true
                tv_volunDown.setTextColor(resources.getColor(R.color.green))
                tv_volunDown.text = "PASS"
                sendCaseResult(CaseId.VolumeDownButton.id, PASSED, ResultCode.MANUAL)
                hasFinishTest()
            }

        }

        return super.onKeyDown(keyCode, event)


    }
    private final val KEY_MENU =  1
    private final val KEY_HOME =  2
    private final val SCREEN_ON =  3
    private final val SCREEN_OFF = 4
    private final val KEY_POWER =  5
    private var isScreenOff = false
    private var isScreenOn = false
    private var vibrator: Vibrator? = null

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onKeyEvent(result: KeyEventBean){
        XLog.d("onKeyEvent：${result.keyName}，${result.keyEvent}")
        when (result.keyEvent) {
            KEY_MENU -> {
                //菜单
           //     isMenuPass = true
             //   tv_btnMenu.setTextColor(resources.getColor(R.color.green))
              //  sendCaseResult(CaseId.HomeButton.id, PASSED)
             //   hasFinishTest()
            }
         /*   KEY_HOME -> {
                //HOME
                isHomePass = true
                tv_btnHome.setTextColor(resources.getColor(R.color.green))
                sendCaseResult(CaseId.HomeButton.id, PASSED)
                hasFinishTest()
            }*/

            SCREEN_ON -> {
                isScreenOn = true

                if(isScreenOn && isScreenOff ){
                    if(!isPowerTest){
                        tv_power.setTextColor(resources.getColor(R.color.green))
                        tv_power.text = "PASS"
                        isPowerTest = true
                        sendCaseResult(CaseId.PowerButton.id, PASSED, ResultCode.MANUAL)
                        hasFinishTest()
                    }
                }
            }
            SCREEN_OFF -> {
                isScreenOff = true
            }
            KEY_POWER -> {
                if(!isPowerTest){
                    tv_power.setTextColor(resources.getColor(R.color.green))
                    tv_power.text = "PASS"
                    isPowerTest = true
                    sendCaseResult(CaseId.PowerButton.id, PASSED, ResultCode.MANUAL)
                    hasFinishTest()
                }
            }
        }

    }

    private var isVibrateFinish =false
    private var isSilentFinish =false
    private var isRingFinish =false

    //监听情景模式
  inner class VibrateReceiver : BroadcastReceiver(){
      override fun onReceive(context: Context?, intent: Intent) {

          if (intent.action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
              if(isFirst){
                  isFirst = false
                  return
              }
              var am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
              var ringerMode = am.ringerMode
              when (ringerMode) {
                   AudioManager.RINGER_MODE_NORMAL->{
                       //normal
                       XLog.d("normal")
                       runOnUiThread {
                           tv_ringResult.text = resources.getString(R.string.Passed)
                           tv_ringResult.setTextColor(resources.getColor(R.color.green))
                           tv_ringFail.visibility = View.GONE
                           isRingFinish = true
                           isManualFinish()
                       }
                   }

                  AudioManager.RINGER_MODE_VIBRATE->{
                      //vibrate
                      XLog.d("vibrate")
                      runOnUiThread {
                          tv_vibrateResult.text = resources.getString(R.string.Passed)
                          tv_vibrateResult.setTextColor(resources.getColor(R.color.green))
                          tv_vibrateFail.visibility = View.GONE
                          isVibrateFinish = true
                          isManualFinish()
                      }
                  }


                  AudioManager.RINGER_MODE_SILENT->{
                      //silent
                      XLog.d("silent")
                      runOnUiThread {
                          tv_silentResult.text = resources.getString(R.string.Passed)
                          tv_silentResult.setTextColor(resources.getColor(R.color.green))
                          tv_silentFail.visibility = View.GONE
                          isSilentFinish = true
                          isManualFinish()
                      }
                  }

              }
          }


      }

  }


    //检测震动功能
    private fun checkVibration() {
        vibrator = getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
        try {
          //  val patter = longArrayOf(200, 600, 200, 600,200, 600,200, 600,200, 600)
            val pattern = longArrayOf(500, 1000, 500, 1000)
            vibrator!!.vibrate(pattern,3)
            showVibratorDialog()
        } catch (e: Exception) {
            XLog.d(e)
        }
    }



    private fun showVibratorDialog(){
        val dialog = MessageDialog.build(this)
                .setTitle("震动提醒")
                .setMessage("请确认手机是否有震动")

        dialog.setOkButton("是", object : OnDialogButtonClickListener {
            override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                dialog!!.doDismiss()
                vibrator?.cancel()
                sendCaseResult(CaseId.Vibration.id, PASSED, ResultCode.MANUAL)
                runOnUiThread {
                    finish()
                }
                return false
            }
        })
        dialog.cancelable = false
        dialog.setCancelButton("否", object : OnDialogButtonClickListener {
            override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                dialog!!.doDismiss()
                vibrator?.cancel()
                sendCaseResult(CaseId.Vibration.id, FAILED, ResultCode.MANUAL)
                runOnUiThread {
                    finish()
                }
                return false

            }
        })
        dialog.show()

    }

    override fun onDestroy() {
        super.onDestroy()
        testNext()
        unregisterReceiver(keyReceiver)
        unregisterReceiver(screenReceiver)
        unregisterReceiver(vibrateReceiver)
        EventBus.getDefault().unregister(this)
    }

    private var isPowerTest = false
    private var isVolumeUpTest = false
    private var isVolumeDownTest = false
    private var canTestVib = false

    private fun hasFinishTest(){

        if(hasVibrateKey){

            if(isVibrateKeyTestFinish && canTestVib && isPowerTest  && isVolumeUpTest && isVolumeDownTest ){
                checkVibration()
            }
        }else{

            if(canTestVib && isPowerTest  && isVolumeUpTest && isVolumeDownTest ){
                checkVibration()
            }
        }

    }
    private var resultCaseList = arrayListOf<TestCase>(
        TestCase(30,"Power Button","",1,0),
        TestCase(31,"Home Button","",1,0),
        TestCase(32,"Volume Down Button","",1,0),
        TestCase(33,"Volume Up Button","",1,0),
        TestCase(35,"Back Button","",1,0),
        TestCase(36,"Menu Button","",1,0),
        TestCase(34,"Flip Switch","",0,0)
    )

    private fun finishTest(){
        if(isPowerTest &&  isVolumeUpTest && isVolumeDownTest ){
            for (testCase in resultCaseList) {
                testCase.result = 1
            }
            sendResult(BUTTON_POSITION, PASSED,resultCaseList)
        }
    }

    override fun onResume() {
        super.onResume()
        canTestVib = true
            tv_btnPower.postDelayed(
                    Runnable {
                        hasFinishTest()
                    },500
            )
    }

    override fun onPause() {
        super.onPause()
        canTestVib = false
    }

    override fun onStart() {
        super.onStart()
        ScreenShotFileObserverManager.registerScreenShotFileObserver(object :
                ScreenShotFileObserver.ScreenShotLister {
            override fun finshScreenShot(path: String?) {

                XLog.d("finshScreenShot path = $path")
                runOnUiThread {
                    isVolumeDownTest = true
                    tv_volunDown.setTextColor(resources.getColor(R.color.green))
                    tv_volunDown.text = "PASS"
                    sendCaseResult(CaseId.VolumeDownButton.id, PASSED, ResultCode.MANUAL)
                    hasFinishTest()

                    tv_power.setTextColor(resources.getColor(R.color.green))
                    tv_power.text = "PASS"
                    isPowerTest = true
                    sendCaseResult(CaseId.PowerButton.id, PASSED, ResultCode.MANUAL)
                    hasFinishTest()
                }

            }

            override fun beganScreenShot(path: String?) {
                XLog.d("beganScreenShot path = $path")
            }
        })
    }
    override fun onStop() {
        super.onStop()
        ScreenShotFileObserverManager.unregisteScreenShotFileObserver()
    }


    private fun reSetView(){

        isPowerTest = false
        isVolumeUpTest = false
        isVolumeDownTest = false
        tv_power.setTextColor(resources.getColor(R.color.gray))
        tv_power.text = "unKnown"
        tv_volunUp.setTextColor(resources.getColor(R.color.gray))
        tv_volunUp.text = "unKnown"
        tv_volunDown.setTextColor(resources.getColor(R.color.gray))
        tv_volunDown.text = "unKnown"
    }

    companion object{
        var caseType : CaseType ?=null
        fun start(context: Context, caseType: CaseType?){
            this.caseType = caseType
            context.startActivity(Intent(context,ButtonActivity::class.java))
        }
    }

}