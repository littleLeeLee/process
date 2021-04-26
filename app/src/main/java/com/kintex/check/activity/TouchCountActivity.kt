package com.kintex.check.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.TestCase
import com.kintex.check.utils.CaseId
import com.kintex.check.utils.ResultCode
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.PASSED
import com.kintex.check.utils.ResultCode.TOUCH_POSITION
import kotlinx.android.synthetic.main.activity_touch_count.*
import kotlinx.android.synthetic.main.title_include.*

class TouchCountActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_touch_count)
        setView()
    }

    private fun setView() {

        tv_titleReset.setOnClickListener {

            finish()

        }

        tv_titleName.text = "Multi Touch"

        tv_titleDone.setOnClickListener {
            sendTestResult(FAILED)
        }


        btn_failed.setOnClickListener {
            sendTestResult(FAILED)
        }

        btn_passed.setOnClickListener {

            sendTestResult(PASSED)
        }

    }

    private fun sendTestResult(result: Int) {
        sendCaseResult(result, CaseId.MultiTouch.id, ResultCode.MANUAL)
        LCDActivity.start(this)
        finish()
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
                sendTestResult(PASSED)
            }
        }

        return true


    }

    override fun onDestroy() {
        super.onDestroy()
            testNext()
    }

    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context,TouchCountActivity::class.java))
        }
    }

}