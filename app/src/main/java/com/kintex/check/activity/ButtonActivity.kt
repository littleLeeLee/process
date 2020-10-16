package com.kintex.check.activity

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
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
import com.kintex.check.utils.ResultCode.BUTTON_POSITION
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.PASSED
import kotlinx.android.synthetic.main.activity_button.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ButtonActivity : BaseActivity(), View.OnClickListener {

    private var btnHome : TextView ?=null
    private var btnPower : TextView ?=null
    private var btnMenu : TextView ?=null
    private var btnVloumeUp : TextView ?=null
    private var btnVoluneDown : TextView ?=null
    private var btnBack : TextView ?=null
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
        btnHome = findViewById(R.id.tv_btnHome)
        btnPower = findViewById(R.id.tv_btnPower)
        btnMenu = findViewById(R.id.tv_btnMenu)
        btnVloumeUp = findViewById(R.id.tv_btnVolumeUp)
        btnVoluneDown = findViewById(R.id.tv_btnVolumeDown)
        btnBack = findViewById(R.id.tv_btnBack)
        btnReset = findViewById(R.id.tv_btnReset)
        btnReset!!.setOnClickListener(this)
        titleHome = findViewById(R.id.tv_titleName)
        titleHome!!.text = "Button Test"
        titleBack = findViewById(R.id.tv_titleReset)
        titleBack!!.setOnClickListener(this)
        titleBack!!.text = "back"
        titleDone = findViewById(R.id.tv_titleDone)
        titleDone!!.setOnClickListener(this)

        keyReceiver = KeyEventReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        registerReceiver(keyReceiver,intentFilter)

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

                finishTest()

            }


        }


    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        XLog.d("keyCode:$keyCode")

        when(keyCode){

            //后退
            KeyEvent.KEYCODE_BACK->{
                isBackPass = true
                tv_btnBack.setTextColor(resources.getColor(R.color.green))
                hasFinishTest()
                return true
            }
            //音量增加
            KeyEvent.KEYCODE_VOLUME_UP->{
                isVolumeUpPass = true
                tv_btnVolumeUp.setTextColor(resources.getColor(R.color.green))
                hasFinishTest()
            }
            //音量减小
            KeyEvent.KEYCODE_VOLUME_DOWN->{
                isVolumeDownPass = true
                tv_btnVolumeDown.setTextColor(resources.getColor(R.color.green))
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onKeyEvent(result: KeyEventBean){
        XLog.d("onKeyEvent")
        when (result.keyEvent) {
            KEY_MENU -> {
                //菜单
                isMenuPass = true
                tv_btnMenu.setTextColor(resources.getColor(R.color.green))
                hasFinishTest()
            }
            KEY_HOME -> {
                //HOME
                isHomePass = true
                tv_btnHome.setTextColor(resources.getColor(R.color.green))
                hasFinishTest()
            }

            SCREEN_ON -> {
                isScreenOn = true

                if(isScreenOn && isScreenOff && isPowerDown){
                    tv_btnPower.setTextColor(resources.getColor(R.color.green))
                    isPowerPass = true
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


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(keyReceiver)
        unregisterReceiver(screenReceiver)
        EventBus.getDefault().unregister(this)
    }

    private var isHomePass = false
    private var isPowerPass = false
    private var isMenuPass = false
    private var isVolumeUpPass = false
    private var isVolumeDownPass = false
    private var isBackPass = false

    private fun hasFinishTest(){

            if(isHomePass && isPowerPass && isMenuPass && isVolumeUpPass && isVolumeDownPass && isBackPass){
                for (testCase in resultCaseList) {
                    testCase.result = 1
                }
                btnHome!!.postDelayed(Runnable {
                    sendResult(BUTTON_POSITION, PASSED,resultCaseList)
                },200)

            }

    }
    private var resultCaseList = arrayListOf<TestCase>(
        TestCase("Buttons",30,"Power Button","",1,0),
        TestCase("Buttons",31,"Home Button","",1,0),
        TestCase("Buttons",32,"Volume Down Button","",1,0),
        TestCase("Buttons",33,"Volume Up Button","",1,0),
        TestCase("Buttons",35,"Back Button","",1,0),
        TestCase("Buttons",36,"Menu Button","",1,0)
    )

    private fun finishTest(){
        if(isHomePass && isPowerPass && isMenuPass && isVolumeUpPass && isVolumeDownPass && isBackPass){
            for (testCase in resultCaseList) {
                testCase.result = 1
            }
            sendResult(BUTTON_POSITION, PASSED,resultCaseList)
        }else{
            if(isPowerPass){
                resultCaseList[0].result = 1
            }
            if(isHomePass){
                resultCaseList[1].result = 1
            }
            if(isVolumeDownPass){
                resultCaseList[2].result = 1
            }

            if(isVolumeUpPass){
                resultCaseList[3].result = 1
            }
            if(isBackPass){
                resultCaseList[4].result = 1
            }
            if(isMenuPass){
                resultCaseList[5].result = 1
            }

            sendResult(BUTTON_POSITION, FAILED,resultCaseList)
        }
    }

    private fun reSetView(){

        isHomePass = false
        isPowerPass = false
        isMenuPass = false
        isVolumeUpPass = false
        isVolumeDownPass = false
        isBackPass = false
        tv_btnMenu.setTextColor(resources.getColor(R.color.textColor))
        tv_btnHome.setTextColor(resources.getColor(R.color.textColor))
        tv_btnPower.setTextColor(resources.getColor(R.color.textColor))
        tv_btnBack.setTextColor(resources.getColor(R.color.textColor))
        tv_btnVolumeUp.setTextColor(resources.getColor(R.color.textColor))
        tv_btnVolumeDown.setTextColor(resources.getColor(R.color.textColor))

    }

    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context,ButtonActivity::class.java))
        }
    }

}