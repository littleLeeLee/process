package com.kintex.check.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
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
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.adapter.CameraAdapter
import com.kintex.check.bean.CameraBean
import com.kintex.check.bean.TestCase
import com.kintex.check.bean.TestResultBean
import com.kintex.check.utils.ResultCode.FAILED
import com.kintex.check.utils.ResultCode.CAM_POSITION
import com.kintex.check.utils.ResultCode.PASSED
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener
import com.kongzue.dialog.util.BaseDialog
import com.kongzue.dialog.v3.MessageDialog
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.title_include.*


class CameraActivity : BaseActivity() {


    private  val cameraManager : CameraManager by lazy { getSystemService(CameraManager::class.java)}
    private  var cameraStateCallback : CameraStateCallback?=null
    private var backGroundThread : HandlerThread?=null


    private var handler : Handler?=null

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
        handler =Handler(backGroundThread!!.looper)
    }

    override fun onResume() {
        super.onResume()
        if(cameraTextureView.isAvailable){
            XLog.d("isAvailable")
            if(getCameraSupport("0")){
                openCamera("0")
            }else{
                XLog.d("相机不支持API2")
            }
        }

    }


    private var cameraList = ArrayList<CameraBean>()
    private fun setView() {

        tv_titleReset.text = "Back"
        tv_titleName.text = "Camera Test"
        tv_titleReset.setOnClickListener {
            finish()
        }
        tv_titleDone.setOnClickListener {
            for (k in cameraList.indices){

                if(cameraList[k].cameraState == "Pass"){
                    resultCaseList[k].result =1
                }else{
                    resultCaseList[k].result =0
                }

            }
            sendResult(CAM_POSITION, FAILED,resultCaseList)

        }

        tv_cameraPassed.setOnClickListener {

            editState("Pass")

        }

        tv_cameraFailed.setOnClickListener {
            editState("Fail")

        }

        val cameraIdList = cameraManager.cameraIdList

        var deviceIdList = ArrayList<String>()
        deviceIdList.addAll(cameraIdList)

        //检查哪些摄像头不能使用
        for (device in deviceIdList) {

            val previewSize = getPreviewSize(device)
            if(null == previewSize){
                deviceIdList.remove(device)
            }else{
                resultCaseList.add(TestCase("Camera",24,"Camera:${device}","",1,0))
                cameraList.add(CameraBean(device,"UnKnown",""))
            }

        }
        //最后一个是闪光灯-1  闪光灯 改为全亮
/*        deviceIdList.add("Flash")
        cameraList.add(CameraBean("Flash","UnKnown",""))*/
        resultCaseList.add(TestCase("Camera",25,"Flash","",1,0))
        val cameraAdapter = CameraAdapter(this, deviceIdList,cameraManager)

        cameraRecyclerView.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)

        cameraAdapter.setOnItemClickListener(object : CameraAdapter.onItemClickListener {
            @SuppressLint("SetTextI18n")
            override fun onItemClick(view: View, position: Int) {
                //显示当前ID的结果
             //   tv_currentId.text = cameraList[position].cameraId + "："+cameraList[position].cameraState

              /*  if(position == deviceIdList.size-1){
                    isFlashPressed = true
                    //闪光灯
                    isFlashOpen = !isFlashOpen
                   var currid = openedCamera!!.id
                    closeCamera()
                    openCamera(currid)

                }else{*/
                /*    isFlashPressed = false
                    isFlashOpen = false
                    if(openedCamera!!.id != deviceIdList[position]){

                        closeCamera()
                        openCamera(deviceIdList[position])

                    }*/
            //    }


            }
        })
        cameraRecyclerView.adapter = cameraAdapter

        cameraTextureView.surfaceTextureListener = TextureListener()
        tv_currentId.text = "0：UnKnown"

    }

    private fun editState(state : String) {
        var stateCount = cameraList.size
        var passCount = 0

            for (cameraBean in cameraList) {

                if(cameraBean.cameraId == openedCamera!!.id){
                    cameraBean.cameraState = state
                }
                if(cameraBean.cameraState != "UnKnown"){
                    stateCount--
                }

                if(cameraBean.cameraState == "Pass"){
                    passCount++
                }
            }
            tv_currentId.text = openedCamera!!.id + "："+state


        if(stateCount ==0){
            if(passCount == cameraList.size){
                showFlashDialog()

            }else{
               // sendResult(CAM_POSITION, FAILED)
                XLog.d("这里应该不会走")
            }
        }


        var index = 0
        for (k in cameraList.indices){

            if(cameraList[k].cameraId == openedCamera!!.id){
                index = k
            }
        }

        if(index  != cameraList.size -1){
            index+=1
            closeCamera()
            openCamera(cameraList[index].cameraId)
            XLog.d("openCamera$index")
        }


    }

    private fun showFlashDialog() {
        val dialog = MessageDialog.build(this)
            .setTitle( "提示")
            .setMessage("请确认手机闪光灯是否亮起")
            .setOkButton("是",object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    dialog!!.dismiss()
                    for (testCase in resultCaseList) {
                        testCase.result = 1
                    }
                    sendResult(CAM_POSITION, PASSED,resultCaseList)
                    return false
                }
            })
            .setCancelButton("否",object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    dialog!!.dismiss()
                    resultCaseList[resultCaseList.size-1].result = 0
                    sendResult(CAM_POSITION, FAILED,resultCaseList)
                    return false
                }
            })

            dialog.cancelable = false
            dialog.show()
    }

    private var previewTexture : SurfaceTexture ?=null

    inner class TextureListener : TextureView.SurfaceTextureListener{
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
            if(getCameraSupport("0")){
                previeSize = Size(width, height)
                //surface 可见的时候打开相机 按照surface 的宽度显示预览
                openCamera("0")
            }else{
                ToastUtils.showShort("不支持API2")
            }
        }

    }

    private fun closeCamera(){
        XLog.d("closeCamera")
        try {
            if(openedCamera != null){
                openedCamera!!.close()
                openedCamera = null
            }
        }catch (e:Exception){
            XLog.d(e)
        }

    }


    private fun openCamera(cameraId: String){

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ToastUtils.showShort("请检查相机权限")
            finish()
            return
        }
        cameraManager.openCamera(cameraId,cameraStateCallback!!,handler)

    }

    private var openedCamera : CameraDevice?=null
    private var previeSize : Size?=null
    inner class CameraStateCallback : CameraDevice.StateCallback(){
        override fun onOpened(camera: CameraDevice) {
            XLog.d("opened")
            openedCamera = camera

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

    private var previewRequest: CaptureRequest ?= null
    private var createCaptureRequest: CaptureRequest.Builder?=null
    //创建一个capture session  本质上camera2 的预览和操作都是capture  只不过预览是重复拍照
    private fun createPreviewSession(){
      //  getPreviewSize()
        previewTexture!!.setDefaultBufferSize(previeSize!!.width,previeSize!!.height)
        val surface = Surface(previewTexture)
        val listOf = listOf(surface)
         openedCamera!!.createCaptureSession(listOf,object : CameraCaptureSession.StateCallback() {
            override fun onConfigureFailed(session: CameraCaptureSession) {

                XLog.d("onConfigureFailed${session.toString()}")
            }

            override fun onConfigured(session: CameraCaptureSession) {
                XLog.d("onConfigured")
                session.setRepeatingRequest(previewRequest!!,object : CameraCaptureSession.CaptureCallback() {
                }, handler)
            }

            override fun onClosed(session: CameraCaptureSession) {
                super.onClosed(session)
                XLog.d("onClosed")
            }
        },handler)



        createCaptureRequest = openedCamera!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        //获取最大支持的缩放倍率
        val cameraCharacteristics = cameraManager.getCameraCharacteristics(openedCamera!!.id)
        val maxZoom = cameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
        XLog.d("maxZoom:$maxZoom")


        //控制闪光灯
     //   if(isFlashOpen){
            createCaptureRequest!!.set(CaptureRequest.FLASH_MODE,
                CaptureRequest.FLASH_MODE_TORCH)
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
    private fun getPreviewSize(id:String) : Array<out Size>? {

        val cameraCharacteristics = cameraManager.getCameraCharacteristics(id)

        val supportMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val outputSizes = supportMap!!.getOutputSizes(SurfaceTexture::class.java)

        if(outputSizes != null){
           /* for (size in outputSizes) {

                XLog.d("id:$id size : $size")

            }*/

            return outputSizes
        }else {

            XLog.d("id :$id camera not has outputSizes")
            return null
        }




    }


    private fun isHardwareLevelSupported(requireLevel : Int) : Boolean{

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
    private fun getCameraSupport(cameraId: String) : Boolean{

            val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
            val level = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            XLog.d("level:$level")
            return isHardwareLevelSupported(level!!)
    }


    companion object{
        fun start(context: Context){
            XLog.d("start")
            context.startActivity(Intent(context,CameraActivity::class.java))
        }
    }

    override fun onPause() {
        super.onPause()
   //     try {
            if(openedCamera != null){
                openedCamera!!.close()
                openedCamera = null
            }

            if(backGroundThread!=null){
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