package com.kintex.check.activity

import android.content.Context
import android.content.Intent
import android.nfc.FormatException
import android.os.Bundle
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.TestCase
import com.kintex.check.utils.NfcUtils
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.NFC_POSITION
import com.kintex.check.utils.ResultCode.PASSED
import kotlinx.android.synthetic.main.activity_nfc.*
import kotlinx.android.synthetic.main.activity_nfc.btn_failed
import kotlinx.android.synthetic.main.title_include.*
import java.io.IOException
import java.io.UnsupportedEncodingException

class NFCActivity : BaseActivity() {
    private var resultCaseList  = arrayListOf<TestCase>(
        TestCase(48,"NFC","",1,0)
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc)
        val nfcUtils = NfcUtils(this)
        setView()

    }

    private fun setView() {

        tv_titleName.text = "NFC Test"
        tv_titleReset.text = "Back"
        tv_titleReset.setOnClickListener {

            finish()

        }

        tv_titleDone.setOnClickListener {
            resultCaseList[0].result = 0
            sendResult(NFC_POSITION, FAILED,resultCaseList)
        }

        btn_failed.setOnClickListener {
            resultCaseList[0].result = 0
            sendResult(NFC_POSITION, FAILED,resultCaseList)

        }

        btn_passed.setOnClickListener {
            resultCaseList[0].result = 1
            sendResult(NFC_POSITION, PASSED,resultCaseList)

        }

    }


    override fun onResume() {
        super.onResume()
        //设定intentfilter和tech-list。如果两个都为null就代表优先接收任何形式的TAG action。也就是说系统会主动发TAG intent。
        if (NfcUtils.mNfcAdapter != null) {
            NfcUtils.mNfcAdapter.enableForegroundDispatch(
                this,
                NfcUtils.mPendingIntent,
                NfcUtils.mIntentFilter,
                NfcUtils.mTechList
            );
        }

    }

    override fun onPause() {
        super.onPause()
        if (NfcUtils.mNfcAdapter != null) {
            NfcUtils.mNfcAdapter.disableForegroundDispatch(this);
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        NfcUtils.mNfcAdapter = null;
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        readWrite(intent);
    }

    private fun readWrite(intent: Intent?) {

         try {
            // 检测卡的id
            val id = NfcUtils.readNFCId(intent)
            // NfcUtils中获取卡中数据的方法
            val result = NfcUtils.readNFCFromTag(intent)
             tv_nfcContent.text = "id:$id"
            // 往卡中写数据
            val data = "1这是写入的数据2"
         //   NfcUtils.writeNFCToTag(this, data, intent)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: FormatException) {
            e.printStackTrace()
        }

    }


    companion object{
        fun start(context: Context){
            XLog.d("start")
            context.startActivity(Intent(context,NFCActivity::class.java))
        }
    }

}