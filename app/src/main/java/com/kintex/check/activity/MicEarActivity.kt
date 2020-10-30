package com.kintex.check.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.TestCase
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.MIC_EAR_POSITION
import com.kintex.check.utils.ResultCode.PASSED
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener
import com.kongzue.dialog.util.BaseDialog
import com.kongzue.dialog.v3.MessageDialog
import kotlinx.android.synthetic.main.activity_micear.*
import kotlinx.android.synthetic.main.title_include.*
import org.greenrobot.eventbus.EventBus

class MicEarActivity  : BaseActivity(){

    private var resultCaseList  = arrayListOf<TestCase>(
        TestCase("Earpiece",12,"Earpiece","",1,0)
    )
    private var mediaPlayer : MediaPlayer ?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_micear)
        setView()
        playSound()
    }

    private fun setView() {
        tv_titleDone.setOnClickListener {
            resultCaseList[0].result = 0
            sendResult(MIC_EAR_POSITION, FAILED,resultCaseList)

        }
        tv_titleName.text = "MIC EAR TEST"
        tv_titleReset.text = "Back"
        tv_titleReset.setOnClickListener {

            finish()

        }


        tv_testMicEar.setOnClickListener {
       //     playSound()

        }

    }

    private fun playSound() {

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = false
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        val streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.MODE_IN_COMMUNICATION)
        audioManager.setStreamVolume(AudioManager.MODE_IN_COMMUNICATION,streamMaxVolume,1)

        val openFd = assets.openFd("morse.wav")
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setDataSource(openFd.fileDescriptor,openFd.startOffset,openFd.length)
        mediaPlayer!!.setOnPreparedListener {
            XLog.d("OnPrepared")
            mediaPlayer!!.start()

        }

        mediaPlayer!!.setOnErrorListener(object : MediaPlayer.OnErrorListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                XLog.d("onError")
                return true
            }
        })
        mediaPlayer!!.setOnCompletionListener {

            showChooseDialog()

        }
        mediaPlayer!!.prepare()

    }


    private fun showChooseDialog() {



        val dialog = MessageDialog.build(this).setTitle("提示")
            .setMessage("请确认听筒能播放清晰明亮的声音")
        dialog.setOkButton("是", object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    tv_testMicEar.text = "PASS"
                    tv_testMicEar.setTextColor(Color.GREEN)
                    dialog!!.doDismiss()
                    resultCaseList[0].result = 1
                    sendResult(MIC_EAR_POSITION, PASSED,resultCaseList)
                    return false
                }
            })
        dialog.setCancelButton("否", object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    tv_testMicEar.text = "FAIL"
                    tv_testMicEar.setTextColor(Color.GREEN)
                    dialog!!.doDismiss()
                    resultCaseList[0].result = 0
                    sendResult(MIC_EAR_POSITION, FAILED,resultCaseList)
                    return false
                }
            })
        dialog.setOtherButton("重试", object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    dialog!!.doDismiss()
                    playSound()
                    return false
                }
            })

        dialog.cancelable = false
        dialog.show()


    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.release()
        mediaPlayer = null

    }

    override fun onDestroy() {
        super.onDestroy()
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = true
    }

    companion object{
        fun start(context: Context){
            XLog.d("start")
            context.startActivity(Intent(context,MicEarActivity::class.java))
        }
    }

}