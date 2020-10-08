package com.kintex.check.other;

import android.text.TextUtils;

import com.elvishew.xlog.XLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class TestPcClient {
	
	 static Scanner scanner = new Scanner(System.in);
	
	public static void main(String[] args) {
        try {

            final Socket socket = new Socket("127.0.0.1", 8000);

            //发送消息
            final PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(),"utf-8")),
                    true);
            //接收app端信息
            final BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(),"utf-8"));


                    boolean isFirst = true;
                    String readMsg = "";

                        try{

                            String msg="";

                                msg = scanner.nextLine();
                                out.println(msg);
                                out.flush();
                                System.out.println("send" + msg);

                            readMsg = in.readLine();
                            if(readMsg != null) {
                                System.out.println("read:"+readMsg);
                            }else {
                                System.out.println("read: null");
                            }

                        }catch (Exception e){
                                System.out.println(e);
                        }





                socket.close();

        } catch (Exception ex) {
            System.out.println("error : "+ex.getMessage());
        }

    }



    public static void createSocket() {

        try {
            final Socket client = new Socket("127.0.0.1", 8000);

            //发送消息
            final PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(client.getOutputStream(),"utf-8")),
                    true);
            //接收app端信息
            final BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream(),"utf-8"));

/*            // 得到socket管道中的输出流--------------像手机端写数据
            final BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
            // 得到socket管道中的输人流--------------读取手机端的数据
            final BufferedInputStream in = new BufferedInputStream(client.getInputStream());*/

            new Thread(new Runnable() {

                @Override
                public void run() {
                    String readMsg = "";
                    boolean isFirst = true;
                    while(true) {
                        if(!client.isConnected()) {
                            break;
                        }
                        try {
                        readMsg = in.readLine();
                        System.out.println("readMsg");
                        if(readMsg!=null) {
                            // 将要返回的数据发送给pc
                            out.println((readMsg + "1"));
                            out.flush();

                        }

                        if(isFirst){
                            isFirst = false;
                            String s = scanner.nextLine();
                            out.println(s);
                            out.flush();
                        }

                        Thread.sleep(200);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    //一个读取输入流的方法
    public static String readMsgFromSocket(InputStream in) {
        String msg = "";
        byte[] tempbuffer = new byte[1024];
        try {
            int numReadedBytes = in.read(tempbuffer, 0, tempbuffer.length);
            if(numReadedBytes == -1){

                return  "";
            }
            msg = new String(tempbuffer, 0, numReadedBytes, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

}
