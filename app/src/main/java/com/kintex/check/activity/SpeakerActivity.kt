package com.kintex.check.activity

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.TestResultBean
import com.kintex.check.utils.AudioUtils
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.MIC_LOUD_POSITION
import com.kintex.check.utils.ResultCode.PASSED
import kotlinx.android.synthetic.main.activity_speaker.*
import kotlinx.android.synthetic.main.title_include.*
import org.greenrobot.eventbus.EventBus
import kotlin.random.Random

class SpeakerActivity : BaseActivity() {

    private var mediaPlayer : MediaPlayer ?=null
    private var isComplete = false
    private var hasError = false
    private var completeCount = 0
    private var isLoudPass = false
    private var isMicPass = false
    private var isVideoMicPass = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speaker)
        setView()
    }

    private fun setView() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,streamMaxVolume,1)


        tv_titleReset.text = "Back"
        tv_titleReset.setOnClickListener {

            finish()

        }

        tv_titleName.text = "Mic Loud Speaker Test"

        tv_titleDone.setOnClickListener {
            EventBus.getDefault().post(TestResultBean(MIC_LOUD_POSITION, FAILED))
            finish()
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
        val openFd = assets.openFd("morse.wav")
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

                    tv_videoMic.postDelayed(Runnable {

                        if(isLoudPass && isMicPass && isVideoMicPass){

                            EventBus.getDefault().post(TestResultBean(MIC_LOUD_POSITION, PASSED))

                        }else{

                            EventBus.getDefault().post(TestResultBean(MIC_LOUD_POSITION, FAILED))

                        }
                        finish()
                    },100)

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

    override fun onPause() {
        super.onPause()
        mediaPlayer?.release()
        mediaPlayer = null

        stopRecorder()

    }


    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context,SpeakerActivity::class.java))
        }
    }

}