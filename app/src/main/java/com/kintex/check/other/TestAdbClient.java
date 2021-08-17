package com.kintex.check.other;

import android.text.TextUtils;

import com.xuhao.didi.core.iocore.interfaces.ISendable;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Scanner;

public class TestAdbClient {
String negotiation = "{\"action\":{\"name\":\"negotiation\",\"udid\":\"xxxx\",\"operations\":[{\"testTypeName\":\"AutomaticTesting\",\"types\":[{\"name\":\"Connection\",\"typeItems\":[{\"caseName\":\"SimReader\",\"caseId\":1,\"enable\":1,\"visible\":1},{\"caseName\":\"WiFi\",\"caseId\":3,\"enable\":1,\"visible\":0},{\"caseName\":\"GPS\",\"caseId\":4,\"enable\":1,\"visible\":0},{\"caseName\":\"BlueTooth\",\"caseId\":5,\"enable\":1,\"visible\":0}]},{\"name\":\"Sensor\",\"typeItems\":[{\"caseName\":\"Gyroscope\",\"caseId\":6,\"enable\":1,\"visible\":0},{\"caseName\":\"Barometer\",\"caseId\":7,\"enable\":1,\"visible\":0},{\"caseName\":\"Accelerometer\",\"caseId\":9,\"enable\":1,\"visible\":2},{\"caseName\":\"NFC\",\"caseId\":50,\"enable\":1,\"visible\":2},{\"caseName\":\"Magnetometer\",\"caseId\":53,\"enable\":1,\"visible\":2}]},{\"name\":\"Secure\",\"typeItems\":[{\"caseName\":\"Fingerprint Sensor\",\"caseId\":28,\"enable\":1,\"visible\":2},{\"caseName\":\"Face ID\",\"caseId\":29,\"enable\":1,\"visible\":2}]}]},{\"testTypeName\":\"ManualTest\",\"types\":[{\"name\":\"Screen Test\",\"typeItems\":[{\"caseName\":\"Display\",\"caseId\":37,\"enable\":1,\"visible\":1},{\"caseName\":\"Touch Panel\",\"caseId\":38,\"enable\":1,\"visible\":1},{\"caseName\":\"3D Touch\",\"caseId\":39,\"enable\":1,\"visible\":1},{\"caseName\":\"True Tone\",\"caseId\":51,\"enable\":1,\"visible\":1},{\"caseName\":\"Multi Touch\",\"caseId\":52,\"enable\":1,\"visible\":1},{\"caseName\":\"Proximity Sensor\",\"caseId\":8,\"enable\":1,\"visible\":1},{\"caseName\":\"LightSensor\",\"caseId\":54,\"enable\":1,\"visible\":1}]},{\"name\":\"Button\",\"typeItems\":[{\"caseName\":\"Power Button\",\"caseId\":30,\"enable\":1,\"visible\":1},{\"caseName\":\"Home Button\",\"caseId\":31,\"enable\":1,\"visible\":1},{\"caseName\":\"Volume Down Button\",\"caseId\":32,\"enable\":1,\"visible\":1},{\"caseName\":\"Volume Up Button\",\"caseId\":33,\"enable\":1,\"visible\":1},{\"caseName\":\"Silence Button\",\"caseId\":34,\"enable\":1,\"visible\":1},{\"caseName\":\"Vibration\",\"caseId\":45,\"enable\":1,\"visible\":1}]},{\"name\":\"Camera Test\",\"typeItems\":[{\"caseName\":\"True Depth Camera\",\"caseId\":22,\"enable\":1,\"visible\":1},{\"caseName\":\"Front Camera\",\"caseId\":23,\"enable\":1,\"visible\":1},{\"caseName\":\"Rear Camera\",\"caseId\":24,\"enable\":1,\"visible\":1},{\"caseName\":\"Flash\",\"caseId\":25,\"enable\":1,\"visible\":1},{\"caseName\":\"Wide Camera\",\"caseId\":26,\"enable\":1,\"visible\":1},{\"caseName\":\"Telephoto Camera\",\"caseId\":27,\"enable\":1,\"visible\":1}]},{\"name\":\"Audio Test\",\"typeItems\":[{\"caseName\":\"Loud Speaker\",\"caseId\":11,\"enable\":1,\"visible\":1},{\"caseName\":\"Ear Speaker\",\"caseId\":12,\"enable\":1,\"visible\":1},{\"caseName\":\"Bottom Microphone\",\"caseId\":15,\"enable\":1,\"visible\":1},{\"caseName\":\"Video Microphone\",\"caseId\":16,\"enable\":1,\"visible\":1},{\"caseName\":\"Front Microphone\",\"caseId\":17,\"enable\":1,\"visible\":1}]}]},{\"testTypeName\":\"SemiAutomatic\",\"types\":[{\"name\":\"Battery\",\"typeItems\":[{\"caseName\":\"USB Charge\",\"caseId\":46,\"enable\":1,\"visible\":0},{\"caseName\":\"Wireless Charge\",\"caseId\":47,\"enable\":1,\"visible\":1}]},{\"name\":\"Headset\",\"typeItems\":[{\"caseName\":\"Headset-Left\",\"caseId\":18,\"enable\":1,\"visible\":0},{\"caseName\":\"Headset-Right\",\"caseId\":19,\"enable\":1,\"visible\":0},{\"caseName\":\"Headset Port\",\"caseId\":20,\"enable\":1,\"visible\":1}]}]},{\"testTypeName\":\"ManualCheck\",\"types\":[{\"name\":\"Cosmetics\",\"typeItems\":[{\"caseName\":\"LCD Quality\",\"caseId\":1024,\"enable\":1,\"visible\":1},{\"caseName\":\"Frame Quality\",\"caseId\":1025,\"enable\":1,\"visible\":1},{\"caseName\":\"Back Quality\",\"caseId\":1026,\"enable\":1,\"visible\":0},{\"caseName\":\"FT Quality\",\"caseId\":1027,\"enable\":1,\"visible\":0}]},{\"name\":\"Notes\",\"typeItems\":[{\"caseName\":\"LCD gap\",\"caseId\":1028,\"enable\":1,\"visible\":1},{\"caseName\":\"Cut the hand\",\"caseId\":1029,\"enable\":1,\"visible\":1}]}]}]}}";
//String start = "{\"action\":{\"name\":\"start\",\"udid\":\"xxxx\",\"test_case_list\":[{\"caseName\":\"SimReader\",\"caseId\":1,\"enable\":1,\"visible\":1},{\"caseName\":\"NetWork\",\"caseId\":2,\"enable\":1,\"visible\":1},{\"caseName\":\"WiFi\",\"caseId\":3,\"enable\":1,\"visible\":0},{\"caseName\":\"GPS\",\"caseId\":4,\"enable\":1,\"visible\":0},{\"caseName\":\"BlueTooth\",\"caseId\":5,\"enable\":1,\"visible\":0},{\"caseName\":\"Gyroscope\",\"caseId\":6,\"enable\":1,\"visible\":0},{\"caseName\":\"Barometer\",\"caseId\":7,\"enable\":1,\"visible\":0},{\"caseName\":\"ProximitySensor\",\"caseId\":8,\"enable\":1,\"visible\":1},{\"caseName\":\"Accelerometer\",\"caseId\":9,\"enable\":1,\"visible\":2},{\"caseName\":\"LoudSpeaker\",\"caseId\":11,\"enable\":1,\"visible\":1},{\"caseName\":\"Earpiece\",\"caseId\":12,\"enable\":1,\"visible\":1},{\"caseName\":\"MicES\",\"caseId\":13,\"enable\":1,\"visible\":1},{\"caseName\":\"VidMicES\",\"caseId\":14,\"enable\":1,\"visible\":1},{\"caseName\":\"Microphone\",\"caseId\":15,\"enable\":1,\"visible\":1},{\"caseName\":\"VideoMicrophone\",\"caseId\":16,\"enable\":1,\"visible\":1},{\"caseName\":\"FrontMicrophone\",\"caseId\":17,\"enable\":1,\"visible\":1},{\"caseName\":\"Headset-Left\",\"caseId\":18,\"enable\":1,\"visible\":0},{\"caseName\":\"Headset-Right\",\"caseId\":19,\"enable\":1,\"visible\":0},{\"caseName\":\"HeadsetPort\",\"caseId\":20,\"enable\":1,\"visible\":1},{\"caseName\":\"LightningHeadset\",\"caseId\":21,\"enable\":1,\"visible\":1},{\"caseName\":\"TrueDepthCamera\",\"caseId\":22,\"enable\":1,\"visible\":1},{\"caseName\":\"FrontCamera\",\"caseId\":23,\"enable\":1,\"visible\":1},{\"caseName\":\"RearCamera\",\"caseId\":24,\"enable\":1,\"visible\":1},{\"caseName\":\"Flash\",\"caseId\":25,\"enable\":1,\"visible\":1},{\"caseName\":\"WideCamera\",\"caseId\":26,\"enable\":1,\"visible\":1},{\"caseName\":\"TelephotoCamera\",\"caseId\":27,\"enable\":1,\"visible\":1},{\"caseName\":\"FingerprintSensor\",\"caseId\":28,\"enable\":1,\"visible\":2},{\"caseName\":\"FaceID\",\"caseId\":29,\"enable\":1,\"visible\":2},{\"caseName\":\"PowerButton\",\"caseId\":30,\"enable\":1,\"visible\":1},{\"caseName\":\"HomeButton\",\"caseId\":31,\"enable\":1,\"visible\":1},{\"caseName\":\"VolumeDownButton\",\"caseId\":32,\"enable\":1,\"visible\":1},{\"caseName\":\"VolumeUpButton\",\"caseId\":33,\"enable\":1,\"visible\":1},{\"caseName\":\"FlipSwitch\",\"caseId\":34,\"enable\":1,\"visible\":1},{\"caseName\":\"BackButton\",\"caseId\":35,\"enable\":1,\"visible\":1},{\"caseName\":\"MenuButton\",\"caseId\":36,\"enable\":1,\"visible\":1},{\"caseName\":\"LCD\",\"caseId\":37,\"enable\":1,\"visible\":1},{\"caseName\":\"TouchScreen\",\"caseId\":38,\"enable\":1,\"visible\":1},{\"caseName\":\"3DTouch\",\"caseId\":39,\"enable\":1,\"visible\":1},{\"caseName\":\"Spen\",\"caseId\":40,\"enable\":0,\"visible\":1},{\"caseName\":\"SpenBackButton\",\"caseId\":41,\"enable\":0,\"visible\":1},{\"caseName\":\"SpenHover\",\"caseId\":42,\"enable\":0,\"visible\":1},{\"caseName\":\"SpenMenuButton\",\"caseId\":43,\"enable\":0,\"visible\":1},{\"caseName\":\"SpenRemove\",\"caseId\":44,\"enable\":0,\"visible\":1},{\"caseName\":\"Vibration\",\"caseId\":45,\"enable\":1,\"visible\":1},{\"caseName\":\"WireCharge\",\"caseId\":46,\"enable\":1,\"visible\":0},{\"caseName\":\"WirelessCharge\",\"caseId\":47,\"enable\":1,\"visible\":1}]}}";
//String start = "{\"action\":{\"name\":\"start\",\"udid\":\"RF8M2191XJV\",\"test_case_list\":\"[{\\\"caseId\\\":1,\\\"enable\\\":1,\\\"caseName\\\":\\\"SimReader\\\",\\\"visible\\\":1},{\\\"caseId\\\":2,\\\"enable\\\":1,\\\"caseName\\\":\\\"NetWork\\\",\\\"visible\\\":1},{\\\"caseId\\\":3,\\\"enable\\\":1,\\\"caseName\\\":\\\"WiFi\\\",\\\"visible\\\":0},{\\\"caseId\\\":4,\\\"enable\\\":1,\\\"caseName\\\":\\\"GPS\\\",\\\"visible\\\":0},{\\\"caseId\\\":5,\\\"enable\\\":1,\\\"caseName\\\":\\\"BlueTooth\\\",\\\"visible\\\":0},{\\\"caseId\\\":6,\\\"enable\\\":1,\\\"caseName\\\":\\\"Gyroscope\\\",\\\"visible\\\":0},{\\\"caseId\\\":7,\\\"enable\\\":1,\\\"caseName\\\":\\\"Barometer\\\",\\\"visible\\\":0},{\\\"caseId\\\":8,\\\"enable\\\":1,\\\"caseName\\\":\\\"Proximity Sensor\\\",\\\"visible\\\":1},{\\\"caseId\\\":9,\\\"enable\\\":1,\\\"caseName\\\":\\\"Accelerometer\\\",\\\"visible\\\":2},{\\\"caseId\\\":11,\\\"enable\\\":1,\\\"caseName\\\":\\\"Loud Speaker\\\",\\\"visible\\\":1},{\\\"caseId\\\":12,\\\"enable\\\":1,\\\"caseName\\\":\\\"Earpiece\\\",\\\"visible\\\":1},{\\\"caseId\\\":13,\\\"enable\\\":1,\\\"caseName\\\":\\\"Mic ES\\\",\\\"visible\\\":1},{\\\"caseId\\\":14,\\\"enable\\\":1,\\\"caseName\\\":\\\"Vid Mic ES\\\",\\\"visible\\\":1},{\\\"caseId\\\":15,\\\"enable\\\":1,\\\"caseName\\\":\\\"Microphone\\\",\\\"visible\\\":1},{\\\"caseId\\\":16,\\\"enable\\\":1,\\\"caseName\\\":\\\"Video Microphone\\\",\\\"visible\\\":1},{\\\"caseId\\\":17,\\\"enable\\\":1,\\\"caseName\\\":\\\"Front Microphone\\\",\\\"visible\\\":1},{\\\"caseId\\\":18,\\\"enable\\\":1,\\\"caseName\\\":\\\"Headset-Left\\\",\\\"visible\\\":0},{\\\"caseId\\\":19,\\\"enable\\\":1,\\\"caseName\\\":\\\"Headset-Right\\\",\\\"visible\\\":0},{\\\"caseId\\\":20,\\\"enable\\\":1,\\\"caseName\\\":\\\"Headset Port\\\",\\\"visible\\\":1},{\\\"caseId\\\":21,\\\"enable\\\":1,\\\"caseName\\\":\\\"Lightning Headset\\\",\\\"visible\\\":1},{\\\"caseId\\\":22,\\\"enable\\\":1,\\\"caseName\\\":\\\"True Depth Camera\\\",\\\"visible\\\":1},{\\\"caseId\\\":23,\\\"enable\\\":1,\\\"caseName\\\":\\\"Front Camera\\\",\\\"visible\\\":1},{\\\"caseId\\\":24,\\\"enable\\\":1,\\\"caseName\\\":\\\"Rear Camera\\\",\\\"visible\\\":1},{\\\"caseId\\\":25,\\\"enable\\\":1,\\\"caseName\\\":\\\"Flash\\\",\\\"visible\\\":1},{\\\"caseId\\\":26,\\\"enable\\\":1,\\\"caseName\\\":\\\"Wide Camera\\\",\\\"visible\\\":1},{\\\"caseId\\\":27,\\\"enable\\\":1,\\\"caseName\\\":\\\"Telephoto Camera\\\",\\\"visible\\\":1},{\\\"caseId\\\":28,\\\"enable\\\":1,\\\"caseName\\\":\\\"Fingerprint Sensor\\\",\\\"visible\\\":2},{\\\"caseId\\\":29,\\\"enable\\\":1,\\\"caseName\\\":\\\"Face ID\\\",\\\"visible\\\":2},{\\\"caseId\\\":30,\\\"enable\\\":1,\\\"caseName\\\":\\\"Power Button\\\",\\\"visible\\\":1},{\\\"caseId\\\":31,\\\"enable\\\":1,\\\"caseName\\\":\\\"Home Button\\\",\\\"visible\\\":1},{\\\"caseId\\\":32,\\\"enable\\\":1,\\\"caseName\\\":\\\"Volume Down Button\\\",\\\"visible\\\":1},{\\\"caseId\\\":33,\\\"enable\\\":1,\\\"caseName\\\":\\\"Volume Up Button\\\",\\\"visible\\\":1},{\\\"caseId\\\":34,\\\"enable\\\":1,\\\"caseName\\\":\\\"Flip Switch\\\",\\\"visible\\\":1},{\\\"caseId\\\":35,\\\"enable\\\":1,\\\"caseName\\\":\\\"Back Button\\\",\\\"visible\\\":1},{\\\"caseId\\\":36,\\\"enable\\\":1,\\\"caseName\\\":\\\"Menu Button\\\",\\\"visible\\\":1},{\\\"caseId\\\":37,\\\"enable\\\":1,\\\"caseName\\\":\\\"LCD\\\",\\\"visible\\\":1},{\\\"caseId\\\":38,\\\"enable\\\":1,\\\"caseName\\\":\\\"Touch Screen\\\",\\\"visible\\\":1},{\\\"caseId\\\":39,\\\"enable\\\":1,\\\"caseName\\\":\\\"3D Touch\\\",\\\"visible\\\":1},{\\\"caseId\\\":40,\\\"enable\\\":0,\\\"caseName\\\":\\\"Spen\\\",\\\"visible\\\":1},{\\\"caseId\\\":41,\\\"enable\\\":0,\\\"caseName\\\":\\\"Spen Back Button\\\",\\\"visible\\\":1},{\\\"caseId\\\":42,\\\"enable\\\":0,\\\"caseName\\\":\\\"Spen Hover\\\",\\\"visible\\\":1},{\\\"caseId\\\":43,\\\"enable\\\":0,\\\"caseName\\\":\\\"Spen Menu Button\\\",\\\"visible\\\":1},{\\\"caseId\\\":44,\\\"enable\\\":0,\\\"caseName\\\":\\\"Spen Remove\\\",\\\"visible\\\":1},{\\\"caseId\\\":45,\\\"enable\\\":1,\\\"caseName\\\":\\\"Vibration\\\",\\\"visible\\\":1},{\\\"caseId\\\":46,\\\"enable\\\":1,\\\"caseName\\\":\\\"Wire Charge\\\",\\\"visible\\\":0},{\\\"caseId\\\":47,\\\"enable\\\":1,\\\"caseName\\\":\\\"Wireless Charge\\\",\\\"visible\\\":1}]\"}}";
String stop = "{\"action\":{\"name\":\"stop\",\"udid\":\"xxxxx\"}}";
    public static void main(String[] args) {


        try {
            String word = "abcd";
            Field value = String.class.getDeclaredField("value");
            value.setAccessible(true);
            char[] newValue = (char[])value.get(word);
            newValue[0] = 'b';
            System.out.println(word);
            String[] s={"111","222","333"};
            for (int i = 0; i < s.length; i++) {
                System.out.println(s[i]);
            }
            s[0] = "2222";
            for (int i = 0; i < s.length; i++) {
                System.out.println(s[i]);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
          new TestAdbClient().actionPerformed();

    }
    OutputStream outputStream;
    public void actionPerformed() {
        Socket socket = null;
//  PrintStream out = null;
        try {
//adb forward tcp:8000 tcp:10086  //65535
//"127.0.0.1", 8000
            socket = new Socket(InetAddress.getByName("192.168.1.250"), 8088);

            socket.setSendBufferSize(1024 * 10);
            socket.setReceiveBufferSize(1024 * 10);
            outputStream= socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
//读取服务器返回的数据
            startReadThread(inputStream);

            String msg;
            Scanner scanner = new Scanner(System.in, "UTF-8");
//发送的数据
           /* outputStream.write(negotiation.getBytes());
            outputStream.flush();*/

            while (scanner.hasNext()) {
                msg = scanner.nextLine();
                switch (msg) {
                    case "111":
                        outputStream.write(negotiation.getBytes());
                        break;
                    case "222":
                        outputStream.write(stop.getBytes());
                        break;
                    case "start":
                        //startThread();
                        sendMsg();
                        break;
                    default:
                        outputStream.write(msg.getBytes());
                        break;
                }
                outputStream.flush();
                System.out.println("消息已发送：" + msg);
                if (msg.equals("exit")) {
                    return;
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            try {
/*if (out != null) {
out.close();
}*/
   /* if (br != null) {
br.close();
}*/
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e3) {

                e3.printStackTrace();
            }
        }

    }
    private volatile static boolean receive =true;
    private void startThread() {

        new Thread(new Runnable() {
            @Override
            public void run() {
        //03FF000A4420000000640100  0001
                int k = 0;
                String msg ="";
                try {
                   while (true){
                        k++;
                        if(k<10){
                            msg = "000" +k;
                        }else if(k < 100){
                            msg = "00" +k;
                        }else if(k < 1000){
                            msg = "0" +k;
                        }else if(k < 10000){
                            msg = "" +k;
                        }else{
                            k=0;
                        }
                        outputStream.write(("03FF000A4420000000640100" + msg).getBytes());
                        outputStream.flush();
                        System.out.println("开始发送消息：" + msg);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    static int count =0;
    private void sendMsg(){
        String msg ="";
        try {

            count++;
            if (count < 10) {
                msg = "000" + count;
            } else if (count < 100) {
                msg = "00" + count;
            } else if (count < 1000) {
                msg = "0" + count;
            } else if (count < 10000) {
                msg = "" + count;
            } else {
                count = 0;
            }
            outputStream.write(("03FF000A4420000000640100" + msg).getBytes());
            outputStream.flush();
            System.out.println("开始发送消息：" + msg);
        }catch (Exception e){

        }

    }

    private void startReadThread(final InputStream inputStream) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                byte[] bytes = new byte[1024 * 10];
                int temp = 0;
                int length = 0;
                try {
                    while ((temp = inputStream.read(bytes)) != -1) {
                        //   System.out.println("temp:" + temp);
                        //   System.out.println(Arrays.toString(bytes));
                        String s = new String(bytes);
                        String str = s.trim();
                        if (str == null || str.length() == 0) {
                            continue;
                        }
                        if(str.equals("8300")){
                            System.out.println("服务器返回：收到消息" );
                            outputStream.write(("01FF000A4420000000640100").getBytes());
                            outputStream.flush();
                            System.out.println("获取发送结果" );
                        }else if(str.startsWith("8100")){
                            String replace = str.replace("8100", "");
                            System.out.println("服务器返回：" +replace);
                            sendMsg();
                        }else {
                            System.out.println("服务器返回：" + str);
                        }
                        Arrays.fill(bytes, (byte) 0);
                        //  bytes = new byte[1024 * 10];
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }


    public byte[] parse(String content) {
        byte[] body = content.getBytes(Charset.defaultCharset());
        ByteBuffer bb = ByteBuffer.allocate(4 + body.length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(body.length);
        bb.put(body);
        return bb.array();
    }

}