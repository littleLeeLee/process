package com.kintex.check.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.GridLayout

class MyGridView : GridLayout {

    constructor(context: Context?) : super(context){
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    {
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        val e = MotionEvent.obtain(ev)
        e.action = MotionEvent.ACTION_DOWN
        return super.dispatchTouchEvent(e)


    }


}