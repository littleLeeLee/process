package com.kintex.check.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.kintex.check.R
import com.kintex.check.bean.AdbBean
import com.kintex.check.other.androidService
import kotlinx.android.synthetic.main.activity_testadb.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread


class TestAdbActivity : BaseActivity() {

    private var serverThread :SocketServerThread?=null
    private var adbIntent : Intent ?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_testadb)
        adbIntent = Intent(this, androidService::class.java)
        startService(adbIntent)

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun readMessageFromAdb( adbBean: AdbBean){
        tv_adbResult.append("\n"+adbBean.resultMessage)
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

        //暴露给外部调用写入流的方法
        fun sendMsg(msg: String) {
            try {
                Thread{
                    out!!.write(msg.toByteArray(charset("UTF-8")))
                    out!!.flush()
                }.start()

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

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this);
        stopService(adbIntent)

    }

    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context,TestAdbActivity::class.java))
        }
    }

}