package com.kintex.check.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.utils.ResultCode
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.HEADSET_POSITION
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener
import com.kongzue.dialog.util.BaseDialog
import com.kongzue.dialog.v3.MessageDialog
import kotlinx.android.synthetic.main.activity_headset.*
import kotlinx.android.synthetic.main.title_include.*

class HeadSetActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_headset)

        setView()

    }
    private var headReceiver : HeadSetReceiver ?=null
    private fun setView() {
        tv_titleReset.text = "Back"
        tv_titleReset.setOnClickListener {

            finish()

        }
        tv_titleName.text = "HeadSet Test"
        tv_titleDone.setOnClickListener {

            sendResult(HEADSET_POSITION, FAILED)
            finish()

        }
        headReceiver = HeadSetReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        registerReceiver(headReceiver,intentFilter)
    }

    private var mediaPlayer : MediaPlayer?=null
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
                        tv_headSet.text = "左声道"
                    }else{
                        tv_headSet.text = "右声道"
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
                showChooseDialog()
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

    private fun showChooseDialog() {



        val dialog = MessageDialog.build(this).setTitle("提示")
            .setMessage("请确认耳机左右声道能听到清晰明亮的声音")
            .setOkButton("是", object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    dialog!!.dismiss()
                    sendResult(ResultCode.HEADSET_POSITION, ResultCode.PASSED)
                    return false
                }
            }).setCancelButton("否", object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    dialog!!.dismiss()
                    sendResult(ResultCode.HEADSET_POSITION, ResultCode.FAILED)
                    return false
                }
            })
            .setOtherButton("重试", object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    dialog!!.dismiss()
                    isFirst = true
                    playWithHeadSet()
                    return false
                }
            })

        dialog.cancelable = false
        dialog.show()


    }


    inner class HeadSetReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            val action = intent!!.action

            when(action){

                Intent.ACTION_HEADSET_PLUG->{
                    val state = intent!!.getIntExtra("state", 0)
                    if(state ==1){
                        XLog.d("插入")
                        isFirst = true
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
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

    }


    companion object{
        fun start(context: Context){
            XLog.d("start")
            context.startActivity(Intent(context,HeadSetActivity::class.java))
        }
    }
}