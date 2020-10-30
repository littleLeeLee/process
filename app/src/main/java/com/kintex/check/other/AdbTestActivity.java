package com.kintex.check.other;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.kintex.check.R;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class AdbTestActivity extends Activity {
    private  WifiManager wifi;
     TextView cmd;

     Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            cmd.append((String) msg.obj);
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testadb);
        cmd = (TextView) findViewById(R.id.tv_adbResult);
        final EditText input = (EditText)findViewById(R.id.et_input);
        MySocketServer mySocketServer = new MySocketServer();
        mySocketServer.startListen(this);
        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String trim = input.getText().toString().trim();
                            outStream.write(trim.getBytes());
                            outStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        XLog.d("send");
                    }
                }).start();

            }
        });
    }
    OutputStream outStream;




     class MySocketServer implements Runnable {
        private  final String TAG = "MySocketServer";
        private  final int serverListenPort = 10086;

        private  Context mContext = null;

        public  void startListen(Context ctx) {
            Log.e(TAG, "+startListen()");
            cmd.append("- startListen\n");
            mContext = ctx;
            Thread serverSocketListen = new Thread(new MySocketServer());
            serverSocketListen.start();
            Log.e(TAG, "-startListen()");

        }


        @Override
        public void run() {
            try {

                int connIndex = 0;
                ServerSocket serverSocket = new ServerSocket(serverListenPort);//, connectionMaxLength, InetAddress.getByName(serverIpString));
                serverSocket.setReceiveBufferSize(1024*1024*10);
                Message.obtain(handler, 0, "- address:" + serverSocket.getLocalSocketAddress() + "\n").sendToTarget();

                while (true) {
                    Message.obtain(handler, 0, "- ServerSocket start listene\n").sendToTarget();
                    Socket incoming = serverSocket.accept();
                    XLog.d("accept");
                    Thread connHandle = new Thread(new ConnectionHandle(mContext, incoming, connIndex));
                    connHandle.start();
                    connIndex++;
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

         class ConnectionHandle implements Runnable {
            public  final String TAG = "ConnectionHandle:";


            private Context mContext;
            private Socket connectedSocket;
            private int connIndex;

            public ConnectionHandle(Context ctx, Socket incoming, int connIdx) {
                mContext = ctx;
                connectedSocket = incoming;
                connIndex = connIdx;
            }

            @Override
            public void run() {
                Message.obtain(handler, 0, "- run client thread to deal socket\n").sendToTarget();
                XLog.d(" thread run()");

                try {
                    StringBuffer stringBuffer = new StringBuffer();
                    InputStream inStream = connectedSocket.getInputStream();

                    outStream = connectedSocket.getOutputStream();
                    //          Scanner in = new Scanner(inStream);

                    outStream.write(("客户端连接成功" ).getBytes());
                    outStream.flush();
                  /*  out = new PrintStream(outStream, true, "UTF8");
                    out.println("客户端连接成功！");
                    out.flush();*/
                    byte[] bytes = new byte[1024 * 20];

                    int temp = 0;
                    int length =0;
                    //read 方法会阻塞直到有新的消息过来
                    while((temp = inStream.read(bytes))!= -1){
                        String s = new String(bytes);
                        XLog.d("s"+s);
                        XLog.d("length :" +temp);
                        Message.obtain(handler, 0, "-第" +connIndex+++ "次成功接收消息: " + s + "\n").sendToTarget();
                        Arrays.fill(bytes,(byte)0);
                        //告诉client 收到了什么消息
                        if(!TextUtils.isEmpty(s)){
                            outStream.write(("客户端成功接收：" + s).getBytes());
                            outStream.flush();
                        }

                    }
                    connectedSocket.close();

                    Thread.sleep(10);
                } catch (Exception e) {
                    XLog.e("IOException:" + e.getMessage());
                    e.printStackTrace();
                }
                XLog.d("-run() finish");
                Message.obtain(handler, 0, "连接已断开\n").sendToTarget();
            }
        }

    }


    /* 从InputStream流中读数据 */
    public  String readFromSocket(InputStream in) {
        try {

            int MAX_BUFFER_BYTES = 1024*1024*10;
            XLog.d("MAX_BUFFER_BYTES:"+MAX_BUFFER_BYTES);
            String msg = "";
            byte[] tempbuffer = new byte[MAX_BUFFER_BYTES];

            int numReadedBytes = in.read(tempbuffer, 0, tempbuffer.length);
            msg = new String(tempbuffer, 0, numReadedBytes, "utf-8");
            return msg;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
