package com.kintex.check.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.TestResultBean
import com.kintex.check.utils.ResultCode.DIGITIZER_POSITION
import com.kintex.check.utils.ResultCode.PASSED
import kotlinx.android.synthetic.main.activity_digitizer.*
import org.greenrobot.eventbus.EventBus

class DigitizerActivity  : BaseActivity(), View.OnTouchListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
        setContentView(R.layout.activity_digitizer)
        setView()
    }

    private var viewList = ArrayList<TextView>()

    private fun setView() {

        for( i in 0..15){
            for(j in 0..8){
                val textView = layoutInflater.inflate(R.layout.item_textview, null) as TextView
           //     var textView =  TextView(this)
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

        if(touchCount == 144){
            EventBus.getDefault().post(TestResultBean(DIGITIZER_POSITION, PASSED))
            finish()
        }

    }

    companion object{
        fun start(context: Context){
            XLog.d("start")
            context.startActivity(Intent(context,DigitizerActivity::class.java))
        }
    }


}