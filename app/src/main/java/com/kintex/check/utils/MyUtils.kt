package com.kintex.check.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.kintex.check.activity.BaseActivity

object MyUtils {

    open fun startActivity(context: Context, className: String){
       var clazz = Class.forName("MainActivity",false, ClassLoader.getSystemClassLoader())
        context.startActivity(Intent(context,clazz))

    }
    fun <T : Any> getClassName(clzObj: T): String {
        return clzObj.javaClass.simpleName
    }

}