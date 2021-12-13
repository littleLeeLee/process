package com.kintex.check.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import com.blankj.utilcode.util.ToastUtils
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
    var currentTest = CaseId.HeadsetPort.id


    private var isHeadSetPass = false
    private var isRightPass = false
    private var isLeftPass = false



    private var headReceiver : HeadSetReceiver ?=null
    private fun setView() {
        btn_headPassed.isEnabled = false
        tv_titleReset.text = "Back"
        tv_titleReset.setOnClickListener {
            finish()
        }
        tv_titleName.text = "耳机测试"
        tv_titleDone.setOnClickListener {
            finish()
        }
        headReceiver = HeadSetReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        registerReceiver(headReceiver,intentFilter)

        btn_headFiled.setOnClickListener {

            sendReslutData(FAILED,currentTest)

        }

        btn_headPassed.setOnClickListener {

            sendReslutData(PASSED,currentTest)
        }

    }

    private fun sendReslutData(result: Int,caseID: Int) {
        //Toast.makeText(this,"curr:$currentTest",Toast.LENGTH_SHORT).show()
        when(caseID){
            CaseId.HeadsetPort.id->{
                if(result == PASSED){
                    btn_headPassed.isEnabled = true
                    tv_headSetResult.text = resources.getString(R.string.Passed)
                    tv_headSetResult.setTextColor(resources.getColor(R.color.restColor))
                    tv_headRight.text = resources.getString(R.string.Testing)
                    sendCaseResult(CaseId.HeadsetPort.id, PASSED,ResultCode.MANUAL)
                    if(!isPlaying){
                        playWithHeadSet()
                        XLog.d("playWithHeadSet")
                    }
                    currentTest = CaseId.HeadsetRight.id

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
            //右声道的结果
            CaseId.HeadsetRight.id->{
                if(result == PASSED){
                    tv_headRight.text = resources.getString(R.string.Passed)
                    tv_headRight.setTextColor(resources.getColor(R.color.restColor))
                }else{
                    tv_headRight.text = resources.getString(R.string.Failed)
                    tv_headRight.setTextColor(resources.getColor(R.color.red))
                }
                sendCaseResult(CaseId.HeadsetRight.id, result, ResultCode.MANUAL)
                tv_headLeft.text = resources.getString(R.string.Testing)
                currentTest = CaseId.HeadsetLeft.id
                mediaPlayer!!.setVolume(1f,0f)
            }

            CaseId.HeadsetLeft.id->{
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
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,(streamMaxVolume * 0.7).toInt(),1)
        val openFd = assets.openFd("opening.wav")
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setDataSource(openFd.fileDescriptor,openFd.startOffset,openFd.length)
        mediaPlayer!!.setOnPreparedListener {
            mediaPlayer!!.setVolume(0f,1f)
            mediaPlayer!!.start()
            isPlaying = true
            XLog.d("start")
        }

        mediaPlayer!!.setOnErrorListener(object : MediaPlayer.OnErrorListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                ToastUtils.showShort("播放声音异常")
                XLog.d("onError")
                return true
            }
        })
        mediaPlayer!!.setOnCompletionListener {
            XLog.d("OnComplet")
        }
        mediaPlayer!!.prepare()

    }

    private var isPlaying =  false
    inner class HeadSetReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            val action = intent!!.action

            when(action){

                Intent.ACTION_HEADSET_PLUG->{
                    val state = intent!!.getIntExtra("state", 0)
                    if(state ==1){
                    //    ToastUtils.showShort("耳机插入")
                        XLog.d("插入")
                        sendReslutData(PASSED,CaseId.HeadsetPort.id)

                    //    Toast.makeText(this@HeadSetActivity,"插入",Toast.LENGTH_SHORT).show()
                    }else if(state == 0){
                        btn_headPassed.isEnabled = false
                        isPlaying = false
                        currentTest = CaseId.HeadsetPort.id
                        XLog.d("拔出")
                  //      ToastUtils.showShort("耳机拔出")
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
            isPlaying = false
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