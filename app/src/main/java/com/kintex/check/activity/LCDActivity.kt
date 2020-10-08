package com.kintex.check.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.TestResultBean
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.LCD_POSITION
import com.kintex.check.utils.ResultCode.PASSED
import kotlinx.android.synthetic.main.activity_lcd.*
import org.greenrobot.eventbus.EventBus

class LCDActivity : BaseActivity(), View.OnClickListener {

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
                EventBus.getDefault().post(TestResultBean(LCD_POSITION, PASSED))
                finish()
            }

            R.id.tv_lcdFail->{
                EventBus.getDefault().post(TestResultBean(LCD_POSITION, FAILED))
                finish()
            }

            R.id.tv_colorView->{
                clickCount++
                setColor()
            }

        }


    }

    private fun setColor() {

        when(clickCount){

            //
            1->{
                tv_colorView.setBackgroundColor(Color.WHITE)
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


    companion object{
        fun start(context: Context){
            XLog.d("start")
            context.startActivity(Intent(context,LCDActivity::class.java))
        }
    }
}