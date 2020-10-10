package com.kintex.check.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.elvishew.xlog.XLog

class MyTouchView : View {

    private var paint : Paint ? = null
    constructor(context: Context?) : super(context){
        initData()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    {
        initData()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    {
        initData()
    }


    private fun initData() {

        paint = Paint()

        //抗锯齿
        paint!!.isAntiAlias = true
        //防抖动
        paint!!.isDither = true

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val pointerCount = event.pointerCount
        XLog.d("pointerCount$pointerCount")
        when(event.action == MotionEvent.ACTION_DOWN){

        }

        return true
    }


    override fun onDraw(canvas: Canvas) {

    }
}