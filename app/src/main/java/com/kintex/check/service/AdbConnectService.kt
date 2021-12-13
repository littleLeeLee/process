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
import com.kintex.check.bean.*
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
            XLog.d("send data:$description")
            Thread(Runnable {
                if(!TextUtils.isEmpty(description)){
                    if(serverSocket != null){
                        if(!serverSocket?.isClosed!!){
                            outStream?.write(description.toByteArray())
                            outStream?.flush()
                        }else{
                            ToastUtils.showShort("连接已断开")
                        }
                    }else{
                        ToastUtils.showShort("Socket Error")
                    }

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
                    ToastUtils.showShort("连接成功")
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

                    if(content.contains("negotiation")){
                    EventBus.getDefault().post(ReceiveAdbBean("negotiation",0,content))
                    }else if(content.contains("get_result")){
                        EventBus.getDefault().post(ReceiveAdbBean("get_result",0,content))
                    }else if(content.contains("send_case_result")){

                    }

                } else {

                    XLog.d("空消息？逗我吗？")
                }


            }

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