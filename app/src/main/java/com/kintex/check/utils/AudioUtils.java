package com.kintex.check.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.elvishew.xlog.XLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

//import android.util.Log;

//音频工具类，现在用的是audiorecoder，但是mediarecoder也可用于录音，但是就是操作较简单，所以不能够对录音后的文件进行处理了
public class AudioUtils {
    //获得sdcard路径
    private static final String SDCard = Environment
            .getExternalStorageDirectory().getPath();
    // 音频获取源
    private int audioSource = AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static int sampleRateInHz = 48000;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private static int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    private static int bufferSizeInBytes = 0;
    private static AudioRecord audioRecord;
    private static boolean isRecord = false;// 设置正在录制的状态
    // AudioName裸音频数据文件
    private static final String tempAudioPath = SDCard + "/tempAudio.tmp";
    // NewAudioName可播放的音频文件
    private static String wavAudioPath = "";

    public AudioUtils(String recodeType, int hz) {
        creatAudioRecord(recodeType,hz);
    }

    //创造录音对象
    private void creatAudioRecord(String recodeType, int hz) {
        // 获得缓冲区字节大小，根据指定的参数大小
        sampleRateInHz=hz;
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        // 创建AudioRecord对象
        if(recodeType.equals("0")){
            audioSource= AudioSource.MIC;
        }else if(recodeType.equals("1")){
            audioSource= AudioSource.VOICE_CALL;
        }else if(recodeType.equals("2")){
            audioSource= AudioSource.VOICE_UPLINK;
        }else if(recodeType.equals("3")){
            audioSource= AudioSource.VOICE_DOWNLINK;
        }
        audioRecord = new AudioRecord(audioSource, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);
        //audioRecord = findAudioRecord();
    }
    private static int currentVolume = 0;

    public boolean startRecord(String wavPath) {
        //获取录音对象得状态，如果是处于已经初始化的状态，则会开启录音，否则会返回false标识不能开始录音
        if(audioRecord.getState() == AudioRecord.STATE_INITIALIZED){
            audioRecord.startRecording();
        }else{
            return false;
        }
        currentVolume = 0;
        // 让录制状态为true
        isRecord = true;
        wavAudioPath = wavPath;
        // 开启音频文件写入线程
        new Thread(new AudioRecordThread()).start();
        return true;
    }

    private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100, 16000 };
    public AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        return null;
    }

    public boolean stopRecord() {
        return close();
    }

    private boolean close() {
        if (audioRecord != null) {
            isRecord = false;// 停止文件写入
            currentVolume = 0;
            if(audioRecord.getState() == AudioRecord.STATE_INITIALIZED){
                try{
                    audioRecord.stop();
                    audioRecord.release();// 释放资源
                    audioRecord=null;
                }catch(Exception e){
                    e.printStackTrace();
                    return false;
                }
            }else{
                return false;
            }
        }else{
            return false;
        }
        return true;
    }
    //这个线程的作用就是将录制的源数据写入一个文件中，但是这是一个不能播放的文件，所以需要给裸数据加上头文件才能够进行播放
    class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            writeDateTOFile();// 往文件中写入裸数据，写入的这个文件也是一个临时的文件。最终的能够进行播放的文件还是加上头文件之后的文件
        //    copyWaveFile(tempAudioPath, wavAudioPath);// 给裸数据加上头文件
        }
    }



    /**
     * 这里将数据写入文件，但是并不能播放，因为AudioRecord获得的音频是原始的裸音频，
     * 如果需要播放就必须加入一些格式或者编码的头信息。但是这样的好处就是你可以对音频的 裸数据进行处理，比如你要做一个爱说话的TOM
     * 猫在这里就进行音频的处理，然后重新封装 所以说这样得到的音频比较容易做一些音频的处理。
     */
    private void writeDateTOFile() {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        short[] audioData = new short[bufferSizeInBytes];
        FileOutputStream fos = null;
        int readsize = 0;
        try {


            File file = new File(tempAudioPath);
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);// 建立一个可存取字节的文件

        } catch (Exception e) {
            XLog.d(e);
        }
        int hz = 0;
        while (isRecord == true) {
            hz ++;
            if (audioRecord == null) {
                Log.i("123456", "audioRecord.is null");
            }
            if (audioData == null) {
                Log.i("123456", "audiodata.is null");
            }
            try {
                readsize = audioRecord.read(audioData, 0, bufferSizeInBytes);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {

                try {

                        // 将 buffer 内容取出，进行平方和运算
                        double v = 0;
                        for (short value : audioData) {
                            v += value * value;
                        }
                        // 平方和除以数据总长度，得到音量大小。
                        double mean = v / readsize;
                        //double volume = 10 * Math.log10(mean);
                        //  进行处理
                       // currentVolume =(int) Math.sqrt(mean);
                        currentVolume =(int)(10 * Math.log10(mean));
                        if(currentVolume>=100){
                            int nextInt = new Random().nextInt(15);
                            Log.d("wylee", "nextInt = "+ nextInt);
                            currentVolume = (100 - nextInt);
                            Log.d("wylee", "分贝值max = " + currentVolume + "dB");
                        }
                        Log.d("wylee", "分贝值 = " + currentVolume + "dB");


                   // fos.write(audioData);

                } catch (Exception e) {

                }
            }
        }
        try {
            fos.close();// 关闭写入流
            } catch (Exception e){
            e.printStackTrace();
            }
    }

    public int getCurrentVolume(){

        return currentVolume;
    }

    // 这里得到可播放的音频文件
    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = sampleRateInHz;
        int channels = 1;
        long byteRate = 16 * sampleRateInHz * channels / 8;
        byte[] data = new byte[bufferSizeInBytes];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static boolean copyFile(String source, String target) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            File targetFile = new File(target);
            if (targetFile.exists()) {
                targetFile.delete();
            }else{
                targetFile.createNewFile();
            }
            in = new FileInputStream(source);
            out = new FileOutputStream(target);
            byte[] buffer = new byte[8 * 1024];
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
            out.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

    }

    /**
     * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
     * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有 自己特有的头文件。
     */
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    public static short[] toShortArray(byte[] src) {

        int count = src.length >> 1;
        short[] dest = new short[count];
        for (int i = 0; i < count; i++) {
            dest[i] = (short) (src[i * 2] << 8 | src[2 * i + 1] & 0xff);
        }
        return dest;
    }

}
