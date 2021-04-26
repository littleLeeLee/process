package com.kintex.check.activity

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.audioview.AudioSource
import com.kintex.check.audioview.Profiler
import com.kintex.check.utils.AudioUtils
import com.kintex.check.utils.CaseId
import com.kintex.check.utils.NewAudioUtils
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.MANUAL
import com.kintex.check.utils.ResultCode.PASSED
import com.paramsen.noise.Noise
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_audiotest.*
import kotlinx.android.synthetic.main.activity_speaker.*
import java.io.File
import java.lang.Exception
import kotlin.random.Random

class AudioTestActivity : BaseActivity(), View.OnClickListener {

    val disposable: CompositeDisposable = CompositeDisposable()
    val TAG = javaClass.simpleName!!
    val p0 = Profiler("p0")
    val p1 = Profiler("p1")
    val p2 = Profiler("p2")
    val p3 = Profiler("p3")

    var isSpeakerTest = false
    var isEarSpeakerTest = false
    var isBottomMicTest = false
    var currentTest = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audiotest)
        setView()
    }

    private fun setView() {

        btn_play.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View, event: MotionEvent): Boolean {

                when(event.action){

                    MotionEvent.ACTION_DOWN->{
                        stopPlayer()
                        runOnUiThread {
                            tv_resultState.visibility = View.INVISIBLE
                            btn_play.text = resources.getString(R.string.TouchPlay)
                        }
                        startRecorder()
                        playRecoderSound()

                    }

                    MotionEvent.ACTION_UP->{
                        runOnUiThread {
                            btn_play.text = resources.getString(R.string.TouchRecode)
                            tv_resultState.visibility = View.VISIBLE
                            tv_resultState.text = "请确认录音声音是否正常"
                        }
                        stopPlayer()
                        stopRecorder()
                        playRecoder()

                    }
                }
                return false
            }

        })
        btn_fail.setOnClickListener(this)
        btn_pass.setOnClickListener(this)

        btn_play.visibility = View.INVISIBLE
        playSound()
        tv_speakerState.text = resources.getText(R.string.Testing)
        tv_resultState.text = "请确认扬声器是否正常"
    }

    private fun playRecoder() {

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = true
        val streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,streamMaxVolume-1,1)
        val path = "/sdcard/save/test.wav"
        mediaPlayer  = MediaPlayer()
        mediaPlayer!!.setDataSource(path)
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
        mediaPlayer!!.prepare()

    }


    override fun onClick(v: View) {

        when(v.id){

            R.id.btn_fail->{
                sendResultData(FAILED)
            }

            R.id.btn_pass->{
                sendResultData(PASSED)
            }
        }


    }
    private var audioUtils: NewAudioUtils?=null
    private var isRecoding = false
    private fun startRecorder() {
        audioUtils = NewAudioUtils("0",48000)
        isRecoding =  audioUtils!! .startRecord("/sdcard/save/test.wav")
      //  XLog.d("isRecoding:$isRecoding")
    }

    private fun stopRecorder(){
        audioUtils?.stopRecord()
        isRecoding = false
    }

    private fun sendResultData(result : Int) {

        when (currentTest) {
            0 -> {
                if(result == PASSED){
                    tv_speakerState.text = resources.getText(R.string.Passed)
                    tv_speakerState.setTextColor(resources.getColor(R.color.restColor))
                }else{
                    tv_speakerState.text = resources.getText(R.string.Failed)
                    tv_speakerState.setTextColor(resources.getColor(R.color.red))
                }
                sendCaseResult(CaseId.LoudSpeaker.id,result, MANUAL)
                currentTest++
                stopPlayer()
                btn_play.visibility = View.VISIBLE
                tv_resultState.visibility = View.INVISIBLE
                tv_bottomMicState.text = resources.getString(R.string.Testing)
                //    tv_resultState.text = "请确认录音声音状态"
                //   playEarSound()
            }
            1 -> {
                if(result == PASSED){
                    tv_bottomMicState.text = resources.getString(R.string.Passed)
                    tv_bottomMicState.setTextColor(resources.getColor(R.color.restColor))
                }else{
                    tv_bottomMicState.text = resources.getString(R.string.Failed)
                    tv_bottomMicState.setTextColor(resources.getColor(R.color.red))
                }

                sendCaseResult(CaseId.BottomMicrophone.id,result,MANUAL)
                currentTest++
                tv_frontMicState.text = resources.getString(R.string.Testing)
            }
            2 -> {
                if(result == PASSED){
                    tv_frontMicState.text = resources.getString(R.string.Passed)
                    tv_frontMicState.setTextColor(resources.getColor(R.color.restColor))
                }else{
                    tv_frontMicState.text = resources.getString(R.string.Failed)
                    tv_frontMicState.setTextColor(resources.getColor(R.color.red))
                }
                sendCaseResult(CaseId.FrontMicrophone.id,result,MANUAL)
                currentTest++
                tv_videoMicState.text = resources.getString(R.string.Testing)
            }
            3 ->{
                if(result == PASSED){
                    tv_videoMicState.text = resources.getString(R.string.Passed)
                    tv_videoMicState.setTextColor(resources.getColor(R.color.restColor))
                }else{
                    tv_videoMicState.text = resources.getString(R.string.Failed)
                    tv_videoMicState.setTextColor(resources.getColor(R.color.red))
                }
                sendCaseResult(CaseId.VideoMicrophone.id,result,MANUAL)
                currentTest++
                btn_play.visibility = View.INVISIBLE
                tv_earState.text = resources.getString(R.string.Testing)
                tv_resultState.text = "请确认听筒声音是否正常"
                playEarSound()
            }
            4 ->{
                if(result == PASSED){
                    tv_earState.text = resources.getText(R.string.Passed)
                    tv_earState.setTextColor(resources.getColor(R.color.restColor))
                }else{
                    tv_earState.text = resources.getText(R.string.Failed)
                    tv_earState.setTextColor(resources.getColor(R.color.red))
                }
                sendCaseResult(CaseId.EarSpeaker.id,result,MANUAL)
                stopPlayer()
                finish()
            }
        }

    }

    private fun stopPlayer() {

        try {
            if(mediaPlayer != null){

                if(mediaPlayer!!.isPlaying){
                    mediaPlayer!!.stop()
                }
                mediaPlayer!!.reset()
                mediaPlayer!!.release()
            }
        }catch (e:Exception){

        }


    }

    private fun playEarSound() {

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = false
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        val streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,streamMaxVolume,1)
        val streamMaxVolume1 = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,streamMaxVolume1,1)

        val openFd = assets.openFd("opening.wav")
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



        }
        mediaPlayer!!.prepare()

    }
    var  mediaPlayer : MediaPlayer ?=null



    private fun playRecoderSound(){

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = true
        audioManager.mode = AudioManager.MODE_NORMAL
        val streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,streamMaxVolume-2,1)
            val openFd = assets.openFd("opening.wav")
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setDataSource(openFd.fileDescriptor,openFd.startOffset,openFd.length)
            mediaPlayer!!.setOnPreparedListener {
                mediaPlayer!!.start()

            }

            mediaPlayer!!.setOnCompletionListener {



            }

            mediaPlayer!!.setOnErrorListener(object : MediaPlayer.OnErrorListener {
                override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {

                    XLog.d("onError")
                    return true
                }
            })

            mediaPlayer!!.prepare()



    }


    private fun playSound(){

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = true
        val streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,streamMaxVolume-1,1)

        val openFd = assets.openFd("opening.wav")
        mediaPlayer  = MediaPlayer()
        mediaPlayer!!.setDataSource(openFd.fileDescriptor,openFd.startOffset,openFd.length)
        mediaPlayer!!.setOnPreparedListener {
       //     XLog.d("OnPrepared")
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
        mediaPlayer!!.prepare()

    }

    override fun onDestroy() {
        super.onDestroy()
            testNext()
    }


    override fun onResume() {
        super.onResume()
        val path = "/sdcard/save/"
        val file = File(path)
        if (!file.exists())
            file.mkdirs()
        start()
    }

    private fun start() {
        val src = AudioSource().stream()
        val noise = Noise.real(4096)
        //AudioView
        disposable
                .add(src.observeOn(Schedulers.newThread())
                        .doOnNext {

                        }
                        .subscribe(shouAudio::onWindow) { e -> Log.e(TAG, e.message) })


    }

    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context,AudioTestActivity::class.java))
        }
    }


    override fun onStop() {
        super.onStop()
        stopPlayer()
    }
}