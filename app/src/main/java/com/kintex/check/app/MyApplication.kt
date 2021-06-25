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
import com.kongzue.dialog.util.DialogSettings
import com.tencent.bugly.crashreport.CrashReport

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CrashReport.initCrashReport(applicationContext, "b71e843921", true)
        initLog()
        Utils.init(this)
        DialogSettings.isUseBlur = (true)                   //是否开启模糊效果，默认关闭
        DialogSettings.modalDialog = (true)                //是否开启模态窗口模式，一次显示多个对话框将以队列形式一个一个显示，默认关闭
        DialogSettings.style =  DialogSettings.STYLE.STYLE_IOS    //全局主题风格，提供三种可选风格，STYLE_MATERIAL, STYLE_KONGZUE, STYLE_IOS
        DialogSettings.theme = (DialogSettings.THEME.LIGHT)          //全局对话框明暗风格，提供两种可选主题，LIGHT, DARK
        DialogSettings.tipTheme = (DialogSettings.THEME.LIGHT)
    }

    private fun initLog() {

        val config = LogConfiguration.Builder()
            .logLevel(if (BuildConfig.DEBUG) LogLevel.ALL  else LogLevel.NONE)
            .tag("MY_TAG")                                         // 指定 TAG，默认为 "Message-LOG"
            .t()                                                   // 允许打印线程信息，默认禁止
            .st(2)                                                 // 允许打印深度为5的调用栈信息，默认禁止
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