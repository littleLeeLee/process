package com.kintex.check.audioview

import android.Manifest.permission.RECORD_AUDIO
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.paramsen.noise.Noise
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_audio.*

class AudioActivity : AppCompatActivity() {
    val TAG = javaClass.simpleName!!

    val disposable: CompositeDisposable = CompositeDisposable()

    val p0 = Profiler("p0")
    val p1 = Profiler("p1")
    val p2 = Profiler("p2")
    val p3 = Profiler("p3")

    private var myLog :FloatArray?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

     //   scheduleAbout()
        printLog.setOnClickListener {
            /*  var max = 0f
            for (fl in myLog!!) {
                if(fl>max){
                    max = fl
                }
        }
            System.out.println("FFT:"+max)
        }*/
            playSound()
        }

    }

    override fun onResume() {
        super.onResume()

        if (requestAudio() && disposable.size() == 0)
            start()
    }

    override fun onStop() {
        stop()
        super.onStop()
    }

    /**
     * Subscribe to microphone
     */
    private fun start() {
        val src = AudioSource().stream()
        val noise = Noise.real(4096)

        //AudioView
        disposable
                .add(src.observeOn(Schedulers.newThread())
                .doOnNext { p0.next() }
                .subscribe(audioView::onWindow, { e -> Log.e(TAG, e.message) }))
        //FFTView
        disposable.add(src.observeOn(Schedulers.newThread())
                .doOnNext { p1.next() }
                .map {
                    for (i in it.indices)
                        it[i] *= 2.0f
                    return@map it
                }
                .map { noise.fft(it, FloatArray(4096 + 2)) }
                .doOnNext { p3.next() }
                .subscribe({ fft ->
                    myLog = fft
                    printLog()
                    fftHeatMapView.onFFT(fft)
                    fftBandView.onFFT(fft)
                }, { e -> Log.e(TAG, e.message) }))

      //  tip.schedule()
    }

//    2020-12-01 15:25:49.122 30019-30641/com.kintex.check I/System.out: FFT:2.586552E7
//    2020-12-01 15:25:49.223 30019-30641/com.kintex.check I/System.out: FFT:3.2201112E7
//    2020-12-01 15:25:49.303 30019-30641/com.kintex.check I/System.out: FFT:2.7776076E7
//    2020-12-01 15:25:49.403 30019-30641/com.kintex.check I/System.out: FFT:4.3264296E7
//    2020-12-01 15:25:49.482 30019-30641/com.kintex.check I/System.out: FFT:2.5847224E7
//    2020-12-01 15:25:49.594 30019-30641/com.kintex.check I/System.out: FFT:2.69476E7
//    2020-12-01 15:25:49.673 30019-30641/com.kintex.check I/System.out: FFT:3.7544984E7
//    2020-12-01 15:25:49.773 30019-30641/com.kintex.check I/System.out: FFT:2.6381238E7
//    2020-12-01 15:25:49.873 30019-30641/com.kintex.check I/System.out: FFT:1.4538059E7

//    2020-12-01 15:46:53.295 30651-911/com.kintex.check I/System.out: FFT:3.1581788E7
//    2020-12-01 15:46:53.394 30651-911/com.kintex.check I/System.out: FFT:5.0281688E7
//    2020-12-01 15:46:53.495 30651-911/com.kintex.check I/System.out: FFT:3.4338776E7
//    2020-12-01 15:46:53.575 30651-911/com.kintex.check I/System.out: FFT:3.3598016E7
//    2020-12-01 15:46:53.675 30651-911/com.kintex.check I/System.out: FFT:3.1395574E7
//    2020-12-01 15:46:53.765 30651-911/com.kintex.check I/System.out: FFT:2.8124628E7
//    2020-12-01 15:46:53.865 30651-911/com.kintex.check I/System.out: FFT:2.312828E7
//    2020-12-01 15:46:53.946 30651-911/com.kintex.check I/System.out: FFT:20521628

        fun printLog() {
            var maxArray = ArrayList<Float>()


            for (fl in myLog!!) {
                if(fl>3.0E7){
                    maxArray.add(fl)
                }
            }

          /*  for (max in maxArray){
                System.out.println("FFT:"+max)
            }*/


        }
    private fun playSound(){

        val openFd = assets.openFd("beep_test.wav")
        var  mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(openFd.fileDescriptor,openFd.startOffset,openFd.length)
        mediaPlayer.setOnPreparedListener {
            XLog.d("OnPrepared")
            mediaPlayer.start()

        }

        mediaPlayer.setOnErrorListener(object : MediaPlayer.OnErrorListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                XLog.d("onError")
                return true
            }
        })
        mediaPlayer.setOnCompletionListener {


        }
        mediaPlayer.prepare()

    }

    /**
     * Dispose microphone subscriptions
     */
    private fun stop() {
        disposable.clear()
    }

    /**
     * Output windows of 4096 len, ~10/sec for 44.1khz, accumulates for FFT
     */
    private fun accumulate(o: Flowable<FloatArray>): Flowable<FloatArray> {
        val size = 4096

        return o.map(object : Function<FloatArray, FloatArray> {
            val buf = FloatArray(size * 2)
            val empty = FloatArray(0)
            var c = 0

            override fun apply(window: FloatArray): FloatArray {
                System.arraycopy(window, 0, buf, c, window.size)
                c += window.size

                if (c >= size) {
                    val out = FloatArray(size)
                    System.arraycopy(buf, 0, out, 0, size)

                    if (c > size) {
                        System.arraycopy(buf, c % size, buf, 0, c % size)
                    }

                    c = 0

                    return out
                }

                return empty
            }
        }).filter { fft -> fft.size == size } //filter only the emissions of complete 4096 windows
    }

    private fun accumulate1(o: Flowable<FloatArray>): Flowable<FloatArray> {
        return o.window(6).flatMapSingle { it.collect({ ArrayList<FloatArray>() }, { a, b -> a.add(b) }) }.map { window ->
            val out = FloatArray(4096)
            var c = 0
            for (each in window) {
                if (c + each.size >= 4096)
                    break
                System.arraycopy(each, 0, out, c, each.size)
                c += each.size - 1
            }
            out
        }
    }

    private fun requestAudio(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(RECORD_AUDIO) != PERMISSION_GRANTED) {
            requestPermissions(arrayOf(RECORD_AUDIO), 1337)
            return false
        }

        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] == PERMISSION_GRANTED)
            start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {


        return true
    }


    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context, AudioActivity::class.java))
        }
    }

}
