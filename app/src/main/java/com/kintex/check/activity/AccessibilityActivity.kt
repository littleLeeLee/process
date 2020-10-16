package com.kintex.check.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog

class AccessibilityActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivityForResult(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),1024)
    }


    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context,AccessibilityActivity::class.java))
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1024){
            if(resultCode == Activity.RESULT_OK){
                ToastUtils.showShort("开启成功")
            }else{
                ToastUtils.showShort("开启失败")
            }
        }

    }
}