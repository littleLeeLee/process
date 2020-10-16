package com.kintex.check.other;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TestAdbClient {
    String json = "{\"action\":{\"name\":\"start\",\"deviceUDID\":\"RF8M2191XJV\",\"test_case_list\":[{\"caseId\":1,\"enable\":1,\"caseName\":\"SimReader\",\"visible\":1}, {\"caseId\":2,\"enable\":1,\"caseName\":\"NetWork\",\"visible\":1}, {\"caseId\":3,\"enable\":1,\"caseName\":\"WiFi\",\"visible\":0}, {\"caseId\":4,\"enable\":1,\"caseName\":\"GPS\",\"visible\":0}, {\"caseId\":5,\"enable\":1,\"caseName\":\"BlueTooth\",\"visible\":0}, {\"caseId\":6,\"enable\":1,\"caseName\":\"Gyroscope\",\"visible\":0}, {\"caseId\":7,\"enable\":1,\"caseName\":\"Barometer\",\"visible\":0}, {\"caseId\":8,\"enable\":1,\"caseName\":\"Proximity Sensor\",\"visible\":1}, {\"caseId\":9,\"enable\":1,\"caseName\":\"Accelerometer\",\"visible\":2}, {\"caseId\":11,\"enable\":1,\"caseName\":\"Loud Speaker\",\"visible\":1}, {\"caseId\":12,\"enable\":1,\"caseName\":\"Earpiece\",\"visible\":1}, {\"caseId\":13,\"enable\":1,\"caseName\":\"Mic ES\",\"visible\":1}, {\"caseId\":14,\"enable\":1,\"caseName\":\"Vid Mic ES\",\"visible\":1}, {\"caseId\":15,\"enable\":1,\"caseName\":\"Microphone\",\"visible\":1}, {\"caseId\":16,\"enable\":1,\"caseName\":\"Video Microphone\",\"visible\":1}, {\"caseId\":17,\"enable\":1,\"caseName\":\"Front Microphone\",\"visible\":1}, {\"caseId\":18,\"enable\":1,\"caseName\":\"Headset-Left\",\"visible\":0}, {\"caseId\":19,\"enable\":1,\"caseName\":\"Headset-Right\",\"visible\":0}, {\"caseId\":20,\"enable\":1,\"caseName\":\"Headset Port\",\"visible\":1}, {\"caseId\":21,\"enable\":1,\"caseName\":\"Lightning Headset\",\"visible\":1}, {\"caseId\":22,\"enable\":1,\"caseName\":\"True Depth Camera\",\"visible\":1}, {\"caseId\":23,\"enable\":1,\"caseName\":\"Front Camera\",\"visible\":1}, {\"caseId\":24,\"enable\":1,\"caseName\":\"Rear Camera\",\"visible\":1}, {\"caseId\":25,\"enable\":1,\"caseName\":\"Flash\",\"visible\":1}, {\"caseId\":26,\"enable\":1,\"caseName\":\"Wide Camera\",\"visible\":1}, {\"caseId\":27,\"enable\":1,\"caseName\":\"Telephoto Camera\",\"visible\":1}, {\"caseId\":28,\"enable\":1,\"caseName\":\"Fingerprint Sensor\",\"visible\":2}, {\"caseId\":29,\"enable\":1,\"caseName\":\"Face ID\",\"visible\":2}, {\"caseId\":30,\"enable\":1,\"caseName\":\"Power Button\",\"visible\":1}, {\"caseId\":31,\"enable\":1,\"caseName\":\"Home Button\",\"visible\":1}, {\"caseId\":32,\"enable\":1,\"caseName\":\"Volume Down Button\",\"visible\":1}, {\"caseId\":33,\"enable\":1,\"caseName\":\"Volume Up Button\",\"visible\":1}, {\"caseId\":34,\"enable\":1,\"caseName\":\"Flip Switch\",\"visible\":1}, {\"caseId\":35,\"enable\":1,\"caseName\":\"Back Button\",\"visible\":1}, {\"caseId\":36,\"enable\":1,\"caseName\":\"Menu Button\",\"visible\":1}, {\"caseId\":37,\"enable\":1,\"caseName\":\"LCD\",\"visible\":1}, {\"caseId\":38,\"enable\":1,\"caseName\":\"Touch Screen\",\"visible\":1}, {\"caseId\":39,\"enable\":1,\"caseName\":\"3D Touch\",\"visible\":1}, {\"caseId\":40,\"enable\":0,\"caseName\":\"Spen\",\"visible\":1}, {\"caseId\":41,\"enable\":0,\"caseName\":\"Spen Back Button\",\"visible\":1}, {\"caseId\":42,\"enable\":0,\"caseName\":\"Spen Hover\",\"visible\":1}, {\"caseId\":43,\"enable\":0,\"caseName\":\"Spen Menu Button\",\"visible\":1}, {\"caseId\":44,\"enable\":0,\"caseName\":\"Spen Remove\",\"visible\":1}, {\"caseId\":45,\"enable\":1,\"caseName\":\"Vibration\",\"visible\":1}, {\"caseId\":46,\"enable\":1,\"caseName\":\"Wire Charge\",\"visible\":0}, {\"caseId\":47,\"enable\":1,\"caseName\":\"Wireless Charge\",\"visible\":1}]}}";
    public static void main(String[] args) {
        new TestAdbClient().actionPerformed();

    }
    public void actionPerformed() {
        // TODO Auto-generated method stub
        Socket socket = null;
        PrintStream out = null;
        BufferedReader br = null;
        try {
            //adb forward tcp:8000 tcp:10086
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
                out.println(json);
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