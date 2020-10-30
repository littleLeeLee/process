package com.kintex.check.activity

import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.TestCase
import com.kintex.check.utils.PhoneInfoCheck
import com.kintex.check.utils.ResultCode
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.FINGER_POSITION
import com.kintex.check.utils.ResultCode.PASSED
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener
import com.kongzue.dialog.util.BaseDialog
import com.kongzue.dialog.v3.MessageDialog
import kotlinx.android.synthetic.main.title_include.*
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class FingerPrintActivity : BaseActivity() {

    private var resultCaseList = arrayListOf<TestCase>(
        TestCase("Fingerprint Sensor",27,"Fingerprint","",1,0)
    )
    private var biometricPrompt : BiometricPrompt ?= null
    private var authenticationCallback : MyAuthenticationCallback ?=null
    private var isHeightVersion = false
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint)
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            ToastUtils.showShort("该手机版本过低")
            resultCaseList[0].result = 0
            sendResult(FINGER_POSITION,FAILED,resultCaseList)
            return
        }


        if(Build.VERSION.SDK_INT >Build.VERSION_CODES.M && Build.VERSION.SDK_INT< Build.VERSION_CODES.P){
            isHeightVersion = false

        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            isHeightVersion = true
        }

        setView()
    }

    private fun setView() {

        tv_titleName.text = "FingerPrint Test"
        tv_titleReset.setOnClickListener {

            finish()

        }

        tv_titleDone.setOnClickListener {
            resultCaseList[0].result = 0
            sendResult(FINGER_POSITION, FAILED,resultCaseList)

        }

    }


    @RequiresApi(Build.VERSION_CODES.P)
    fun heightVersionCheck(){

        biometricPrompt = BiometricPrompt.Builder(this).setTitle("指纹验证").setDescription("请用录入的手指解锁\n短时间五次解锁失败会使指纹模块停用")
            .setNegativeButton("取消", mainExecutor, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    resultCaseList[0].result = 0
                    sendResult(FINGER_POSITION, FAILED,resultCaseList)
                }
            })
            .build()
        authenticationCallback = MyAuthenticationCallback()
        biometricPrompt!!.authenticate(cancellationSignal!!, mainExecutor,authenticationCallback!!)
    }


    @RequiresApi(Build.VERSION_CODES.P)
    inner class MyAuthenticationCallback : BiometricPrompt.AuthenticationCallback() {

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
            super.onAuthenticationError(errorCode, errString)
            ToastUtils.showShort("指纹模块已停用，请等待30秒")
            XLog.d("onAuthenticationError$errString errorCode$errorCode")
            if(errorCode == 7 || errorCode == 9){
                resultCaseList[0].result = 0
                   sendResult(FINGER_POSITION, FAILED,resultCaseList)
            }else{
                ToastUtils.showShort(errString)
                resultCaseList[0].result = 0
                sendResult(FINGER_POSITION, FAILED,resultCaseList)
            }

        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
            super.onAuthenticationSucceeded(result)
            XLog.d("onAuthenticationSucceeded$result")
            ToastUtils.showShort("PASS")
            resultCaseList[0].result = 1
            sendResult(FINGER_POSITION, PASSED,resultCaseList)
        }


        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            XLog.d("onAuthenticationFailed")
        }

    }


    private fun lowVersionCheck(){
        ToastUtils.showLong("请用录入的手指解锁，失败五次指纹传感器会停用30秒")
        val fingerprintManager =
            getSystemService(FingerprintManager::class.java) as FingerprintManager

        val hardwareDetected = fingerprintManager.isHardwareDetected
        if(!hardwareDetected){
            ToastUtils.showShort("该手机不支持指纹功能")
            return
        }

        initKey()

        fingerprintManager.authenticate(FingerprintManager.CryptoObject(cipher!!),
            cancellationSignal
        ,0,object : FingerprintManager.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    ToastUtils.showShort("指纹模块已停用，请等待30秒")
                    XLog.d("onAuthenticationError$errString errorCode$errorCode")
                    if(errorCode == 7 || errorCode == 9){
                        resultCaseList[0].result = 0
                        sendResult(FINGER_POSITION, FAILED,resultCaseList)
                    }else if(errorCode == 10){
                        resultCaseList[0].result = 0
                        sendResult(FINGER_POSITION, FAILED,resultCaseList)
                    }else{
                        ToastUtils.showShort(errString)
                        resultCaseList[0].result = 0
                        sendResult(FINGER_POSITION, FAILED,resultCaseList)
                    }
                }

                override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)
                    XLog.d("onAuthenticationSucceeded$result")
                    ToastUtils.showShort("PASS")
                    resultCaseList[0].result = 1
                    sendResult(FINGER_POSITION, PASSED,resultCaseList)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }




            }, null )


    }

    private var cipher: Cipher ?=null
    @TargetApi(23)
    private fun initKey() {
        try {
          var  keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val keyGenerator: KeyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val builder = KeyGenParameterSpec.Builder(
                "default_key",
                KeyProperties.PURPOSE_ENCRYPT or
                        KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            keyGenerator.init(builder.build())
            keyGenerator.generateKey()

            val key: SecretKey = keyStore.getKey("default_key", null) as SecretKey

            cipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7
            )
            cipher!!.init(Cipher.ENCRYPT_MODE, key)

        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private var cancellationSignal:CancellationSignal ?= null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()
        val hasEnrolledFingerprints = FingerprintManagerCompat.from(this).hasEnrolledFingerprints()
        if(!hasEnrolledFingerprints){
             //   ToastUtils.showShort("请先录入指纹")
                showInputDialog()
                return
        }

        cancellationSignal = CancellationSignal()
        if(isHeightVersion){

            heightVersionCheck()
         //   lowVersionCheck()

        }else{

            lowVersionCheck()

        }
    }

    private fun showInputDialog() {

        val dialog = MessageDialog.build(this).setTitle("提示")
            .setMessage("请先录入一个指纹")
        dialog.setOkButton("好的", object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    dialog!!.doDismiss()
                    gotoSet()
                    return false
                }
            })
        dialog.setCancelButton("取消", object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    dialog!!.doDismiss()
                    resultCaseList[0].result = 0
                    sendResult(FINGER_POSITION, ResultCode.FAILED,resultCaseList)
                    return false
                }
            })


        dialog.cancelable = false
        dialog.show()
    }

    private fun gotoSet() {
        val phoneInfoCheck = PhoneInfoCheck.getInstance(this, android.os.Build.BRAND)
        phoneInfoCheck.startFingerprint()

    }


    override fun onPause() {
        super.onPause()
        if (cancellationSignal != null) {
            cancellationSignal!!.cancel()
            cancellationSignal = null
        }

        if(authenticationCallback != null){
            authenticationCallback = null
        }
    }

    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context,FingerPrintActivity::class.java))
        }
    }

}