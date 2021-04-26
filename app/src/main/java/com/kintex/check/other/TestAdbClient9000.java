package com.kintex.check.other;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Scanner;

public class TestAdbClient9000 {
String negotiation = "{\"action\": {\"name\": \"negotiation\", \"udid\": \"xxxxx\"}}";
String start = "{\"action\":{\"name\":\"start\",\"udid\":\"xxxx\",\"test_case_list\":[{\"caseName\":\"SimReader\",\"caseId\":1,\"enable\":1,\"visible\":1},{\"caseName\":\"NetWork\",\"caseId\":2,\"enable\":1,\"visible\":1},{\"caseName\":\"WiFi\",\"caseId\":3,\"enable\":1,\"visible\":0},{\"caseName\":\"GPS\",\"caseId\":4,\"enable\":1,\"visible\":0},{\"caseName\":\"BlueTooth\",\"caseId\":5,\"enable\":1,\"visible\":0},{\"caseName\":\"Gyroscope\",\"caseId\":6,\"enable\":1,\"visible\":0},{\"caseName\":\"Barometer\",\"caseId\":7,\"enable\":1,\"visible\":0},{\"caseName\":\"ProximitySensor\",\"caseId\":8,\"enable\":1,\"visible\":1},{\"caseName\":\"Accelerometer\",\"caseId\":9,\"enable\":1,\"visible\":2},{\"caseName\":\"LoudSpeaker\",\"caseId\":11,\"enable\":1,\"visible\":1},{\"caseName\":\"Earpiece\",\"caseId\":12,\"enable\":1,\"visible\":1},{\"caseName\":\"MicES\",\"caseId\":13,\"enable\":1,\"visible\":1},{\"caseName\":\"VidMicES\",\"caseId\":14,\"enable\":1,\"visible\":1},{\"caseName\":\"Microphone\",\"caseId\":15,\"enable\":1,\"visible\":1},{\"caseName\":\"VideoMicrophone\",\"caseId\":16,\"enable\":1,\"visible\":1},{\"caseName\":\"FrontMicrophone\",\"caseId\":17,\"enable\":1,\"visible\":1},{\"caseName\":\"Headset-Left\",\"caseId\":18,\"enable\":1,\"visible\":0},{\"caseName\":\"Headset-Right\",\"caseId\":19,\"enable\":1,\"visible\":0},{\"caseName\":\"HeadsetPort\",\"caseId\":20,\"enable\":1,\"visible\":1},{\"caseName\":\"LightningHeadset\",\"caseId\":21,\"enable\":1,\"visible\":1},{\"caseName\":\"TrueDepthCamera\",\"caseId\":22,\"enable\":1,\"visible\":1},{\"caseName\":\"FrontCamera\",\"caseId\":23,\"enable\":1,\"visible\":1},{\"caseName\":\"RearCamera\",\"caseId\":24,\"enable\":1,\"visible\":1},{\"caseName\":\"Flash\",\"caseId\":25,\"enable\":1,\"visible\":1},{\"caseName\":\"WideCamera\",\"caseId\":26,\"enable\":1,\"visible\":1},{\"caseName\":\"TelephotoCamera\",\"caseId\":27,\"enable\":1,\"visible\":1},{\"caseName\":\"FingerprintSensor\",\"caseId\":28,\"enable\":1,\"visible\":2},{\"caseName\":\"FaceID\",\"caseId\":29,\"enable\":1,\"visible\":2},{\"caseName\":\"PowerButton\",\"caseId\":30,\"enable\":1,\"visible\":1},{\"caseName\":\"HomeButton\",\"caseId\":31,\"enable\":1,\"visible\":1},{\"caseName\":\"VolumeDownButton\",\"caseId\":32,\"enable\":1,\"visible\":1},{\"caseName\":\"VolumeUpButton\",\"caseId\":33,\"enable\":1,\"visible\":1},{\"caseName\":\"FlipSwitch\",\"caseId\":34,\"enable\":1,\"visible\":1},{\"caseName\":\"BackButton\",\"caseId\":35,\"enable\":1,\"visible\":1},{\"caseName\":\"MenuButton\",\"caseId\":36,\"enable\":1,\"visible\":1},{\"caseName\":\"LCD\",\"caseId\":37,\"enable\":1,\"visible\":1},{\"caseName\":\"TouchScreen\",\"caseId\":38,\"enable\":1,\"visible\":1},{\"caseName\":\"3DTouch\",\"caseId\":39,\"enable\":1,\"visible\":1},{\"caseName\":\"Spen\",\"caseId\":40,\"enable\":0,\"visible\":1},{\"caseName\":\"SpenBackButton\",\"caseId\":41,\"enable\":0,\"visible\":1},{\"caseName\":\"SpenHover\",\"caseId\":42,\"enable\":0,\"visible\":1},{\"caseName\":\"SpenMenuButton\",\"caseId\":43,\"enable\":0,\"visible\":1},{\"caseName\":\"SpenRemove\",\"caseId\":44,\"enable\":0,\"visible\":1},{\"caseName\":\"Vibration\",\"caseId\":45,\"enable\":1,\"visible\":1},{\"caseName\":\"WireCharge\",\"caseId\":46,\"enable\":1,\"visible\":0},{\"caseName\":\"WirelessCharge\",\"caseId\":47,\"enable\":1,\"visible\":1}]}}";
String stop = "{\"action\":{\"name\":\"stop\",\"udid\":\"xxxxx\"}}";
    public static void main(String[] args) {
        new TestAdbClient9000().actionPerformed();

    }

    public void actionPerformed() {
// TODO Auto-generated method stub
        Socket socket = null;
//  PrintStream out = null;
//  BufferedReader br = null;
        try {
//adb forward tcp:8000 tcp:10086
//"127.0.0.1", 8000
            socket = new Socket(InetAddress.getByName("127.0.0.1"), 9000);
            socket.setSendBufferSize(1024 * 10);
            socket.setReceiveBufferSize(1024 * 10);
            OutputStream outputStream = socket.getOutputStream();
//  out = new PrintStream(socket.getOutputStream(), true, "UTF8");
            InputStream inputStream = socket.getInputStream();
//读取服务器返回的数据
            startReadThread(inputStream);
/*br = new BufferedReader(new InputStreamReader(
));*/


//   byte[] parse = parse(json);

            String msg;
            Scanner scanner = new Scanner(System.in, "UTF-8");
//   msg = br.readLine();
//   System.out.println("客户端返回：" + msg);
            outputStream.write(negotiation.getBytes());
            outputStream.flush();

//out.println(json);
//out.flush();
            while (scanner.hasNext()) {
                msg = scanner.nextLine();
                if(msg.equals("111")){
                    outputStream.write(start.getBytes());
                }else if(msg.equals("222")){
                    outputStream.write(stop.getBytes());
                }else{
                    outputStream.write(msg.getBytes());
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
// TODO Auto-generated catch block
                e3.printStackTrace();
            }
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
                        System.out.println("服务器返回：" + str);
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