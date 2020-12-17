package com.kintex.check.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.audioview.AudioActivity
import com.kintex.check.utils.MyUtils
import kotlinx.android.synthetic.main.activity_welcome.*
import kotlinx.android.synthetic.main.newmain.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class WelcomeActivity : AppCompatActivity() {

    var k = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
     //   setContentView(R.layout.newmain)
        setContentView(R.layout.activity_welcome)
    //  var version =  findViewById<TextView>(R.id.tv_welVersionNum)
   //    version.text = AppUtils.getAppVersionName()


/*        Thread{
            kotlin.run {
                while (k<120){
                    k++
                    updateProcess()
                    Thread.sleep(1000)
                }
            }
        }.start()*/

        tv_welVersionNum.postDelayed(Runnable {

     // AudioActivity.start(this)
       MainActivity.start(this)
            finish()
        },500)



    }

    fun updateProcess(){
        myProcessView.updateProcess()
    }

}