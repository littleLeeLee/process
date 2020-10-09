package com.kintex.check.other;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TestAdbClient {

    public static void main(String[] args) {
        new TestAdbClient().actionPerformed();
    }

    public void actionPerformed() {
        // TODO Auto-generated method stub
        Socket socket = null;
        PrintStream out = null;
        BufferedReader br = null;
        try {
            //"127.0.0.1", 8000
            socket = new Socket(InetAddress.getByName("127.0.0.1"), 8000);
            out = new PrintStream(socket.getOutputStream(), true, "UTF8");
            br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            String msg;
            Scanner scanner = new Scanner(System.in, "UTF-8");
            msg = br.readLine();
            System.out.println("客户端返回：" + msg);
            out.println("客户端连接成功！");
            out.flush();
            while(scanner.hasNext()){
                msg = scanner.nextLine();
                out.println(msg);
                out.flush();
                System.out.println("消息已发送：" + msg);
                if(msg.equals("exit")){
                    return;
                }
            }
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (br != null) {
                    br.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e3) {
                // TODO Auto-generated catch block
                e3.printStackTrace();
            }
        }

    }
}