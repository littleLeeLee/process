package com.kintex.check.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.utils.ResultCode
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener
import com.kongzue.dialog.util.BaseDialog
import com.kongzue.dialog.v3.MessageDialog
import kotlinx.android.synthetic.main.activity_callphone.*
import kotlinx.android.synthetic.main.title_include.*
import javax.xml.parsers.FactoryConfigurationError


class CallPhoneActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_callphone)
        setView()
    }

   private var teleManager : TelephonyManager ?=null
    private var phoneListener : MyPhoneStateListener ?=null
    private fun setView() {

        teleManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simState = teleManager!!.simState
        if(simState == TelephonyManager.SIM_STATE_ABSENT || simState == TelephonyManager.SIM_STATE_UNKNOWN){
            tv_simState.text = "没有发现SIM卡"
        }else{
            tv_simState.text = "SIM卡已插入"
        }

        phoneListener = MyPhoneStateListener()
        teleManager!!.listen(
            phoneListener,
            PhoneStateListener.LISTEN_CALL_STATE
        )

        tv_titleReset.text = "Back"
        tv_titleReset.setOnClickListener {

            finish()

        }
        tv_titleName.text = "CallPhone Test"
        tv_titleDone.setOnClickListener {

            sendResult(ResultCode.TEST_CALL_POSITION, ResultCode.FAILED)
            finish()

        }

        btn_callPhone.setOnClickListener {
            if(TextUtils.isEmpty(ed_inputNum.text)){
                ToastUtils.showShort("请输入正确的号码")
                return@setOnClickListener
            }
            callPhone()

        }


    }

    private fun callPhone() {

        val num = ed_inputNum.text.toString().trim()
        var intent: Intent? = null
        val uri: Uri = Uri.parse("tel:$num")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "请到设置中打开电话权限", Toast.LENGTH_SHORT)
            intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
            return
        }
        intent = Intent(Intent.ACTION_CALL)
        intent.data = uri
        startActivity(intent)
    }
    private var lastCallState = 0
    private var currCallState = 0


    inner  class MyPhoneStateListener : PhoneStateListener() {
        override fun onCallStateChanged(
            state: Int,
            incomingNumber: String
        ) {

            lastCallState = currCallState
            currCallState = state

            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {

                    if (lastCallState == TelephonyManager.CALL_STATE_OFFHOOK) {
                       XLog.d("挂电话！")
                        showChooseDialog()
                    }
                }
                TelephonyManager.CALL_STATE_RINGING -> {

                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {

                }
                else -> {
                }
            }
        }
    }


    private fun showChooseDialog() {

        val dialog = MessageDialog.build(this).setTitle("提示")
            .setMessage("请确认能正常拨打电话")
            .setOkButton("是", object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    dialog!!.dismiss()
                    sendResult(ResultCode.TEST_CALL_POSITION, ResultCode.PASSED)
                    return false
                }
            }).setCancelButton("否", object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    dialog!!.dismiss()
                    sendResult(ResultCode.TEST_CALL_POSITION, ResultCode.FAILED)
                    return false
                }
            })
            .setOtherButton("重试", object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    dialog!!.dismiss()
                    callPhone()
                    return false
                }
            })

        dialog.cancelable = false
        dialog.show()


    }

    override fun onDestroy() {
        super.onDestroy()
        teleManager?.listen(phoneListener,
            PhoneStateListener.LISTEN_NONE)
        phoneListener = null
    }


    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context,CallPhoneActivity::class.java))
        }
    }

}