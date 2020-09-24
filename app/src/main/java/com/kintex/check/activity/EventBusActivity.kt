package com.kintex.check.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

//适合使用EventBus 的Activity 继承使用
open class EventBusActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this)
        }
        EventBus.getDefault().register(this)


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun testEvent(code :Int){

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

    }

}