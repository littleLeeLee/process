package com.kintex.check.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.text.TextUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import com.kintex.check.bean.ActionCase
import com.kintex.check.bean.ReceiveAdbBean
import com.kintex.check.bean.SendAction
import com.kintex.check.bean.TestResultBean
import com.kintex.check.utils.ResultCode.START
import com.kintex.check.utils.ResultCode.STOP
import org.greenrobot.eventbus.EventBus
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class AdbConnectService : Service() {

    val BUFFER_SIZE = 1024 * 10
    var isRunning = true
    override fun onCreate() {
        super.onCreate()
        startAdbService()
    }

    private var socketThread: Thread? = null
    private fun startAdbService() {
        socketThread = Thread(MySocketServer())
        socketThread!!.start()

    }

    private var myBinder: AdbBinder? = null

    override fun onBind(intent: Intent?): IBinder? {
        myBinder = AdbBinder()

        return myBinder
    }

    inner class AdbBinder : Binder() {

        fun testStart() {

        }

        fun testFinish(description: String) {

            Thread(Runnable {
                if(!TextUtils.isEmpty(description)){
                    outStream?.write(description.toByteArray())
                    outStream?.flush()
                }
            }).start()


        }

        fun testProcess(toJson: String) {

            Thread(Runnable {
                if(!TextUtils.isEmpty(toJson)){
                    outStream?.write(toJson.toByteArray())
                    outStream?.flush()
                }
            }).start()
        }

    }

    private var outStream: OutputStream? = null
    private var serverSocket:ServerSocket?=null
    private var connected:Socket?=null
    inner class MySocketServer : Runnable {
        private val serverListenPort = 10086

        override fun run() {
            try {
                XLog.d("startListen()")
               serverSocket =
                    ServerSocket(serverListenPort)
                serverSocket!!.receiveBufferSize = BUFFER_SIZE
                XLog.d("address:${serverSocket!!.localSocketAddress}")

                while (isRunning) {
                    connected = serverSocket?.accept()
                    XLog.d("accept")

                    val connHandle = Thread(ConnectionHandle(connected!!))
                    connHandle.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        inner class ConnectionHandle(
            private val connectedSocket: Socket
        ) :
            Runnable {
            override fun run() {

                XLog.d("Connection thread run()")
                try {
                    val inStream = connectedSocket.getInputStream()
                    outStream = connectedSocket.getOutputStream()
                    //          Scanner in = new Scanner(inStream);
                    outStream?.write("客户端连接成功".toByteArray())
                    outStream?.flush()

                    val bytes = ByteArray(BUFFER_SIZE)
                    var temp = 0
                    //read 方法会阻塞直到有新的消息过来
                    while (inStream.read(bytes).also { temp = it } != -1) {
                        val content = String(bytes, 0, temp)
                        Arrays.fill(bytes, 0.toByte())
                        XLog.d("length :$temp")
                        //处理数据
                        processingData(content)
                    }
                    connectedSocket.close()
                    XLog.d("连接断开")
                    Thread.sleep(10)
                } catch (e: Exception) {
                    XLog.e("IOException:" + e.message)
                    e.printStackTrace()
                }
                //连接断开
                XLog.d("-run() finish")
                ToastUtils.showShort("连接已断开")
            }


            //处理收到的数据
            private fun processingData(content: String) {
                println("content:$content")
                //告诉client 收到了什么消息
                if (!TextUtils.isEmpty(content)) {
                    val gson = Gson()
                    try {
                        val receiveAdbBean =
                            gson.fromJson<ReceiveAdbBean>(content, ReceiveAdbBean::class.java)
                        if (receiveAdbBean != null) {

                            val action = receiveAdbBean.action
                            when (action.name) {

                                "negotiation" -> {
                                    XLog.d("negotiation")
                                    //保存UID
                                    SPUtils.getInstance().put("UUID", action.udid)
                                    val sendAction = SendAction(ActionCase("negotiation", action.udid))
                                    sendDataToClient(sendAction)
                                    ToastUtils.showShort("negotiation success uuid is ${action.udid}")
                                }

                                "start" -> {

                                    XLog.d("start")
                                    var testResultBean = TestResultBean(START, 0, null)
                                    EventBus.getDefault().post(testResultBean)
                                    val sendAction = SendAction(ActionCase("test_started", action.udid))
                                    sendDataToClient(sendAction)
                                    ToastUtils.showShort("Test Start")
                                }

                                "stop" -> {
                                    XLog.d("stop")
                                    var testResultBean = TestResultBean(STOP, 0, null)
                                    EventBus.getDefault().post(testResultBean)
                                    val sendAction = SendAction(ActionCase("test_stopped", action.udid))
                                    sendDataToClient(sendAction)
                                    ToastUtils.showShort("Test Stop")
                                }


                            }

                        } else {
                            XLog.d("parse json = null ")
                        }


                    } catch (e: java.lang.Exception) {
                        XLog.d("parse json $e")
                    }

                } else {

                    XLog.d("空消息？逗我吗？")
                }


            }

        }
    }


    fun sendDataToClient(sendAction: SendAction) {
        val sendJson = Gson().toJson(sendAction)
        if (!TextUtils.isEmpty(sendJson)) {
            outStream!!.write(sendJson.toByteArray())
            outStream!!.flush()
        } else {
            XLog.d("send json is null")
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        XLog.d("onDestroy")
        try {
            isRunning = false
            socketThread = null
            connected?.close()
            serverSocket?.close()
        } catch (e: java.lang.Exception) {
            XLog.d(e)
        }

    }

    override fun onUnbind(intent: Intent?): Boolean {

        XLog.d("onUnbind")
        return super.onUnbind(intent)

    }
}