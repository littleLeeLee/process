package com.kintex.check.activity

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Vibrator
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.KeyEventBean
import com.kintex.check.bean.TestCase
import com.kintex.check.bean.TestResultBean
import com.kintex.check.recevier.KeyEventReceiver
import com.kintex.check.recevier.ScreenReceiver
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        EventBus.getDefault().register(this)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_button)
        btnReset = findViewById(R.id.tv_btnReset)
        btnReset!!.setOnClickListener(this)
        titleHome = findViewById(R.id.tv_titleName)
        titleHome!!.text = "Button Test"
        titleBack = findViewById(R.id.tv_titleReset)
        titleBack!!.setOnClickListener(this)
        titleBack!!.text = "Back"
        titleDone = findViewById(R.id.tv_titleDone)
        titleDone!!.setOnClickListener(this)

        keyReceiver = KeyEventReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        registerReceiver(keyReceiver,intentFilter)
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
        }

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
    private var isPowerDown = false
    private var isScreenOff = false
    private var isScreenOn = false
    private var vibrator: Vibrator? = null

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onKeyEvent(result: KeyEventBean){
        XLog.d("onKeyEvent")
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

                if(isScreenOn && isScreenOff && isPowerDown){
                    tv_power.setTextColor(resources.getColor(R.color.green))
                    tv_power.text = "PASS"
                    isPowerTest = true
                    sendCaseResult(CaseId.PowerButton.id, PASSED, ResultCode.MANUAL)
                    hasFinishTest()
                }
            }
            SCREEN_OFF -> {
                isScreenOff = true
            }
            KEY_POWER -> {
                isPowerDown = true
            }
        }

    }

    private fun checkVibration() {
        vibrator = getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
        try {
          //  val patter = longArrayOf(200, 600, 200, 600,200, 600,200, 600,200, 600)
            vibrator!!.vibrate(1000 * 60)
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
        EventBus.getDefault().unregister(this)
    }

    private var isPowerTest = false
    private var isVolumeUpTest = false
    private var isVolumeDownTest = false

    private fun hasFinishTest(){

            if(isPowerTest  && isVolumeUpTest && isVolumeDownTest ){
                /*for (testCase in resultCaseList) {
                    testCase.result = 1
                }*/
                checkVibration()
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
        fun start(context: Context){
            context.startActivity(Intent(context,ButtonActivity::class.java))
        }
    }

}