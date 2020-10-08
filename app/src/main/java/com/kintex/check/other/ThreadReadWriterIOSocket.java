package com.kintex.check.other;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.kintex.check.bean.AdbBean;

import org.greenrobot.eventbus.EventBus;


public class ThreadReadWriterIOSocket implements Runnable
{
	private Socket client;
	private Context context;

	public ThreadReadWriterIOSocket(Context context, Socket client)
	{
		this.client = client;
		this.context = context;
	}

	@Override
	public void run()
	{
		Log.d("chl", "a client has connected to server!");
		BufferedOutputStream out;
		BufferedInputStream in;
		//command
		String command = "screencap -p /sdcard/screen.jpg";
		Process process = null;
		DataOutputStream os = null;
		try
		{
			/* PC端发来的数据msg */
			String currCMD = "";
			out = new BufferedOutputStream(client.getOutputStream());
			in = new BufferedInputStream(client.getInputStream());
			androidService.ioThreadFlag = true;
			//command

			while (androidService.ioThreadFlag)
			{
				try
				{
					if (!client.isConnected())
					{
						break;
					}
					/* 接收PC发来的数据 */
					Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "will read......");
					/* 读操作命令 */
					currCMD = readCMDFromSocket(in);
					Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "**currCMD ==== " + currCMD);
					if(TextUtils.isEmpty(currCMD)){
						EventBus.getDefault().post(new AdbBean("断开连接" ));
					}else {
						EventBus.getDefault().post(new AdbBean("收到的内容：" + currCMD));
					}

					out.write(("手机收到了："+currCMD).getBytes());
					out.flush();

					/*if(currCMD.equals("getfile")){
						currCMD = "file";
						out.write(currCMD.getBytes());
						out.flush();
						while(true) {
							process = Runtime.getRuntime().exec(new String[]{"su", "-c", command});// the phone must be root,it can exctue the adb command
							process.waitFor();
							saveMyBitmap(getSmallBitmap("/sdcard/screen.jpg"));
*//*

						currCMD = "file";
						out.write(currCMD.getBytes());
						out.flush();
						currCMD = readCMDFromSocket(in);
						Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "begin send file**currCMD ==== " + currCMD);
*//*

							byte[] filebytes = FileHelper.readFile("screensmall.jpg");
							byte[] filelength = new byte[4];
							filelength = MyUtil.intToByte(filebytes.length);
							byte[] fileformat = null;
							fileformat = ".png".getBytes();
							out.write(filelength);
							out.flush();
							out.write(filebytes);
							out.flush();

						}
//						currCMD = readCMDFromSocket(in);

					}*/

				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			out.close();
			in.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (client != null)
				{
					Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "client.close()");
					client.close();
				}
			} catch (IOException e)
			{
				Log.e(androidService.TAG, Thread.currentThread().getName() + "---->" + "read write error333333");
				e.printStackTrace();
			}
		}
	}

	/* 读取命令 */
	public String readCMDFromSocket(InputStream in)
	{
		int MAX_BUFFER_BYTES = 2048;
		String msg = "";
		byte[] tempbuffer = new byte[MAX_BUFFER_BYTES];
		try
		{
			int numReadedBytes = in.read(tempbuffer, 0, tempbuffer.length);
				msg = new String(tempbuffer, 0, numReadedBytes, "utf-8");
				tempbuffer = null;
		} catch (Exception e)
		{
			Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "readFromSocket error");
			androidService.ioThreadFlag = false;
			e.printStackTrace();
		}

		return msg;
	}

	public void saveMyBitmap(Bitmap mBitmap)  {
		File f = new File( "/sdcard/screensmall.jpg");
		FileOutputStream fOut = null;
		if(f.exists()){
			f.delete();
		}
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mBitmap.compress(Bitmap.CompressFormat.JPEG, 60, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}