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

import com.kintex.check.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class AdbTestActivity extends Activity {
    private static WifiManager wifi;
    static TextView cmd;

    static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            cmd.append((String)msg.obj);
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


    private static class NetState{

        private String intToIp(int ip){
            return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
                    + ((ip >> 24) & 0xFF);
        }

        public String GetIPAddress(){
            String ServerIP = intToIp(wifi.getConnectionInfo().getIpAddress());
            return ServerIP;
        }
    }

    static class MySocketServer implements Runnable {
        private static final String TAG="MySocketServer";
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
                // establish server socket
                int connIndex = 0;
                ServerSocket serverSocket = new ServerSocket(serverListenPort);//, connectionMaxLength, InetAddress.getByName(serverIpString));
                Message.obtain(handler, 0, "- address:" + serverSocket.getLocalSocketAddress() + "\n").sendToTarget();


                while (true) {
                    Message.obtain(handler, 0,"- ServerSocket start listene\n").sendToTarget();
                    Socket incoming = serverSocket.accept();
                    Message.obtain(handler, 0, "- Connected a client!connIndex:" + connIndex + " RemoteSocketAddress:" + String.valueOf(incoming.getRemoteSocketAddress()) + "\n").sendToTarget();
                    Log.e(TAG, "Connected a client!connIndex:" + connIndex + " RemoteSocketAddress:" + String.valueOf(incoming.getRemoteSocketAddress()));
                    Thread connHandle = new Thread(new ConnectionHandle(mContext, incoming, connIndex));
                    connHandle.start();
                    connIndex++;
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static   Scanner in;
    static PrintStream out;
    static class ConnectionHandle implements Runnable {
        public static final String TAG = "ConnectionHandle:";
        public static final String DELIMITER = "##";
        public static final String CMD_GETDIR = "cmd::msg";

        private Context mContext;
        private Socket connectedSocket;
        private int connIndex;

        public ConnectionHandle(Context ctx, Socket incoming, int connIdx) {
            mContext = ctx;
            connectedSocket = incoming;
            connIndex = connIdx;
        }

        @Override
        public void run(){
            Message.obtain(handler, 0, "- run client thread to deal socket\n").sendToTarget();
            Log.e(TAG, "+run()");

            try {
                try {
                    InputStream inStream = connectedSocket.getInputStream();
                    OutputStream outStream = connectedSocket.getOutputStream();

                     in = new Scanner(inStream, "UTF8");
                     out = new PrintStream(outStream, true, "UTF8");
                    //PrintWriter out = new PrintWriter(outStream, true);

                    //InputStreamReader  reader  = new InputStreamReader(inStream, "UTF8");
                    //OutputStreamWriter writer = new OutputStreamWriter(outStream, "UTF8");

                    //String test = TAG + "abc陈123";
                    //writer.write(test);
                    //writer.flush();
                    out.println("客户端连接成功！");
                    out.flush();
                    boolean done = false;


                    while (!done && in.hasNext()) {
                        String token = in.next();
                        Log.e(TAG, token);
                        Message.obtain(handler, 0, "-第" +connIndex+ "次成功接收消息: " + token + "\n").sendToTarget();
                        out.println("客户端成功接收：" + token);
                        out.flush();
                        if (token.equals("exit")) {
                            done = true;
                        }
                    }

                    connectedSocket.close();

                    Thread.sleep(10);
                }
                finally {
                    //incoming.close();
                    //outStream.close();
                }
            }
            catch (IOException e) {
                Log.e(TAG, "IOException:" + e.getMessage());
                e.printStackTrace();
            }
            catch (InterruptedException e) {
                Log.e(TAG, "InterruptedException:" + e.getMessage());
                e.printStackTrace();
            }
            catch (Exception e) {
                Log.e(TAG, "Exception:" + e.getClass().getName() + " msg:" + e.getMessage());
            }
            Log.e(TAG, "-run()");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }
}