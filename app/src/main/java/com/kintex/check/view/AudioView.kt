package com.kintex.check.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.elvishew.xlog.XLog
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.random.Random

class AudioView : View {
    private var mPaint: Paint?=null
    private var linePaint: Paint?=null
    private var mPath: Path ?=null
    private var startPoint : Point ?= null
    private var endPoint : Point ?= null
    private var assistPoint : Point ?= null
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

        mPaint = Paint()
        linePaint = Paint()

        //抗锯齿
        mPaint!!.isAntiAlias = true
        linePaint!!.isAntiAlias = true
        //防抖动
        mPaint!!.isDither = true
        linePaint!!.isDither = true

        mPaint!!.color = Color.RED
        mPaint!!.strokeWidth = 10f
        mPaint!!.style = Paint.Style.FILL

        linePaint!!.color = Color.RED
        linePaint!!.strokeWidth =10f
        linePaint!!.style = Paint.Style.FILL
    }

    private var scWidth = 0
    private var scHeight = 0
    private var pointList = ArrayList<Point>()
    private var newPointList = ArrayList<Point>()
    private var rectList = ArrayList<Rect>()
    private var newRectList = ArrayList<Rect>()
    private var pointWidth: Int=0
    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        pointList.clear()
        rectList.clear()
        scWidth =measuredWidth
        scHeight = measuredHeight
        XLog.d("scheight:$scHeight")
        //计算出屏幕的点 40个

        pointWidth = scWidth / 81
        XLog.d("pointWidth$pointWidth")
        val pointHeight = scHeight
        var index = 0
        for ( k  in 0 until 81){
            index = pointWidth * (k+1)
            if(k%2 == 1){
               // 默认给10个高度
                rectList.add( Rect(index,pointHeight - 10,index + pointWidth,pointHeight))
            }
        }

        XLog.d("rect list${rectList.size}")
    }

    private var isFirst = true

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawLine(0f,scHeight.toFloat(),scWidth.toFloat(),scHeight.toFloat(),linePaint!!)

        if(isFirst){

            for (rect in rectList){
                canvas.drawRect(rect,mPaint!!)
            }
                isFirst = false

        }else{

            for (rect in newRectList){
                canvas!!.drawRect(rect,mPaint!!)
            }

        }
    }


    private var maxValue = 20
    private var defaultValue = 20

    //设定最大为100
    fun setMaxPoint(value : Int){
        var volume= value
        //过滤不正常的值
        if(value>= 100){
            volume = 100
        }
        if(value <=0){
            volume = 0
        }

        val maxHeight = 20 * 70

//        maxValue =  value % 10 *30
        if(volume<50){
            volume = 50
        }
        var abs = abs(volume - 50) * 20
        //根据最大声音的比值  显示对应屏幕高度的比值
        val i = abs / maxHeight.toDouble() * 0.7

        maxValue = (i * scHeight).toInt()


        //不能大于最大高度  的90
        if(maxValue > scHeight * 0.9){
            maxValue = ((scHeight * 0.9)-10).toInt()
        }


        if(newRectList.size == 0){
            for (rect in rectList){
                //给20个左右的振幅
                val nextInt = random.nextInt(20)
                newRectList.add(Rect(rect.left,rect.top - maxValue - nextInt,rect.right,rect.bottom))
            }
        }else{
            for (k in rectList.indices){
                val nextInt = random.nextInt(20)
                val rect = rectList[k]
                newRectList[k].top = rect.top-maxValue - nextInt
            }
        }

        invalidate()
    }


    private val random = Random(1)
    private fun startLoop(){

        Thread() {
            kotlin.run {
                while (true){
                    if(newPointList.size == 0){
                        for (point in pointList){
                            val y = point.y
                            val nextInt = random.nextInt( defaultValue)
                            newPointList.add(Point(point.x,y-(nextInt +maxValue)))
                        }
                    }else{
                        for (k in pointList.indices){
                            val y = pointList[k].y
                            val nextInt = random.nextInt( defaultValue)
                            newPointList[k].y = y-(nextInt +maxValue)
                        }
                    }

                    invalidate()
                    sleep(50)
                }
            }
        }.start()
    }


}