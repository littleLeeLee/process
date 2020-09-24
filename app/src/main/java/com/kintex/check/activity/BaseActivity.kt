package com.kintex.check.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.kintex.check.R
import com.kintex.check.utils.MyUtils
import com.zyao89.view.zloading.ZLoadingDialog
import com.zyao89.view.zloading.Z_TYPE
import org.greenrobot.eventbus.EventBus
import java.lang.Exception

 open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initDialog()
    }



    var dialog: ZLoadingDialog? = null
    private fun initDialog() {
        dialog = ZLoadingDialog(this)
        dialog!!.setLoadingBuilder(Z_TYPE.SINGLE_CIRCLE)//设置类型
            .setLoadingColor(resources.getColor(R.color.red))//颜色
            .setHintText(resources.getString(R.string.loading))
            //   .setCancelable(false)
            .setHintTextSize(16f) // 设置字体大小 dp
            .setHintTextColor(Color.GRAY)  // 设置字体颜色
            .setDurationTime(0.5) // 设置动画时间百分比 - 0.5倍
            .setDialogBackgroundColor(Color.parseColor("#FFFFFF")) // 设置背景色，默认白色
        dialog!!.setCanceledOnTouchOutside(true)

    }

    open fun showDialog(title:String) {
        runOnUiThread {
            try {
                dialog?.setHintText(title)
                dialog?.show()
            } catch (e: Exception) {

            }
        }
    }

    fun dissmissDialog() {
        runOnUiThread {
            try {
                    dialog?.dismiss()
            } catch (E: Exception) {

            }

        }
    }

     override fun onDestroy() {
         super.onDestroy()
         EventBus.getDefault().unregister(this)

     }


}