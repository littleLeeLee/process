package com.kintex.check.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.TestCase
import com.kintex.check.utils.AudioUtils
import com.kintex.check.utils.ResultCode
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.MIC_LOUD_POSITION
import com.kintex.check.utils.ResultCode.PASSED
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener
import com.kongzue.dialog.util.BaseDialog
import com.kongzue.dialog.v3.MessageDialog
import kotlinx.android.synthetic.main.activity_headset.*
import kotlinx.android.synthetic.main.activity_micear.*
import kotlinx.android.synthetic.main.activity_speaker.*
import kotlinx.android.synthetic.main.title_include.*
import kotlin.random.Random

class SpeakerActivity : BaseActivity() {

    private var resultCaseList  = arrayListOf<TestCase>(
        TestCase(11,"Loud Speaker","",1,0),
        TestCase(15,"Microphone","",1,0),
        TestCase(16,"Video Microphone","",1,0),
        TestCase(12,"Earpiece","",1,0),
        TestCase(20,"Headset Port","",1,0),
        TestCase(18,"Headset-Left","",1,0),
        TestCase(19,"Headset-Right","",1,0)
    )
    private var mediaPlayer : MediaPlayer ?=null
    private var isComplete = false
    private var hasError = false
    private var completeCount = 0
    private var isLoudPass = false
    private var isMicPass = false
    private var isVideoMicPass = false
    private var isEarPass = false
    private var isHeadSetPortPass = false
    private var isHeadSetLeftPass = false
    private var isHeadSetRightPass = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance= this
        setContentView(R.layout.activity_speaker)
        setView()
    }

    private fun setView() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_NORMAL
        val streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,streamMaxVolume,1)
        audioManager.isSpeakerphoneOn = true

        tv_titleReset.text = "Back"
        tv_titleReset.setOnClickListener {

            finish()

        }

        tv_titleName.text = "Mic Loud Speaker Test"

        tv_titleDone.setOnClickListener {
            if(isLoudPass){
                resultCaseList[0].result = 1
            }else{
                resultCaseList[0].result = 0
            }
            if(isMicPass){
                resultCaseList[1].result = 1
            }else{
                resultCaseList[1].result = 0
            }

            if(isVideoMicPass){
                resultCaseList[2].result = 1
            }else{
                resultCaseList[2].result = 0
            }

            if(isEarPass){
                resultCaseList[3].result = 1
            }else{
                resultCaseList[3].result = 0
            }

            if(isHeadSetPortPass){
                resultCaseList[4].result = 1
            }else{
                resultCaseList[4].result = 0
            }

            if(isHeadSetLeftPass){
                resultCaseList[5].result = 1
            }else{
                resultCaseList[5].result = 0
            }

            if(isHeadSetRightPass){
                resultCaseList[6].result = 1
            }else{
                resultCaseList[6].result = 0
            }

            sendResult(MIC_LOUD_POSITION, FAILED,resultCaseList)
        }
        tv_play.setOnClickListener {

                mediaPlayer!!.start()
                isComplete = false
        }

        tv_recoder.setOnClickListener {
            if(isRecoding){
                audioUtils!!.stopRecord()
                isRecoding = false
            }else{
                audioUtils = AudioUtils("0",48000)
                audioUtils!! .startRecord("/sdcard/save/test.wav")
                isRecoding = true
                updateVolume()
            }


        }
        startSpeaker()

    }




    private fun startVideoMic() {

        initMediaData()

    }

    private fun startRecorder() {
        maxVolume = 0
        audioUtils = AudioUtils("0",48000)
        audioUtils!! .startRecord("/sdcard/save/test.wav")
        isRecoding = true
        updateVolume()
    }

    private fun stopRecorder(){
        audioUtils?.stopRecord()
        isRecoding = false
        updateVolume()
    }

    private fun startMicrophone() {

        initMediaData()

    }

    private fun startSpeaker() {

        initMediaData()

    }

    private var maxVolume = 0

    private fun updateVolume() {

        if(isRecoding){
            Thread(){
                kotlin.run {

                    while (isRecoding){
                        val currentVolume = audioUtils!!.currentVolume
                        view_speak.setMaxPoint(currentVolume)
                        if(maxVolume<=currentVolume){
                            maxVolume = currentVolume
                        }
                        Thread.sleep(30)
                    }

                }
            }.start()
        }else{
            view_speak.setMaxPoint(maxVolume)
        }
    }

    private var audioUtils: AudioUtils ?=null
    private var isPrepared = false
    private var isRecoding = false

    private fun initMediaData() {
        val openFd = assets.openFd("opening.wav")
        if(mediaPlayer != null){
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
        }

        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setDataSource(openFd.fileDescriptor,openFd.startOffset,openFd.length)
        mediaPlayer!!.setOnPreparedListener {
            XLog.d("OnPrepared")
            isPrepared = true
            startRecorder()
            mediaPlayer!!.start()

        }

        mediaPlayer!!.setOnCompletionListener {
            if(maxVolume>=100){
                maxVolume -= Random(1).nextInt(10)
            }
            isComplete = true
            completeCount++
            XLog.d("complete")
            when (completeCount) {
                1 -> {
                    //
                    stopRecorder()
                    if(maxVolume>60){
                        isLoudPass = true
                        tv_loudSpeaker.text = "DB:${maxVolume} pass"
                        tv_loudSpeaker.setTextColor(resources.getColor(R.color.green))
                    }else{
                        isLoudPass = false
                        tv_loudSpeaker.text = "DB:${maxVolume} fail"
                        tv_loudSpeaker.setTextColor(resources.getColor(R.color.red))
                    }
                    tv_loudSpeaker.postDelayed(Runnable {
                        startMicrophone()
                    },1000)

                }
                2 -> {
                    stopRecorder()
                    if(maxVolume>60){
                        isMicPass = true
                        tv_microphone.text = "DB:${maxVolume} pass"
                        tv_microphone.setTextColor(resources.getColor(R.color.green))
                    }else{
                        isMicPass = false
                        tv_microphone.text = "DB:${maxVolume} fail"
                        tv_microphone.setTextColor(resources.getColor(R.color.red))
                    }
                    tv_loudSpeaker.postDelayed(Runnable {
                        startVideoMic()
                    },1000)

                }
                3 -> {

                    stopRecorder()
                    if(maxVolume>60){
                        isVideoMicPass = true
                        tv_videoMic.text = "DB:${maxVolume} pass"
                        tv_videoMic.setTextColor(resources.getColor(R.color.green))
                    }else{
                        isVideoMicPass = false
                        tv_videoMic.text = "DB:${maxVolume} fail"
                        tv_videoMic.setTextColor(resources.getColor(R.color.red))
                    }

                    if(isLoudPass){
                        resultCaseList[0].result = 1
                    }else{
                        resultCaseList[0].result = 0
                    }
                    if(isMicPass){
                        resultCaseList[1].result = 1
                    }else{
                        resultCaseList[1].result = 0
                    }

                    if(isVideoMicPass){
                        resultCaseList[2].result = 1
                    }else{
                        resultCaseList[2].result = 0
                    }

                    tv_videoMic.postDelayed(Runnable {
                        playEarSound()
                    },200)


                }
            }

        }

        mediaPlayer!!.setOnErrorListener(object : MediaPlayer.OnErrorListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                hasError = true
                XLog.d("onError")
                return true
            }
        })

        mediaPlayer!!.prepare()

    }


    private fun playEarSound() {
        ToastUtils.showLong("请将耳朵靠近手机听筒")
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = false
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        val streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,streamMaxVolume,1)

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
                tv_earResult.text = "PASS"
                tv_earResult.setTextColor(Color.GREEN)
                dialog!!.doDismiss()
                isEarPass = true
                resultCaseList[3].result = 1
                setHeadSetData()
             //   sendResult(ResultCode.MIC_EAR_POSITION, PASSED,resultCaseList)
                return false
            }
        })
        dialog.setCancelButton("否", object : OnDialogButtonClickListener {
            override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                tv_earResult.text = "FAIL"
                tv_earResult.setTextColor(Color.RED)
                dialog!!.doDismiss()
                isEarPass = false
                resultCaseList[3].result = 0
                setHeadSetData()
            //    sendResult(ResultCode.MIC_EAR_POSITION, FAILED,resultCaseList)
                return false
            }
        })
        dialog.setOtherButton("重试", object : OnDialogButtonClickListener {
            override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                dialog!!.doDismiss()
                playEarSound()
                return false
            }
        })

        dialog.cancelable = false
        dialog.show()
    }

    private fun setHeadSetData(){

        headReceiver = HeadSetReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        registerReceiver(headReceiver,intentFilter)
        if(!isInset){
            tv_channel.text = "请插入耳机"
        }

    }


    private fun playWithHeadSet(){

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = false
        val streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.MODE_IN_COMMUNICATION)
        audioManager.setStreamVolume(AudioManager.MODE_IN_COMMUNICATION,streamMaxVolume,1)
        val openFd = assets.openFd("morse.wav")
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setDataSource(openFd.fileDescriptor,openFd.startOffset,openFd.length)
        mediaPlayer!!.setOnPreparedListener {
            XLog.d("OnPrepared")

            runOnUiThread {
                if(isFirst){
                    tv_channel.text = "Testing Left Channel..."
                }else{
                    tv_channel.text = "Testing Right Channel..."
                }
            }

            mediaPlayer!!.start()

        }

        mediaPlayer!!.setOnErrorListener(object : MediaPlayer.OnErrorListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                XLog.d("onError")
                return true
            }
        })
        mediaPlayer!!.setOnCompletionListener {
            if(isFirst){
                isFirst = false
                playWithHeadSet()
            }else{
                showChooseHeadSetDialog()
            }


        }
        if(isFirst){
            mediaPlayer!!.setVolume(1f,0f)
        }else{
            mediaPlayer!!.setVolume(0f,1f)
        }

        mediaPlayer!!.prepare()

    }


    private var isFirst = true

    private fun showChooseHeadSetDialog() {
        val dialog = MessageDialog.build(this).setTitle("提示")
            .setMessage("请确认耳机左右声道能听到清晰明亮的声音")
        dialog.setOkButton("是", object : OnDialogButtonClickListener {
            override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                dialog!!.doDismiss()
                isHeadSetRightPass = true
                isHeadSetLeftPass = true
                resultCaseList[5].result = 1
                resultCaseList[6].result = 1
                sendPassInfo()
            //    sendResult(ResultCode.HEADSET_POSITION, ResultCode.PASSED,resultCaseList)
                return false
            }
        })
        dialog.setCancelButton("否", object : OnDialogButtonClickListener {
            override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                dialog!!.doDismiss()
                isHeadSetRightPass = false
                isHeadSetLeftPass = false
                resultCaseList[5].result = 0
                resultCaseList[6].result = 0
            //    sendResult(ResultCode.HEADSET_POSITION, ResultCode.FAILED,resultCaseList)
                sendPassInfo()
                return false
            }
        })
        dialog.setOtherButton("重试", object : OnDialogButtonClickListener {
            override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                dialog!!.doDismiss()
                if(isInset){
                    isFirst = true
                    playWithHeadSet()
                }else{
                    ToastUtils.showShort("请插入耳机")
                }

                return false
            }
        })

        dialog.cancelable = false
        dialog.show()
    }

    private fun sendPassInfo() {

        XLog.d(isHeadSetRightPass)
        XLog.d(isHeadSetLeftPass)
        XLog.d(isEarPass)
        XLog.d(isMicPass)
        XLog.d(isLoudPass)
        XLog.d(isHeadSetPortPass)
        XLog.d(isVideoMicPass)

        if(isHeadSetRightPass&&isHeadSetLeftPass&&isEarPass&&isMicPass&&isLoudPass&&isHeadSetPortPass&&isVideoMicPass){
            sendResult(MIC_LOUD_POSITION, PASSED,resultCaseList)
        }else{
            sendResult(MIC_LOUD_POSITION, FAILED,resultCaseList)
        }


    }

    private var headReceiver : HeadSetReceiver?=null
    private var isInset = false
    inner class HeadSetReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            val action = intent!!.action

            when(action){

                Intent.ACTION_HEADSET_PLUG->{
                    val state = intent!!.getIntExtra("state", 0)
                    if(state ==1){
                        resultCaseList[0].result = 1
                        XLog.d("插入")
                        isFirst = true
                        isInset = true
                        isHeadSetPortPass = true
                        resultCaseList[4].result = 1
                        playWithHeadSet()
                    }else if(state == 0){
                        XLog.d("拔出")
                        isFirst = false
                        isInset = false
                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                        mediaPlayer = null
                    }
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        if(mediaPlayer != null){

            if(mediaPlayer!!.isPlaying){
                mediaPlayer!!.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        }


        stopRecorder()

    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
    companion object{
        var instance : SpeakerActivity?=null
        fun start(context: Context){
            context.startActivity(Intent(context,SpeakerActivity::class.java))
        }
    }

}