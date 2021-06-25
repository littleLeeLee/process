package com.kintex.check.utils

import android.content.Context
import android.content.Intent
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object MyUtils {

    open fun startActivity(context: Context, className: String){
       var clazz = Class.forName("MainActivity",false, ClassLoader.getSystemClassLoader())
        context.startActivity(Intent(context,clazz))

    }
    fun <T : Any> getClassName(clzObj: T): String {
        return clzObj.javaClass.simpleName
    }

    fun getStrFromAssets(context: Context, name: String?): String? {
        val assetManager = context.assets
        try {
            val `is` = assetManager.open(name!!)
            val br = BufferedReader(InputStreamReader(`is`))
            val sb = StringBuilder()
            var str: String?
            while (br.readLine().also { str = it } != null) {
                sb.append(str)
            }
            return sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


    /**
     * 根据字符串获取资源ID
     *
     * @param variableName
     * @param c
     * @return
     */
    fun getResId(variableName : String ,  c:Class<Any>) :Int{
        try {
            var idField = c.getDeclaredField(variableName)
            return idField.getInt(idField)
        } catch ( e :Exception) {
            e.printStackTrace()
            return -1
        }
    }



}