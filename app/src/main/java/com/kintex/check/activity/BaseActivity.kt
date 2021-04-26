package com.kintex.check.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.kintex.check.bean.CaseResultBean
import com.kintex.check.bean.CheckBean
import com.kintex.check.bean.TestCase
import com.kintex.check.bean.TestResultBean
import com.kintex.check.utils.ResultCode.currentActivity
import org.greenrobot.eventbus.EventBus


open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lp = window.attributes
        val decorView: View = window.getDecorView()
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        hideBottomUIMenu()
        currentActivity = this
     //   initDialog()
    }



/*    var dialog: ZLoadingDialog? = null
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

    }*/

/*    open fun showDialog(title:String) {
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
    }*/


     fun sendResult(position:Int,result :Int,caseList: ArrayList<TestCase>){
         EventBus.getDefault().post(TestResultBean(position,result,caseList))
         finish()
     }

     fun sendCaseResult(caseId : Int,result :Int,type:Int){
         EventBus.getDefault().post(CaseResultBean(caseId,result,type))
     }

    fun testNext(){
        EventBus.getDefault().post(CheckBean(""))
    }

     override fun onDestroy() {
         super.onDestroy()
         currentActivity = null
     }

     private fun hideBottomUIMenu() {
         //隐藏虚拟按键，并且全屏
         if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
             var v = window.decorView
             v.systemUiVisibility = View.GONE
         } else if (Build.VERSION.SDK_INT >= 19) {
             //for new api versions.
             var decorView = getWindow().getDecorView()
             var uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN;
             decorView.setSystemUiVisibility(uiOptions)
         }
     }
}