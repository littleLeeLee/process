package com.kintex.check.recevier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kintex.check.bean.KeyEventBean
import org.greenrobot.eventbus.EventBus

class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val action = intent!!.action
        if(action == Intent.ACTION_SCREEN_ON){

            EventBus.getDefault().post(KeyEventBean("screen",3))

        }else if(action == Intent.ACTION_SCREEN_OFF){
            EventBus.getDefault().post(KeyEventBean("screen",4))
        }

    }
}