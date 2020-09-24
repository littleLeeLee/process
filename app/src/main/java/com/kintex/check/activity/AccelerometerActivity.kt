package com.kintex.check.activity

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.TestResultBean
import com.kintex.check.utils.ResultCode
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.PASSED
import kotlinx.android.synthetic.main.activity_accelerometer.*
import kotlinx.android.synthetic.main.title_include.*
import org.greenrobot.eventbus.EventBus
import java.lang.Math.abs
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class AccelerometerActivity : BaseActivity() ,SensorEventListener{

     var sensorManager : SensorManager ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accelerometer)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mRenderer = MyRenderer(this,sensorManager!!)
        view_GLSurfaceView.setRenderer(mRenderer)
        tv_titleName.text = "Accelerometer"
        tv_titleReset.text = "back"
        tv_titleReset.setOnClickListener {

            finish()

        }
        tv_titleDone.setOnClickListener {

            EventBus.getDefault().post(TestResultBean(ResultCode.ACCELEROMETER_POSITION, FAILED))
            finish()

        }

    }



    //环境光
    private var lightSensor: Sensor?=null
    //重力感应器
    private var gravitySensor: Sensor?=null
    //加速度传感器
    private var accelerometerSensor: Sensor?=null
    //陀螺仪
    private var gyroscopeSensor: Sensor?=null

    private fun getAllSensor() {

        val sensorList = sensorManager!!.getSensorList(Sensor.TYPE_ALL)
            XLog.d("sensor list :" + sensorList.size)

        gravitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_GRAVITY)
        accelerometerSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        //旋转矢量传感器
        //  val rotationSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        gyroscopeSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)

        if(gravitySensor != null){
            sensorManager!!.registerListener(this,gravitySensor,1000000)
        }

        if(accelerometerSensor != null){
            sensorManager!!.registerListener(this,accelerometerSensor,1000000)
        }

        if(gyroscopeSensor != null){
            sensorManager!!.registerListener(this,gyroscopeSensor,50000)
        }

        if(lightSensor != null){
            sensorManager!!.registerListener(this,lightSensor,100000)
        }

    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {


    }
    private var isAccPassed =false
    private var isGyrPassed =false
    private var isScrPassed =false
    private var isGraPassed =false
    private var isLightPassed =false

    private var isAccFirst = true
    private var isGyrFirst = true
    private var isScrFirst = true
    private var isGraFirst = true
    private var isLightFirst = true

    private var gyrFirstX = 0f
    private var gyrFirstY = 0f
    private var gyrFirstZ = 0f
    private var isGyrX = false
    private var isGyrY = false
    private var isGyrZ = false

    private var accFirstX = 0f
    private var accFirstY = 0f
    private var accFirstZ = 0f
    private var isAccX = false
    private var isAccY = false
    private var isAccZ = false

    private var scrFirstX = 0f
    private var scrFirstY = 0f
    private var scrFirstZ = 0f
    private var isScrX = false
    private var isScrY = false
    private var isScrZ = false

    private var graFirstX = 0f
    private var graFirstY = 0f
    private var graFirstZ = 0f
    private var isGraX = false
    private var isGraY = false
    private var isGraZ = false

    private var ligFirstX = 0f


    override fun onSensorChanged(event: SensorEvent?) {

        when(event!!.sensor.type){

            Sensor.TYPE_GYROSCOPE->{
                XLog.d("TYPE_GYROSCOPE")
                val X_lateral: Float = event.values[0]
                val Y_longitudinal: Float = event.values[1]
                val Z_vertical: Float = event.values[2]
                if(isGyrPassed){
                    tv_gyroscope.text = "Gyroscope : pass"
                    tv_gyroscope.setTextColor(resources.getColor(R.color.green))
                    checkPassed()
                    return
                }
                tv_gyroscope.text = "Gyroscope [ X:$X_lateral  \nY:$Y_longitudinal Z:$Z_vertical ]"
                if(isGyrFirst){
                    isGyrFirst = false
                    gyrFirstX = X_lateral
                    gyrFirstY = Y_longitudinal
                    gyrFirstZ = Z_vertical
                }else{

                    if(kotlin.math.abs(X_lateral - gyrFirstX) > 3){
                        isGyrX = true
                    }
                    if(kotlin.math.abs(Y_longitudinal - gyrFirstY) > 3){
                        isGyrY = true
                    }
                    if(kotlin.math.abs(Z_vertical - gyrFirstZ) > 3){
                        isGyrZ = true
                    }
                    isGyrPassed =  isGyrX&&isGyrY&&isGyrZ
                    XLog.d("isGyrPassed:$isGyrPassed")
                }


            }

            Sensor.TYPE_GRAVITY->{
                XLog.d("TYPE_GRAVITY")
                val X_lateral: Float = event.values[0]
                val Y_longitudinal: Float = event.values[1]
                val Z_vertical: Float = event.values[2]
                if(isGraPassed){
                    tv_gravity.text = "Gravity : pass"
                    tv_gravity.setTextColor(resources.getColor(R.color.green))
                    checkPassed()
                    return
                }
                tv_gravity.text = "Gravity [ X:$X_lateral  \nY:$Y_longitudinal Z:$Z_vertical ]"
                if(isGraFirst){
                    isGraFirst = false
                    graFirstX = X_lateral
                    graFirstY = Y_longitudinal
                    graFirstZ = Z_vertical
                }else{

                    if(kotlin.math.abs(X_lateral - graFirstX) > 3){
                        isGraX = true
                    }
                    if(kotlin.math.abs(Y_longitudinal - graFirstY) > 3){
                        isGraY = true
                    }
                    if(kotlin.math.abs(Z_vertical - graFirstZ) > 3){
                        isGraZ = true
                    }
                    isGraPassed =  isGraX&&isGraY&&isGraZ
                    XLog.d("isGraPassed:$isGraPassed")
                }

            }

            Sensor.TYPE_ACCELEROMETER->{
                XLog.d("TYPE_ACCELEROMETER")
                val X_lateral: Float = event.values[0]
                val Y_longitudinal: Float = event.values[1]
                val Z_vertical: Float = event.values[2]
                if(isAccPassed){
                    tv_accelertometer.text = "Accelerometer : pass"
                    tv_accelertometer.setTextColor(resources.getColor(R.color.green))
                    checkPassed()
                    return
                }
                tv_accelertometer.text = "Accelerometer [ X:$X_lateral  \nY:$Y_longitudinal Z:$Z_vertical ]"

                if(isAccFirst){
                    isAccFirst = false
                    accFirstX = X_lateral
                    accFirstY = Y_longitudinal
                    accFirstZ = Z_vertical
                }else{

                    if(kotlin.math.abs(X_lateral - accFirstX) > 3){
                        isAccX = true
                    }
                    if(kotlin.math.abs(Y_longitudinal - accFirstY) > 3){
                        isAccY = true
                    }
                    if(kotlin.math.abs(Z_vertical - accFirstZ) > 3){
                        isAccZ = true
                    }
                    isAccPassed =  isAccX&&isAccY&&isAccZ
                    XLog.d("isAccPassed:$isAccPassed")
                }

            }
            Sensor.TYPE_LIGHT->{
                XLog.d("TYPE_LIGHT")
                val light: Float = event.values[0]
                if(isLightPassed){
                    tv_Light.text = "Light : pass"
                    tv_Light.setTextColor(resources.getColor(R.color.green))
                    checkPassed()
                    return
                }

                tv_Light.text = "Light :[$light]"

                if(isLightFirst){
                    isLightFirst = false
                    ligFirstX = light

                }else{

                    if(kotlin.math.abs(light - ligFirstX) > 100){
                        isLightPassed = true
                    }
                    XLog.d("isLightPassed:$isLightPassed")
                }


            }

      }

    }

    @Synchronized
    private fun checkPassed() {

        if(isGraPassed && isGyrPassed && isAccPassed && isLightPassed && isScrPassed){
            EventBus.getDefault().post(TestResultBean(ResultCode.ACCELEROMETER_POSITION,PASSED))
            ToastUtils.showShort("通过")
            finish()
        }

    }

    private var  mRenderer: MyRenderer?=null
    override fun onResume() {
        super.onResume()
        getAllSensor()
        mRenderer!!.start();
        view_GLSurfaceView!!.onResume();
    }


    override fun onPause() {
        super.onPause()
        mRenderer!!.stop()
        view_GLSurfaceView!!.onPause()
        sensorManager!!.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()

    }


    inner class MyRenderer(
        context: Context,
        sensorManager: SensorManager
    ) : GLSurfaceView.Renderer, SensorEventListener {

        private val mCube: Cube
        private var mContext: Context = context
        private var sensorManager : SensorManager = sensorManager
        private val mRotationVectorSensor: Sensor = sensorManager.getDefaultSensor(
            Sensor.TYPE_ROTATION_VECTOR
        )
        private val mRotationMatrix = FloatArray(16)

        fun start() {

            sensorManager.registerListener(this, mRotationVectorSensor, 10000)
        }

        fun stop() {
            sensorManager.unregisterListener(this)
        }

        override fun onSensorChanged(event: SensorEvent) {

            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {

                val X_lateral: Float = event.values[0]
                val Y_longitudinal: Float = event.values[1]
                val Z_vertical: Float = event.values[2]

                SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, event.values
                )
                if(isScrPassed){
                    tv_screenRotation.text = "ScreenRotation : pass"
                    tv_screenRotation.setTextColor(resources.getColor(R.color.green))
                    checkPassed()
                    return
                }
                tv_screenRotation.text = "ScreenRotation [ \nX:$X_lateral  \nY:$Y_longitudinal \nZ:$Z_vertical ]"
                if(isScrFirst){
                    isScrFirst = false
                    scrFirstX = X_lateral
                    scrFirstY = Y_longitudinal
                    scrFirstZ = Z_vertical
                }else{

                    if(kotlin.math.abs(X_lateral - scrFirstX) > 0.3){
                        isScrX = true
                    }
                    if(kotlin.math.abs(Y_longitudinal - scrFirstY) > 0.3){
                        isScrY = true
                    }
                    if(kotlin.math.abs(Z_vertical - scrFirstZ) > 0.3){
                        isScrZ = true
                    }
                    isScrPassed =  isScrX&&isScrY&&isScrZ
                    XLog.d("isScrPassed:$isScrPassed")
                }

            }
        }

        override fun onDrawFrame(gl: GL10) {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT)
            // set-up modelview matrix
            gl.glMatrixMode(GL10.GL_MODELVIEW)
            gl.glLoadIdentity()
            gl.glTranslatef(0f, 0f, -3.0f)
            gl.glMultMatrixf(mRotationMatrix, 0)
            // draw our object
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY)
            mCube.draw(gl)
        }

        override fun onSurfaceChanged(
            gl: GL10,
            width: Int,
            height: Int
        ) { // set view-port
            gl.glViewport(0, 0, width, height)
            // set projection matrix
            val ratio = width.toFloat() / height
            gl.glMatrixMode(GL10.GL_PROJECTION)
            gl.glLoadIdentity()
            gl.glFrustumf(-ratio, ratio, -1f, 1f, 1f, 10f)
        }

        override fun onSurfaceCreated(
            gl: GL10,
            config: EGLConfig?
        ) { // dither is enabled by default, we don't need it
            gl.glDisable(GL10.GL_DITHER)
            // clear screen in white
            gl.glClearColor(1f, 1f, 1f, 1f)
        }

        internal inner class Cube {
            // initialize our cube
            private val mVertexBuffer: FloatBuffer
            private val mColorBuffer: FloatBuffer
            private val mIndexBuffer: ByteBuffer
            fun draw(gl: GL10) {
                gl.glEnable(GL10.GL_CULL_FACE)
                gl.glFrontFace(GL10.GL_CW)
                gl.glShadeModel(GL10.GL_SMOOTH)
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer)
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer)
                gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, mIndexBuffer)
            }

            init {
                val vertices = floatArrayOf(
                    -0.8f,
                    -0.8f,
                    -0.8f,
                    0.8f,
                    -0.8f,
                    -0.8f,
                    0.8f,
                    0.8f,
                    -0.8f,
                    -0.8f,
                    0.8f,
                    -0.8f,
                    -0.8f,
                    -0.8f,
                    0.8f,
                    0.8f,
                    -0.8f,
                    0.8f,
                    0.8f,
                    0.8f,
                    0.8f,
                    -0.8f,
                    0.8f,
                    0.8f
                )
                val colors = floatArrayOf(
                    0f,
                    0f,
                    0f,
                    1f,
                    1f,
                    0f,
                    0f,
                    1f,
                    1f,
                    1f,
                    0f,
                    1f,
                    0f,
                    1f,
                    0f,
                    1f,
                    0f,
                    0f,
                    1f,
                    1f,
                    1f,
                    0f,
                    1f,
                    1f,
                    1f,
                    1f,
                    1f,
                    1f,
                    0f,
                    1f,
                    1f,
                    1f
                )
                val indices = byteArrayOf(
                    0, 4, 5, 0, 5, 1,
                    1, 5, 6, 1, 6, 2,
                    2, 6, 7, 2, 7, 3,
                    3, 7, 4, 3, 4, 0,
                    4, 7, 6, 4, 6, 5,
                    3, 0, 1, 3, 1, 2
                )
                val vbb: ByteBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
                vbb.order(ByteOrder.nativeOrder())
                mVertexBuffer = vbb.asFloatBuffer()
                mVertexBuffer.put(vertices)
                mVertexBuffer.position(0)
                val cbb: ByteBuffer = ByteBuffer.allocateDirect(colors.size * 4)
                cbb.order(ByteOrder.nativeOrder())
                mColorBuffer = cbb.asFloatBuffer()
                mColorBuffer.put(colors)
                mColorBuffer.position(0)
                mIndexBuffer = ByteBuffer.allocateDirect(indices.size)
                mIndexBuffer.put(indices)
                mIndexBuffer.position(0)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor,accuracy: Int) {

        }

        init {
            mCube = Cube()
            // initialize the rotation matrix to identity
            mRotationMatrix[0] = 0.6f
            mRotationMatrix[4] = 0.6f
            mRotationMatrix[8] = 0.6f
            mRotationMatrix[12] = 0.6f
        }
    }




    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context,AccelerometerActivity::class.java))
        }
    }



}