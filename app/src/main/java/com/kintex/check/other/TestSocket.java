package com.kintex.check.other;

import android.util.Log;

import com.elvishew.xlog.XLog;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class TestSocket {

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(10086);
            System.out.println("start：");
            Socket incoming = serverSocket.accept();
            System.out.println("accept：");


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
