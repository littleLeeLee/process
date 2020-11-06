package com.kintex.check.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.TestCase
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

    private var resultCaseList  = arrayListOf<TestCase>(
        TestCase("LCD",37,"LCD","",1,0),
        TestCase("Digitizer",38,"Touch Screen","",1,0),
        TestCase("Multi Touch",49,"Multi Touch","",1,0)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
        setContentView(R.layout.activity_lcd)
        tv_colorView.setOnClickListener(this)
        tv_lcdPass.setOnClickListener(this)
        tv_lcdFail.setOnClickListener(this)

    }


    private var clickCount = 0

    override fun onClick(v: View?) {

        when(v!!.id){

            R.id.tv_lcdPass->{
                resultCaseList[0].result = 1
            //    sendResult(LCD_POSITION, PASSED,resultCaseList)
                showDigitizer()
            }

            R.id.tv_lcdFail->{
                resultCaseList[0].result = 0
             //   sendResult(LCD_POSITION, FAILED,resultCaseList)
                showDigitizer()
            }

            R.id.tv_colorView->{
                clickCount++
                setColor()
            }

        }


    }
    private var viewList = ArrayList<TextView>()
    private fun showDigitizer() {
        view_lcd.visibility = View.GONE
        view_digitizer.visibility = View.VISIBLE

        tv_digitizerFailed.setOnClickListener {
            resultCaseList[1].result = 0
           // sendResult(ResultCode.DIGITIZER_POSITION, FAILED,resultCaseList)
            showTouchCount()
        }

        tv_failed1.setOnClickListener {
            resultCaseList[1].result = 0
          //  sendResult(ResultCode.DIGITIZER_POSITION, FAILED,resultCaseList)
            showTouchCount()
        }

        tv_failed2.setOnClickListener {
            resultCaseList[1].result = 0
          //  sendResult(ResultCode.DIGITIZER_POSITION, FAILED,resultCaseList)
            showTouchCount()
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

    private fun showTouchCount() {

        view_digitizer.visibility = View.GONE
        view_touch.visibility = View.VISIBLE

        tv_titleName.text = "LCD TEST"

        tv_titleDone.setOnClickListener {
            resultCaseList[2].result = 0
          //  sendResult(ResultCode.LCD_POSITION, FAILED,resultCaseList)
            checkResult()
        }


        btn_failed.setOnClickListener {
            resultCaseList[2].result = 0
          //  sendResult(ResultCode.LCD_POSITION, FAILED,resultCaseList)
            checkResult()

        }

        btn_passed.setOnClickListener {
            resultCaseList[2].result = 1
            checkResult()
          //  sendResult(ResultCode.LCD_POSITION, ResultCode.PASSED,resultCaseList)
        }


    }

    private fun setColor() {

        when(clickCount){

            //
            1->{
                tv_colorView.setBackgroundColor(Color.BLACK)
            }
            2->{
                tv_colorView.setBackgroundColor(Color.RED)
            }
            3->{
                tv_colorView.setBackgroundColor(Color.GREEN)
            }
            4->{
                tv_colorView.setBackgroundColor(Color.BLUE)
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

    private fun checkCount() {

        if(touchCount == 91){
            resultCaseList[1].result = 1
          //  sendResult(ResultCode.DIGITIZER_POSITION, ResultCode.PASSED,resultCaseList)
            showTouchCount()
        }

    }


    var pointerCount = 0
    var hasSend = false
    override fun onTouchEvent(event: MotionEvent): Boolean {

        pointerCount  = event!!.pointerCount
        //  XLog.d("pointerCount:$pointerCount action:${event.action}")

        if(event.action == MotionEvent.ACTION_UP){
            pointerCount  = 0
        }

        runOnUiThread {

            tv_fingerCount.text = "数量:$pointerCount"
            if(pointerCount>=2&&!hasSend){
                hasSend = true
                resultCaseList[2].result = 1
                checkResult()

            }
        }

        return true


    }

    var passed = 0
    private fun checkResult() {
        for(case in resultCaseList){
            if(case.result == 1){
                passed ++
            }

        }
        XLog.d("passed:$passed")
        if(passed == resultCaseList.size){
            sendResult(ResultCode.LCD_POSITION, PASSED,resultCaseList)
        }else{
            sendResult(ResultCode.LCD_POSITION, FAILED,resultCaseList)
        }


    }

    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context,LCDActivity::class.java))
        }
    }


}