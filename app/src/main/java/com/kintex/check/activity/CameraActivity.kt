package com.kintex.check.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.TestCase
import com.kintex.check.utils.CaseId
import com.kintex.check.utils.ResultCode
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.PASSED
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener
import com.kongzue.dialog.util.BaseDialog
import com.kongzue.dialog.v3.MessageDialog
import kotlinx.android.synthetic.main.activity_camera.*


class CameraActivity : BaseActivity() {


    private val cameraManager: CameraManager by lazy { getSystemService(CameraManager::class.java) }
    private var cameraStateCallback: CameraStateCallback? = null
    private var backGroundThread: HandlerThread? = null


    private var handler: Handler? = null

    private var resultCaseList = arrayListOf<TestCase>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        setView()
        initData()
    }

    private fun initData() {
        cameraStateCallback = CameraStateCallback()
        backGroundThread = HandlerThread("CameraThread")
        backGroundThread!!.start()
        handler = Handler(backGroundThread!!.looper)
    }

    override fun onResume() {
        super.onResume()
        if (cameraTextureView.isAvailable) {
            XLog.d("isAvailable")
            if (getCameraSupport(opendId)) {
                openCamera(opendId)
            } else {
                XLog.d("相机不支持API2")
            }
        }

    }


    private fun setView() {

   /*     tv_titleReset.text = "Back"
        tv_titleName.text = "Camera Test"
        tv_titleReset.setOnClickListener {
            finish()
        }
        tv_titleDone.setOnClickListener {
           finish()
        }*/

        tv_cameraPassed.setOnClickListener {

            if(opendId == "0"){
                sendCaseResult(CaseId.FrontCamera.id, PASSED, ResultCode.MANUAL)
                closeCamera()
                openCamera("1")
            }else if(opendId == "1"){
                sendCaseResult(CaseId.RearCamera.id, PASSED, ResultCode.MANUAL)
              //  closeCamera()
              //  openCamera(cameraList[index].cameraId)
                showFlashDialog()
            }

        }

        tv_cameraFailed.setOnClickListener {
            if(opendId == "0"){
                sendCaseResult(CaseId.FrontCamera.id, FAILED, ResultCode.MANUAL)
                closeCamera()
                openCamera("1")
            }else if(opendId == "1"){
                sendCaseResult(CaseId.RearCamera.id, FAILED, ResultCode.MANUAL)
                //  closeCamera()
                //  openCamera(cameraList[index].cameraId)
                showFlashDialog()
            }

        }

        cameraTextureView.surfaceTextureListener = TextureListener()

  /*      val cameraIdList = cameraManager.cameraIdList
        var deviceIdList = ArrayList<String>()
        deviceIdList.addAll(cameraIdList)

        //检查哪些摄像头不能使用
        for (device in deviceIdList) {

            val previewSize = getPreviewSize(device)
            if (null == previewSize) {
                deviceIdList.remove(device)
            } else {
                val cameraCharacteristics = cameraManager!!.getCameraCharacteristics(device)
                val facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                when(facing){

                    CameraCharacteristics.LENS_FACING_FRONT -> { // 前置摄像
                        resultCaseList.add(TestCase("Camera", 24, "Front Camera:${device}", "", 1, 0))

                    }
                    CameraCharacteristics.LENS_FACING_BACK -> { // 后置摄像头
                        resultCaseList.add(TestCase("Camera", 24, "Rear Camera:${device}", "", 1, 0))

                    }
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> { // 外置摄像头
                        resultCaseList.add(TestCase("Camera", 24, "Extra Camera:${device}", "", 1, 0))

                    }

                }

            }

        }*/


    }


    private fun showFlashDialog() {
        val dialog = MessageDialog.build(this)
            .setTitle("提示")
            .setMessage("请确认手机闪光灯是否亮起")
        dialog.setOkButton("是", object : OnDialogButtonClickListener {
            override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                dialog!!.doDismiss()
                sendCaseResult(CaseId.Flash.id, PASSED, ResultCode.MANUAL)
                finish()
                return false
            }
        })
        dialog.setCancelButton("否", object : OnDialogButtonClickListener {
            override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                sendCaseResult(CaseId.Flash.id, FAILED, ResultCode.MANUAL)
                finish()
                return false
            }
        })

        dialog.cancelable = false
        dialog.show()
    }

    private var previewTexture: SurfaceTexture? = null

    inner class TextureListener : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture?,
            width: Int,
            height: Int
        ) {


        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {


        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {

            return false
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            XLog.d("onSurfaceTextureAvailable")
            previewTexture = surface
            if (getCameraSupport("0")) {


                previeSize = Size(width, width * 16/9)
                XLog.d(previeSize)
                //surface 可见的时候打开相机 按照surface 的宽度显示预览
                openCamera("0")
            } else {
                ToastUtils.showShort("不支持API2")
            }
        }

    }

    private fun closeCamera() {
        XLog.d("closeCamera")
        try {
            if (openedCamera != null) {
                openedCamera!!.close()
                openedCamera = null
            }
        } catch (e: Exception) {
            XLog.d(e)
        }

    }


    private fun openCamera(cameraId: String) {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ToastUtils.showShort("请检查相机权限")
            finish()
            return
        }
        cameraManager.openCamera(cameraId, cameraStateCallback!!, handler)

    }

    private var openedCamera: CameraDevice? = null
    private var previeSize: Size? = null
    private var opendId = "0"

    inner class CameraStateCallback : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            XLog.d("opened")
            openedCamera = camera
            opendId = camera.id
            //获取目标摄像头可支持的预览分辨率
            getPreviewSize(camera.id)
            createPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            XLog.d("onDisconnected")
        }

        override fun onError(camera: CameraDevice, error: Int) {
            XLog.d("onError:$error")
        }

    }

    private var previewRequest: CaptureRequest? = null
    private var createCaptureRequest: CaptureRequest.Builder? = null

    //创建一个capture session  本质上camera2 的预览和操作都是capture  只不过预览是重复拍照
    private fun createPreviewSession() {
        //  getPreviewSize()
        val optimalSize = getOptimalSize(previeSize!!.width, previeSize!!.height)
        if (optimalSize == null) {
            previewTexture!!.setDefaultBufferSize(previeSize!!.width, previeSize!!.height)
        } else {
            previewTexture!!.setDefaultBufferSize(optimalSize!!.width, optimalSize!!.height)
        }

        val surface = Surface(previewTexture)
        val listOf = listOf(surface)
        openedCamera!!.createCaptureSession(listOf, object : CameraCaptureSession.StateCallback() {
            override fun onConfigureFailed(session: CameraCaptureSession) {

                XLog.d("onConfigureFailed${session.toString()}")
            }

            override fun onConfigured(session: CameraCaptureSession) {
                XLog.d("onConfigured")
                session.setRepeatingRequest(
                    previewRequest!!,
                    object : CameraCaptureSession.CaptureCallback() {
                    },
                    handler
                )
            }

            override fun onClosed(session: CameraCaptureSession) {
                super.onClosed(session)
                XLog.d("onClosed")
            }
        }, handler)



        createCaptureRequest = openedCamera!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        //获取最大支持的缩放倍率
        val cameraCharacteristics = cameraManager.getCameraCharacteristics(openedCamera!!.id)
        val maxZoom =
            cameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
        XLog.d("maxZoom:$maxZoom")


        //控制闪光灯
        //   if(isFlashOpen){
        createCaptureRequest!!.set(
            CaptureRequest.FLASH_MODE,
            CaptureRequest.FLASH_MODE_TORCH
        )
        /* }else{
             createCaptureRequest!!.set(CaptureRequest.FLASH_MODE,
                 CaptureRequest.FLASH_MODE_OFF)
         }*/
        //add 的surface  必须是创建session 时传入的surface
        createCaptureRequest!!.addTarget(surface)
        //创建一个build 实例
        previewRequest = createCaptureRequest!!.build()

    }

    //获取预览尺寸
    private fun getPreviewSize(id: String): Array<out Size>? {

        val cameraCharacteristics = cameraManager.getCameraCharacteristics(id)

        val supportMap =
            cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val outputSizes = supportMap!!.getOutputSizes(SurfaceTexture::class.java)

        if (outputSizes != null) {

            return outputSizes
        } else {

            XLog.d("id :$id camera not has outputSizes")
            return null
        }

    }


    private fun getOptimalSize(maxWidth: Int, maxHeight: Int): Size? {
        val cameraCharacteristics = cameraManager.getCameraCharacteristics(openedCamera!!.id)
        val aspectRatio = maxWidth.toFloat() / maxHeight
        val streamConfigurationMap =
            cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val supportedSizes = streamConfigurationMap?.getOutputSizes(SurfaceTexture::class.java)
        if (supportedSizes != null) {
            for (size in supportedSizes) {
                if (size.width.toFloat() / size.height == aspectRatio && size.height <= maxHeight && size.width <= maxWidth) {
                    return size
                }
            }
        }
/*        val asList = supportedSizes!!.asList()
        Collections.sort(asList,object :Comparator<Size>{
            override fun compare(o1: Size?, o2: Size?): Int {

                return o1!!.getWidth() * o1.getHeight() - o2!!.getWidth() * o2.getHeight()
            }

        })*/
        return null
    }


    private fun isHardwareLevelSupported(requireLevel: Int): Boolean {

        val sortedLevels = intArrayOf(
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY,
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED,
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL,
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3,
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL
        )

        return requireLevel in sortedLevels

    }

    //获取摄像头的信息
    private fun getCameraSupport(cameraId: String): Boolean {

        val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
        val level = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
        XLog.d("level:$level")
        return isHardwareLevelSupported(level!!)
    }


    override fun onDestroy() {
        super.onDestroy()
            testNext()
    }
    companion object {
        fun start(context: Context) {
            XLog.d("start")
            context.startActivity(Intent(context, CameraActivity::class.java))
        }
    }

    override fun onPause() {
        super.onPause()
        //     try {
        if (openedCamera != null) {
            openedCamera!!.close()
            openedCamera = null
        }

        if (backGroundThread != null) {
            backGroundThread!!.quitSafely()
            backGroundThread!!.join()
            backGroundThread = null
            handler = null
        }
        //  }catch (e:Exception){
        //       XLog.d(e)
        //    }

    }


}