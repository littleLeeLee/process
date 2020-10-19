package com.kintex.check.other;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.kintex.check.R;
import com.kintex.check.bean.AdbBean;
import com.kintex.check.bean.KeyEventBean;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Scanner;

public class AdbTestActivity extends Activity {
    private static WifiManager wifi;
    static TextView cmd;

    static Handler handler = new Handler() {
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
        MySocketServer.startListen(this);
        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        out.println("哈哈哈");
                        out.flush();
                        XLog.d("send");
                    }
                }).start();

            }
        });
    }

    private void startSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                PrintStream out = null;
                BufferedReader br = null;
                try {
                    Socket socket = new Socket(InetAddress.getByName("192.168.0.240"), 10086);
                    out = new PrintStream(socket.getOutputStream(), true, "UTF8");
                    br = new BufferedReader(new InputStreamReader(
                            socket.getInputStream()));
                    out.println("客户端连接成功！");
                    out.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }


    private static class NetState {

        private String intToIp(int ip) {
            return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
                    + ((ip >> 24) & 0xFF);
        }

        public String GetIPAddress() {
            String ServerIP = intToIp(wifi.getConnectionInfo().getIpAddress());
            return ServerIP;
        }
    }

    static PrintStream out;

    static class MySocketServer implements Runnable {
        private static final String TAG = "MySocketServer";
        private static final int serverListenPort = 10086;

        private static Context mContext = null;

        public static void startListen(Context ctx) {
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
                 //   Message.obtain(handler, 0, "- Connected a client!connIndex:" + connIndex + " RemoteSocketAddress:" + String.valueOf(incoming.getRemoteSocketAddress()) + "\n").sendToTarget();
                //    XLog.d("Connected a client!connIndex:" + connIndex + " RemoteSocketAddress:" + String.valueOf(incoming.getRemoteSocketAddress()));
                    Thread connHandle = new Thread(new ConnectionHandle(mContext, incoming, connIndex));
                    connHandle.start();
                    connIndex++;
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }


        static class ConnectionHandle implements Runnable {
            public static final String TAG = "ConnectionHandle:";


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
                    OutputStream outStream = connectedSocket.getOutputStream();
                    Scanner in = new Scanner(inStream, "UTF8");
                    byte[] readBuffer = new byte[1024 * 1024 * 10];
                    int read = inStream.read(readBuffer);
                    if(read != -1){
                        String s = new String(readBuffer, Charset.forName("utf-8"));
                        Message.obtain(handler, 0, "-第" +connIndex+ "次成功接收消息: " + s + "\n").sendToTarget();
                    }else {
                        XLog.d("-11111");
                    }
                    out = new PrintStream(outStream, true, "UTF8");
                    out.println("客户端连接成功！");
                    out.flush();
                    boolean done = false;
                    while (!done  ) {
                        /*String token = in.next();
                        stringBuffer.append(token);
                        XLog.d("token" + token);
                        Message.obtain(handler, 0, "-第" +connIndex+ "次成功接收消息: " + token + "\n").sendToTarget();
                        out.println("客户端成功接收：" + token);
                        out.flush();
                        if (token.equals("exit")) {
                            done = true;
                        }*/
                        /*readBuffer = new byte[1024 * 1024 * 10];
                        read = inStream.read(readBuffer);
                        if(read != -1){
                            String s = new String(readBuffer, Charset.forName("utf-8"));
                            Message.obtain(handler, 0, "-第" +connIndex+ "次成功接收消息: " + s + "\n").sendToTarget();
                        }*/
                    }

                   /* String toString = readFromSocket(inStream);
                    XLog.d("stringBuffer:" + toString);*/
                 //   Message.obtain(handler, 0, "-第" + connIndex + "次成功接收消息: " + toString + "\n").sendToTarget();
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
    public static String readFromSocket(InputStream in) {
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
