package com.kintex.check.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.util.AttributeSet
import android.view.View
import com.elvishew.xlog.XLog
import com.kintex.check.R
import java.lang.Thread.sleep
import kotlin.math.abs


class WaveView: View {

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
    var viewPaint : Paint ?= null
    var textPaint : Paint ?= null
    //半径
    var radius = 0f
    //圆心
    var drawX  =0f
    var drawY = 0f
    private fun initData() {
        isFirst = true
        viewPaint = Paint()
        viewPaint?.style = Paint.Style.STROKE
        viewPaint?.strokeWidth = 6f
        viewPaint?.isAntiAlias = true
        viewPaint?.color = resources.getColor(R.color.tvColorMain)


        textPaint = Paint()
        textPaint?.style = Paint.Style.FILL
        textPaint?.strokeWidth = 3f
        textPaint?.isAntiAlias = true
        textPaint?.textSize =textSize
        textPaint?.textAlign = Paint.Align.CENTER
        textPaint?.color = resources.getColor(R.color.tvColorMain)

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        radius = measuredWidth/2f
        drawX = radius
        drawY = measuredHeight/2f
        radius -= 5
    }
    var isFirst = true

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawCircle(drawX,drawY,radius,viewPaint!!)

        val fontMetrics: FontMetricsInt = textPaint!!.fontMetricsInt
        val newY = abs(fontMetrics.bottom + fontMetrics.top) / 2f
    //    XLog.d("newY:$newY")
        textPaint?.textSize =textSize
        canvas?.drawText("START",drawX,drawY + newY ,textPaint!!)

        if(isFirst){
            isFirst = false
            postDelayed(
                Runnable {
                    startMove()
                },200
            )
        }

    }

    var MIN_RADIUS = 0f
    var MAX_RADIUS = 0f
    var textSize = 60f
    fun startMove(){
        MIN_RADIUS = radius -20
        MAX_RADIUS = radius
        var expand = false
        XLog.d("startMove")
        Thread(){
            kotlin.run {

                while (true){

                    if(!expand){

                        if(radius >= MIN_RADIUS){
                            radius -=0.5f
                            textSize -= 0.25f
                            expand = false
                        }else{
                            expand = true
                        }


                    }else{
                       if(radius < MAX_RADIUS){
                           radius +=0.5f
                           textSize += 0.25f
                           expand = true
                       }else{
                           expand = false
                       }
                    }

                    invalidate()
                    sleep(30)

                }

            }
        }.start()


    }

}