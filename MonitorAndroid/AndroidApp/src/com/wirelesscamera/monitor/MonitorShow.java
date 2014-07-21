package com.wirelesscamera.monitor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

public class MonitorShow extends Activity {

	private Device device;
	MonitorSurfaceView msv;
	private MonitorServer ms;
	private NetClient netClient;
	public DataPackage pD=new DataPackage();
	public byte parseStatus = parseType.wait0xaa;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		msv = new MonitorSurfaceView(this);
		setContentView(msv);
		Intent intent = this.getIntent();
		device = (Device) intent.getSerializableExtra("device");
		ms = new MonitorServer(device);
		netClient = new NetClient();
		netClient.setOnNetProcess(new NetHandler());
		netClient.connect(device.ip, Integer.parseInt(device.port));

		// Toast.makeText(this, "device.name="+device.Name,
		// Toast.LENGTH_SHORT).show();

	}

	class NetHandler implements onNetProcess {

		@Override
		public void OnConnect(String ip) {
			// TODO Auto-generated method stub

		}

		@Override
		public void OnReceiveData(String data) {
			// TODO Auto-generated method stub

		}

		@Override
		public void OnSendDataSuccess(int i) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onDisConnetct() {
			// TODO Auto-generated method stub

		}

		@Override
		public void OnReceiveByte(byte[] obj, int port, String ip) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSendDataSuccess(byte[] b, int count) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetLocalAddress(String ip) {
			// TODO Auto-generated method stub

		}

		@Override
		public void OnReceiveByte(byte[] obj,int count) {
			// TODO Auto-generated method stub
			handleReceiveByte(obj,count);
			//
		}

	}

	public void handleReceiveByte(byte[] b,int count) {

		for (int i = 0; i < count; i++) {

			byte data = b[i];
			switch (parseStatus) {
			case parseType.wait0xaa:
				if ((byte) 0xaa == data) {
					parseStatus = parseType.wait0x55;
					// System.out.println("------------wait0xaa b="+data);
				} else {
					//System.out.println("error 1");
					parseStatus = parseType.wait0xaa;
				}
				break;
			case parseType.wait0x55:
				if ((byte) 0x55 == data) {
					parseStatus = parseType.waitCmd;
					//pD = new DataPackage();
					pD.resetDataPackage();
					// System.out.println("------------wait0x55 b="+data);
				} else {
					//System.out.println("error 2");
					parseStatus = parseType.wait0xaa;
				}
				break;
			case parseType.waitCmd:
				//System.out.println("------------waitCmd b=" + data);
				pD.cmd = data;
				parseStatus = parseType.waitDataLen;
				break;
			case parseType.waitDataLen:
				pD.dataLen += (data << (pD.dataLengthStep * 8));
				pD.dataLengthStep--;
				// System.out.println("------------waitDataLen b="+data);
				if ((pD.dataLengthStep < 0) && (pD.dataLen>0))
				{
					//pD.data = new byte[pD.dataLen];
					if(pD.dataLen>pD.maxLen)
					{
						//pD.maxLen=pD.dataLen;
						//pD.data=new byte[pD.maxLen];
						parseStatus = parseType.wait0xaa;
						break;
					}else
					{

					}
					parseStatus = parseType.waitData;
				}
				break;
			case parseType.waitData:
				// System.out.println("------------waitData b="+data);
				pD.data[pD.dataIndex++] = data;
				if (pD.dataIndex >= pD.dataLen) {
					parseStatus = parseType.waittail;
				}
				break;
			case parseType.waittail:
				if (data == (byte) 0xA5) {
					// System.out.println("------------waittail b="+data);
					parseStatus = parseType.dataready;
				} else {
					System.out.println("error 3");
					parseStatus = parseType.wait0xaa;
				}
				break;
			default:
				parseStatus = parseType.wait0xaa;
				break;
			}

		}
		if (parseStatus == parseType.dataready) {
			parseStatus = parseType.wait0xaa;
			//System.out.println("-----------showImage------------\n");
			showImage(pD.data,pD.dataLen);
		}
	}
	
	public void showImage(byte[] buffer,int count)
	{
		Bitmap bm=BitmapFactory.decodeByteArray(buffer, 0, count);
		msv.setBitmap(bm);

	}
}
