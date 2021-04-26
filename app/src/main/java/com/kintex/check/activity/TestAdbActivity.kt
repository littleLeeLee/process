package com.kintex.check.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ScrollView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import com.kintex.check.R
import com.kintex.check.bean.NewTestPlanBean
import com.kintex.check.bean.ReceiveAdbBean
import kotlinx.android.synthetic.main.activity_testadb.*
import org.greenrobot.eventbus.EventBus
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.*


class TestAdbActivity : BaseActivity() {

    private var serverThread :SocketServerThread?=null
    private var adbIntent : Intent ?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  EventBus.getDefault().register(this)
        setContentView(R.layout.activity_testadb)

        startAdbService()
        btnSend.setOnClickListener {
            if(TextUtils.isEmpty(et_input.text.toString())){
                setText("不能为空")
                ToastUtils.showShort("不能为空")
            }else{

                sendMsg(et_input.text.toString())
            }


        }
/*
        findViewById<View>(R.id.btnStart).setOnClickListener {
            androidServer = AndroidServer()
            androidServer!!.start()

            *//*serverThread = SocketServerThread()
            serverThread!!.start()*//*

        }

        findViewById<View>(R.id.btnStop).setOnClickListener { androidServer!!.setStop(true)
        }
        findViewById<View>(R.id.btnSend).setOnClickListener {
            if(androidServer == null || androidServer!!.writer == null){

            ToastUtils.showShort("null")
        }
            thread {
                androidServer?.writer?.println("test send")
                androidServer?.writer?.flush()
                XLog.d("test send")
            }
         //   serverThread!!.sendMsg("send test")

        }*/

    }

    //暴露给外部调用写入流的方法
    fun sendMsg(msg: String) {
        try {
            Thread{
                outStream!!.write(msg.toByteArray(charset("UTF-8")))
                outStream!!.flush()
            }.start()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private var outStream: OutputStream? = null
    private var serverSocket:ServerSocket?=null
    private var connected:Socket?=null
    val BUFFER_SIZE = 1024 * 10
    var isRunning = true
    var content : StringBuffer= StringBuffer()
    inner class MySocketServer : Runnable {
        private val serverListenPort = 10086

        override fun run() {
            try {
                setText("startListen : 10086")
                XLog.d("startListen()")
                serverSocket =
                        ServerSocket(serverListenPort)
                serverSocket!!.receiveBufferSize = BUFFER_SIZE
                XLog.d("address:${serverSocket!!.localSocketAddress}")

                while (isRunning) {
                    connected = serverSocket?.accept()
                    XLog.d("accept")
                    setText("accept")
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
                    setText("客户端连接成功")
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
                        setText(content)
                      //  processingData(content)
                    }
                    connectedSocket.close()
                    XLog.d("连接断开")
                    setText("连接断开")
                    Thread.sleep(10)
                } catch (e: Exception) {
                    XLog.e("IOException:" + e.message)
                    e.printStackTrace()
                }
                //连接断开
                XLog.d("-run() finish")
                setText("连接已断开")
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
                                gson.fromJson<NewTestPlanBean>(content, NewTestPlanBean::class.java)
                        if (receiveAdbBean != null) {

                            val action = receiveAdbBean.action
                            when (action.name) {

                                "negotiation" -> {
                                    XLog.d("negotiation")
                                    //保存UID
                                  /*  SPUtils.getInstance().put("UUID", action.udid)
                                    var adb= ReceiveAdbBean()
                                    adb.name = action.name
                                    adb.test_case_list = receiveAdbBean
                                    EventBus.getDefault().post(adb)*/
                                    setText("negotiation")
                                    ToastUtils.showShort("negotiation success uuid is ${action.udid}")
                                }

                                "start" -> {
                                    setText("start")
                                    ToastUtils.showShort("Test Start")
                                }

                                "stop" -> {
                                    setText("stop")
                                    ToastUtils.showShort("Test Stop")
                                }


                            }

                        } else {
                            XLog.d("parse json = null ")
                            setText("parse json = null ")
                        }


                    } catch (e: java.lang.Exception) {
                        XLog.d("parse json $e")
                        setText("parse json $e")
                    }

                } else {
                    setText("空消息？逗我吗？")
                    XLog.d("空消息？逗我吗？")
                }


            }

        }
    }

    private fun setText(s: String) {
        runOnUiThread {
            content.append( "\n" + s)
            XLog.d(content.toString())
            tv_adbResult.text = content.toString()
            tv_adbResult.postDelayed(Runnable {
                srview.fullScroll(ScrollView.FOCUS_DOWN)
            },500 )

        }


    }

    private fun startAdbService() {
        socketThread = Thread(MySocketServer())
        socketThread!!.start()

    }


     class AndroidServer : Thread() {
        private var stop = false
         var reader : BufferedReader? =null
         var writer : PrintWriter? =null
        fun setStop(stop: Boolean) {
            this.stop = stop
        }


        override fun run() {
            var serverSocket: ServerSocket? = null
            try {
                serverSocket = ServerSocket(11111)
                while (!stop) {
                    Log.d("USB", "run $stop")

                        ToastUtils.showShort("start")

                    val socket = serverSocket.accept()
                    Log.d("USB", "msg: accept")
                    //读取服务端信息
                    if(reader == null){
                        reader = BufferedReader(
                            InputStreamReader(socket.getInputStream(), "utf-8")
                        )
                    }

                    //发给服务端信息

                    if(writer == null){
                        writer = PrintWriter(
                            BufferedWriter(
                                OutputStreamWriter(socket.getOutputStream())
                            ), true
                        )
                    }
                    val msg = reader!!.readLine()
                    Log.d("USB", "read msg: $msg")
                    ToastUtils.showShort("" + msg)
                    writer!!.println("我收到啦")
                    writer!!.flush()
                    socket.close()
                //    sleep(500)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (serverSocket != null) {
                    try {
                        serverSocket.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }


     class SocketServerThread : Thread() {
        private var out: BufferedOutputStream? = null
        private var client: Socket? = null
        override fun run() {
            try {
                Log.e("wsy", "等待连接")

                val serverSocket = ServerSocket(10010)
                while (true) {
                    client = serverSocket.accept()
                    XLog.d("accept")
                    out = BufferedOutputStream(client!!.getOutputStream())
                    // 开启子线程去读去数据
                    Thread(SocketReadThread(BufferedInputStream(client!!.getInputStream())))
                        .start()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        internal inner class SocketReadThread(private val input: BufferedInputStream) :
            Runnable {
            override fun run() {
                try {
                    val readMsg = ""
                    while (true) {
                        try {
                            if (!client!!.isConnected) {
                                break
                            }
                            //   读到后台发送的消息  然后去处理
                            val readMsg = readMsgFromSocket(input)
                            XLog.d("readMsg :$readMsg")
                            //    处理读到的消息(主要是身份证信息),然后保存在sp中;
                            if (readMsg.isEmpty()) {
                                break
                            }
                            //  将要返回的数据发送给 pc
                            out!!.write((readMsg + "flag").toByteArray())
                            out!!.flush()
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }

                    }
                    input.close()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

            //读取PC端发送过来的数据
            private fun readMsgFromSocket(input: InputStream): String {
                var msg = ""
                val temp = ByteArray(1024)
                try {
                    val readedBytes = input.read(temp, 0, temp.size)
                    msg = String(temp, 0, readedBytes)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                return msg
            }

        }
    }

    private var socketThread: Thread? = null
    override fun onDestroy() {
        super.onDestroy()
      //  EventBus.getDefault().unregister(this);
       try {
            isRunning = false
            socketThread = null
            connected?.close()
            serverSocket?.close()
        } catch (e: java.lang.Exception) {
            XLog.d(e)
        }
    }

    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context,TestAdbActivity::class.java))
        }
    }

}