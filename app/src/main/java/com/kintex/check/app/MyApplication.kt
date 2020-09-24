package com.kintex.check.app

import android.app.Application
import android.os.Environment
import com.blankj.utilcode.util.Utils
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.interceptor.BlacklistTagsFilterInterceptor
import com.elvishew.xlog.printer.AndroidPrinter
import com.elvishew.xlog.printer.file.FilePrinter
import com.kintex.check.BuildConfig
import com.kongzue.dialog.v2.DialogSettings
import com.kongzue.dialog.v2.DialogSettings.STYLE_IOS
import com.kongzue.dialog.v2.DialogSettings.THEME_LIGHT

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initLog()
        Utils.init(this)
        DialogSettings.style = STYLE_IOS
        DialogSettings.tip_theme = THEME_LIGHT
    }

    private fun initLog() {

        val config = LogConfiguration.Builder()
            .logLevel(if (BuildConfig.DEBUG) LogLevel.ALL  else LogLevel.NONE)
            .tag("MY_TAG")                                         // 指定 TAG，默认为 "Message-LOG"
            .t()                                                   // 允许打印线程信息，默认禁止
            .st(5)                                                 // 允许打印深度为5的调用栈信息，默认禁止
            .b()

            // 允许打印日志边框，默认禁止
            .addInterceptor(
                BlacklistTagsFilterInterceptor(    // 添加黑名单 TAG 过滤器
                    "blacklist1", "blacklist2", "blacklist3")
            )
            .build()

        val filePrinter = FilePrinter
            .Builder(Environment.getExternalStorageDirectory().path + "/log/$packageName")
            // 指定保存日志文件的路径
            .build()
        XLog.init(config, filePrinter, AndroidPrinter())

    }

}