package com.kintex.check.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import com.elvishew.xlog.XLog
import com.kintex.check.R
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

        tv_titleName.text = "TouchCount Test"

        tv_titleDone.setOnClickListener {

            sendResult(TOUCH_POSITION, FAILED)

        }


        btn_failed.setOnClickListener {

            sendResult(TOUCH_POSITION, FAILED)

        }

        btn_passed.setOnClickListener {

            sendResult(TOUCH_POSITION, PASSED)

        }

    }

    var pointerCount = 0;
    override fun onTouchEvent(event: MotionEvent): Boolean {

        pointerCount  = event!!.pointerCount
      //  XLog.d("pointerCount:$pointerCount action:${event.action}")

        if(event.action == MotionEvent.ACTION_UP){
            pointerCount  = 0
        }

        runOnUiThread {

            tv_fingerCount.text = "数量:$pointerCount"

        }

        return true


    }


    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context,TouchCountActivity::class.java))
        }
    }

}