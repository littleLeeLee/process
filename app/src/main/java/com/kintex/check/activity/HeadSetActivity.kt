package com.kintex.check.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.utils.CaseId
import com.kintex.check.utils.ResultCode
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.PASSED
import kotlinx.android.synthetic.main.activity_headset.*
import kotlinx.android.synthetic.main.title_include.*
import java.lang.Exception

class HeadSetActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_headset)
        setView()
    }
    var currentTest = 0

    private var headReceiver : HeadSetReceiver ?=null
    private fun setView() {
        btn_headPassed.isEnabled = false
        tv_titleReset.text = "Back"
        tv_titleReset.setOnClickListener {

            finish()

        }
        tv_titleName.text = "HeadSet Test"
        tv_titleDone.setOnClickListener {

            finish()

        }
        headReceiver = HeadSetReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        registerReceiver(headReceiver,intentFilter)

        btn_headFiled.setOnClickListener {
            sendReslutData(FAILED)
            currentTest++
        }

        btn_headPassed.setOnClickListener {
            sendReslutData(PASSED)
            currentTest++
        }

    }

    private fun sendReslutData(result: Int) {
        when(currentTest){
            0->{
                if(result == PASSED){
                    tv_headSetResult.text = resources.getString(R.string.Passed)
                    tv_headSetResult.setTextColor(resources.getColor(R.color.restColor))
                }else{
                    tv_headSetResult.text = resources.getString(R.string.Failed)
                    tv_headSetResult.setTextColor(resources.getColor(R.color.red))
                    tv_headRight.text = resources.getString(R.string.Failed)
                    tv_headRight.setTextColor(resources.getColor(R.color.red))
                    tv_headLeft.text = resources.getString(R.string.Failed)
                    tv_headLeft.setTextColor(resources.getColor(R.color.red))
                    sendCaseResult(CaseId.HeadsetPort.id, FAILED, ResultCode.MANUAL)
                    sendCaseResult(CaseId.HeadsetRight.id, FAILED, ResultCode.MANUAL)
                    sendCaseResult(CaseId.HeadsetLeft.id, FAILED, ResultCode.MANUAL)
                    finish()
                }
            }
            1->{
                if(result == PASSED){
                    tv_headRight.text = resources.getString(R.string.Passed)
                    tv_headRight.setTextColor(resources.getColor(R.color.restColor))
                }else{
                    tv_headRight.text = resources.getString(R.string.Failed)
                    tv_headRight.setTextColor(resources.getColor(R.color.red))
                }
                sendCaseResult(CaseId.HeadsetRight.id, result, ResultCode.MANUAL)
                tv_headLeft.text = resources.getString(R.string.Testing)
                mediaPlayer!!.setVolume(1f,0f)
            }
            2->{
                if(result == PASSED){
                    tv_headLeft.text = resources.getString(R.string.Passed)
                    tv_headLeft.setTextColor(resources.getColor(R.color.restColor))
                }else{
                    tv_headLeft.text = resources.getString(R.string.Failed)
                    tv_headLeft.setTextColor(resources.getColor(R.color.red))
                }
                sendCaseResult(CaseId.HeadsetLeft.id, result,ResultCode.MANUAL)
                finish()
            }
        }

    }

    private var mediaPlayer : MediaPlayer?=null
    private fun playWithHeadSet(){

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = false
        val streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.MODE_IN_COMMUNICATION)
        audioManager.setStreamVolume(AudioManager.MODE_IN_COMMUNICATION,(streamMaxVolume * 0.8).toInt(),1)
        val openFd = assets.openFd("opening.wav")
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setDataSource(openFd.fileDescriptor,openFd.startOffset,openFd.length)
        mediaPlayer!!.setOnPreparedListener {

            mediaPlayer!!.start()

        }

        mediaPlayer!!.setOnErrorListener(object : MediaPlayer.OnErrorListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                XLog.d("onError")
                return true
            }
        })
        mediaPlayer!!.setOnCompletionListener {

        }
       mediaPlayer!!.setVolume(0f,1f)
        mediaPlayer!!.prepare()

    }

    private var isFirst = true




    inner class HeadSetReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            val action = intent!!.action

            when(action){

                Intent.ACTION_HEADSET_PLUG->{
                    val state = intent!!.getIntExtra("state", 0)
                    if(state ==1){
                        sendCaseResult(CaseId.HeadsetPort.id, PASSED,ResultCode.MANUAL)
                        XLog.d("插入")
                        runOnUiThread {
                            btn_headPassed.isEnabled = true
                            tv_headSetResult.text = resources.getString(R.string.Passed)
                            tv_headSetResult.setTextColor(resources.getColor(R.color.restColor))
                            tv_headRight.text = resources.getString(R.string.Testing)
                        }
                        currentTest++
                        playWithHeadSet()
                    }else if(state == 0){
                        XLog.d("拔出")
                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                        mediaPlayer = null
                    }


                }


            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(headReceiver)
        try {
                testNext()
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }catch (e:Exception){

        }


    }


    companion object{
        fun start(context: Context){
            XLog.d("start")
            context.startActivity(Intent(context,HeadSetActivity::class.java))
        }
    }
}