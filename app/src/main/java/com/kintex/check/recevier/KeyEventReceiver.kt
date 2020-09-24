package com.kintex.check.recevier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elvishew.xlog.XLog
import com.kintex.check.bean.KeyEventBean
import org.greenrobot.eventbus.EventBus

class KeyEventReceiver : BroadcastReceiver() {
    private final val KEY_MENU =  1
    private final val KEY_HOME =  2
    private final val KEY_POWER =  5
    override fun onReceive(context: Context?, intent: Intent?) {

        val action = intent!!.action
        if(Intent.ACTION_CLOSE_SYSTEM_DIALOGS == action){

            val reason = intent.getStringExtra("reason")
            if(reason != null){
                XLog.d("reason::$reason")
                if(reason == "homekey"){

                    EventBus.getDefault().post(KeyEventBean("home",KEY_HOME))
                }else if(reason == "recentapps"){
                    EventBus.getDefault().post(KeyEventBean("menu",KEY_MENU))

                }else if(reason == "dream"){
                    //dream  电源
                    EventBus.getDefault().post(KeyEventBean("dream",KEY_POWER))
                }

            }
        }

    }
}