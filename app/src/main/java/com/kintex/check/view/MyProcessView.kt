package com.kintex.check.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.elvishew.xlog.XLog
import com.kintex.check.R
import kotlin.math.*

class MyProcessView : View {

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
    private var whitePaint : Paint ?=null
    private var whiteRectPaint : Paint ?=null
    private var bluePaint : Paint ?=null

    private fun initData() {

        whitePaint = Paint()
        whitePaint!!.style = Paint.Style.STROKE
        whitePaint!!.strokeWidth = strokeWidth
        whitePaint!!.color = Color.WHITE
        whitePaint!!.isAntiAlias = true
        whitePaint!!.isDither = true

        bluePaint = Paint()
        bluePaint!!.style = Paint.Style.STROKE
        bluePaint!!.strokeWidth = strokeWidth
        //#8CACF8
        bluePaint!!.color = resources.getColor(R.color.processColor)
        bluePaint!!.isAntiAlias = true
        bluePaint!!.isDither = true


        whiteRectPaint = Paint()
        whiteRectPaint!!.style = Paint.Style.FILL
        whiteRectPaint!!.strokeWidth = 8f
        whiteRectPaint!!.color = Color.WHITE
        whiteRectPaint!!.isAntiAlias = true
        whiteRectPaint!!.isDither = true
    }

    private var strokeWidth = 24f


    private var startX = strokeWidth/2
    private var startY = strokeWidth/2
    private var trueWidth = width - startX
    private var trueHeight = height -startY

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        trueWidth = width - startX
        trueHeight = height -startY

        val oval = RectF(
            startX, startY,
            trueWidth, trueHeight
        )
        //整体进度条
        canvas.drawArc(oval,140f,260f,false,whitePaint!!)
        //更新进度
        canvas.drawArc(oval,140f,currentProcess,false,bluePaint!!)
       // val sqrt = sqrt((width / 2.toDouble()).pow(2.toDouble()) - top.toDouble().pow(2))

        val cos = cos(50 * PI / 180)
        val sin = sin(50 * PI /180)

        var top = trueHeight/2 + (trueHeight/2 * cos).toFloat() -12 * cos(45 *PI /180).toFloat()
        var left = trueWidth/2 - (trueWidth/2 *sin).toFloat()
        var right =  12 * cos(45 *PI /180).toFloat()
        var bottom = 12 * cos(45 *PI /180).toFloat()
        val oval1 = RectF(
            left,top ,right , bottom
        )
  //      XLog.d("width:$trueWidth height:$trueHeight left:$left top:$top right:$right bottom:$bottom ")
    //    canvas.drawArc(oval1,0f,360f,false,whiteRectPaint!!)


        //画刻度
        for (i in 0..8) {

            canvas.drawLine(width /2f, strokeWidth+15f, width /2f, strokeWidth+15f + 10f, whiteRectPaint!!)
            //跳过4格
            if(i == 4){
                //旋转的角度，旋转的圆心
                canvas.rotate(120f,width/2.toFloat(),height/2.toFloat())
            }else{
                //旋转的角度，旋转的圆心
                canvas.rotate((360 / 12).toFloat(),width/2.toFloat(),height/2.toFloat())
            }

        }


    }

    private var currentProcess : Float = 0f
    fun updateProcess(){
        currentProcess += (260f / 120f)

        invalidate()
    }

    private var MAXPROCESS = 0
    fun setMaxProcess(max : Int){
        MAXPROCESS = max
    }




}