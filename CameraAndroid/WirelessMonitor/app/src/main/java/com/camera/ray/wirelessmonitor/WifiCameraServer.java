package com.camera.ray.wirelessmonitor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class WifiCameraServer extends Activity {
	private String tag = "WifiCamera";
	private Button startButton;
	private TextView connectStatus;
	private TextView connectIp;
	private TextView localIp;
	private TextView cameraInfo;
	private Camera myCamera = null;
	private Boolean sendflag = false;
	private Boolean sendfinish = true;
	private Boolean open = false;
	private CommunicationServer server = null;
	private String infoStr = "����ͷ��Ϣ:\n";
	private TextView receiveCmd;
	SurfaceTexture surfacetexture;
	private CheckBox cbTCP;
	private CheckBox cbUCP;
	private TextView recvCountText;
	private EditText scanPortEditText;
	private EditText deviceNameEditText;
	private EditText portEditText;
	private int recvCount;
	private Handler cameraHandler;//����UI
	private Camera.PreviewCallback streamIt = new Camera.PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			 System.out.println("---------onPreviewFrame-------\n");
			Size size = camera.getParameters().getPreviewSize();
			YuvImage yuv = new YuvImage(data, ImageFormat.NV21, size.width,
					size.height, null);
			ByteArrayOutputStream oS = new ByteArrayOutputStream();

			yuv.compressToJpeg(new Rect(0, 0, size.width, size.height), 50, oS);
			byte[] jdata = oS.toByteArray();
			if (sendflag && sendfinish) {
				// System.out.println("----send----------------\n");
				sendfinish = false;
				// netClient.sendDataByByte(jdata);
				if (server != null)
					server.sendDataByByte(jdata);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wificamera_server1);
		startButton = (Button) this.findViewById(R.id.start);
		connectStatus = (TextView) this.findViewById(R.id.status);
		connectIp = (TextView) this.findViewById(R.id.ip);
		cameraInfo = (TextView) this.findViewById(R.id.info);
		localIp = (TextView) this.findViewById(R.id.localip);
		receiveCmd = (TextView) this.findViewById(R.id.receiveCmd);
		deviceNameEditText=(EditText)this.findViewById(R.id.deviceName);
		scanPortEditText=(EditText)this.findViewById(R.id.scanPort);
		portEditText=(EditText)this.findViewById(R.id.devicePort);
		localIp.setText("" + getLocalIpAddress());

		cbTCP = (CheckBox) this.findViewById(R.id.tcp);
		cbTCP.setChecked(true);
		cbUCP = (CheckBox) this.findViewById(R.id.ucp);
		cbTCP.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (cbTCP.isChecked())
					cbUCP.setChecked(false);
			}

		});
		cbUCP.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (cbUCP.isChecked())
					cbTCP.setChecked(false);
			}

		});
		startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!open) {
					DeviceInfomation dInformation=new DeviceInfomation();
					dInformation.name=deviceNameEditText.getText().toString();
					if(cbTCP.isChecked())	
					{	
					  dInformation.netType=netType.tcp;
					}else if(cbUCP.isChecked())
					{
						dInformation.netType=netType.udp;
					} 	
					dInformation.port=portEditText.getText().toString();
					dInformation.scanString=scanPortEditText.getText().toString();
					System.out.println("----buttonClick-----open-----------"+dInformation.netType+","+dInformation.port);
					// TODO Auto-generated method stub
					if (cbTCP.isChecked())
						server = new CommunicationServer(dInformation);
					if (cbUCP.isChecked())
						server = new CommunicationServer(dInformation);
					//server.deviceName=deviceNameEditText.getText().toString();
					server.setOnNetProcess(new DataProcess());
					
					connectStatus.setText("�ȴ�����");
					if (myCamera == null)
						openCamera();
					startButton.setText("�ر�");
					open = true;
				} else {
					System.out
							.println("----buttonClick-----close-----------\n");
					startButton.setText("��");
					connectStatus.setText("δ����");
					
					if (server != null) {
						server.closeNet();
						server = null;
					}
					if (myCamera != null) {
						closeCamera();
						myCamera = null;
					}
					infoStr = "";
					cameraInfo.setText(infoStr);
					open = false;
				}
				enableDisableEidtText();
			}

		});
		cameraHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
			   switch(msg.what)
			   {
			   case 0: //���ش򿪹ر�״̬
				   connectIp.setText(msg.obj.toString());
				   break;
			   case 1: //����״̬
				   connectStatus.setText(msg.obj.toString());
				   break;
			   case 2: //���ܵ�������
				   break;
			   default:
				   break;
			   }
			}

		};
		surfacetexture=new SurfaceTexture(10);
	}

	private void enableDisableEidtText()
	{
			cbTCP.setEnabled(!open);
			cbUCP.setEnabled(!open);
			scanPortEditText.setEnabled(!open);
			deviceNameEditText.setEnabled(!open);
			portEditText.setEnabled(!open);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("------------onDestroy----------------");
		if (myCamera != null) {
			closeCamera();
		}
		if (server != null) {
			server.closeNet();
		}
	}

	private String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& !inetAddress.isLinkLocalAddress())// ����õ�����IPv4�ĵ�ַ��ȥ��&&!inetAddress.isLinkLocalAddress()��õ�ipv6�ĵ�ַ
					{
						return inetAddress.getHostAddress().toString();
					}
				}

			}
		} catch (SocketException ex) {

		}
		return null;
	}

	private String getLocalIp() {

		WifiManager wifimanager = (WifiManager) this
				.getSystemService(android.content.Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifimanager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		return String.format("%d.%d.%d.%d", (ipAddress & 0xff),
				(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
				(ipAddress >> 24 & 0xff));
	}

	private String getLocalIp2() {
		InetAddress localMachine = null;
		try {
			localMachine = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return localMachine.getHostAddress();
	}

	private void openCamera() {
		System.out.println("--------openCamera----");
		myCamera = Camera.open();
		if (null != myCamera) {
			Camera.Parameters myParam = myCamera.getParameters();
			myCamera.setPreviewCallback(streamIt);

			if (myParam.isAutoExposureLockSupported()) {
				System.out.println("AutoExposureLockSupprt");
				if (myParam.getAutoExposureLock()) {
					Log.d("wifiCamera", "Auto Exposure is Locked");
				} else {
					Log.d("wifiCamera", "Auto Exposure is not Locked");
				}
			} else {
				System.out.println("AutoExposureLockSupprt not");
			}
			List<Size> cSizeList = myParam.getSupportedPreviewSizes();
			this.infoStr += "֧�ֵķֱ���:\n";
			for (Size s : cSizeList) {

				Log.i(tag, "SupportpreviewSize height=" + s.height + ",width="
						+ s.width);
				this.infoStr += "�߶�:" + s.height + "���:" + s.width + "\n";
			}
			this.infoStr += "�Ƿ�֧������:";
			if (myParam.isZoomSupported()) {
				this.infoStr += "��" + "���֧������Ϊ��" + myParam.getMaxZoom()
						+ "���ڵ�����Ϊ:" + myParam.getZoom() + "\n";
			} else {
				this.infoStr += "��\n";
			}
			// �鿴camera�ĶԽ�ģʽ
			List<String> focusModesList = myParam.getSupportedFocusModes();
			if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {

				Log.d("wifiCamera", "Camera Support AUTO FOCUS MODE");

			} else if (focusModesList
					.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {

				Log.d("wifiCamera",
						"Current Focus Mode is FOCUS_MODE_CONTINUOUS_PICTURE");

			} else if (focusModesList
					.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {

				Log.d("wifiCamera",
						"Current Focus Mode is FOCUS_MODE_CONTINUOUS_VIDEO");

			} else if (focusModesList
					.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {

				Log.d("wifiCamera", "Current Focus Mode is FOCUS_MODE_INFINITY");

			} else if (focusModesList
					.contains(Camera.Parameters.FOCUS_MODE_MACRO)) {

				Log.d("wifiCamera", "Current Focus Mode is FOCUS_MODE_MACRO");

			} else if (focusModesList
					.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {

				Log.d("wifiCamera", "Current Focus Mode is FOCUS_MODE_FIXED");

			} else if (focusModesList
					.contains(Camera.Parameters.FOCUS_MODE_EDOF)) {

				Log.d("wifiCamera", "Current Focus Mode is FOCUS_MODE_EDOF");
			}
			// myParam.setZoom(20);
			// myParam.
			myParam.setPreviewSize(640, 480);
			myParam.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			myCamera.setParameters(myParam);
			//myCamera.setPreviewDisplay(null);
			//myCamera.setPreviewDisplay(null);
			try {
				myCamera.setPreviewTexture(surfacetexture);
			} catch (IOException e) {
				e.printStackTrace();
			}
			myCamera.startPreview();
		}
		this.cameraInfo.setText(infoStr);

	}

	private void closeCamera() {
		if (null != myCamera) {
			System.out.println("-----------close camera-----------");
			myCamera.setPreviewCallback(null); /*
												 * ������PreviewCallbackʱ���������ǰ��Ȼ�˳�����
												 * ����ʵ����ע�͵�Ҳû��ϵ
												 */

			myCamera.stopPreview();
			myCamera.release();
			myCamera = null;
		}
	}

	private class DataProcess implements onDataProcess {

		public void OnConnect(String ip) {
			// TODO Auto-generated method stub
			sendflag = true;
			sendfinish = true;
			// connectStatus.setText("״̬��������");
			// connectIp.setText("����ip:" + ip);
			/*
			 * if(myCamera==null) { openCamera(); }
			 */
			Message msg = new Message();
			msg.what=1;
			msg.obj="������";
			cameraHandler.sendMessage(msg);
			
			Message msg1 = new Message();
			msg1.what=0;
			msg1.obj=ip;
			cameraHandler.sendMessage(msg1);

		}

		public void OnReceiveData(String data) {
			// TODO Auto-generated method stub

		}

		public void OnSendDataSuccess(int i) {
			// TODO Auto-generated method stub
			sendfinish = true;
		}

		public void onDisConnetct() {
			// TODO Auto-generated method stub
			System.out.println("------------onDisConnetct----------------");
			Message msg = new Message();
			msg.what=1;
			msg.obj="�ѶϿ�";
			cameraHandler.sendMessage(msg);		
			
			Message msg1 = new Message();
			msg1.what=0;
			msg1.obj="��";
			cameraHandler.sendMessage(msg1);
			//connectStatus.setText("״̬���ѶϿ�");
			sendflag = false;
			sendfinish = false;
			/*
			 * if (myCamera != null) { closeCamera(); myCamera=null; }
			 */
			/*
			 * if (server != null) { server.closeNet(); }
			 */
		}

		public void OnReceiveByte(byte[] obj) {
			// TODO Auto-generated method stub
			if (obj[0] == 1) {
				myCamera.stopPreview();
				// Camera.Parameters myParam = myCamera.getParameters();
				Camera.Parameters myParam = myCamera.getParameters();
				myParam.setZoom(15);
				myCamera.setParameters(myParam);
				receiveCmd.setText("�Ŵ� ");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				myCamera.startPreview();
				// myCamera.startPreview();
			} else {
				receiveCmd.setText("��С");

				myCamera.stopPreview();
				// Camera.Parameters myParam = myCamera.getParameters();
				Camera.Parameters myParam = myCamera.getParameters();
				myParam.setZoom(0);
				myCamera.setParameters(myParam);
				receiveCmd.setText("�Ŵ� ");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				myCamera.startPreview();
				// myCamera.startPreview();

			}
		}

		public void onSendDataSuccess(byte[] b, int count) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetLocalAddress(String ip) {
			// TODO Auto-generated method stub
			// localIp.setText("����ip:"+ip);
			// System.out.println(ip);
		}

		@Override
		public void onReceiveDataPackage(DataPackage dp) {
			// TODO Auto-generated method stub
			if (dp.cmd == 1)// ��С�Ŵ�
			{
				Camera.Parameters myParam = myCamera.getParameters();
				int currentZoom = myParam.getZoom();
				if (currentZoom == dp.data[0]) {

				} else if (currentZoom > dp.data[0]) {
					if ((int) dp.data[0] <= 0) {
						dp.data[0] = 0;
					}
					myParam.setZoom((int) (dp.data[0]));
					System.out.println("--------zoomRate=" + dp.data[0]);
					myCamera.setParameters(myParam);
					receiveCmd.setText("��С ");

				} else {
					if ((int) dp.data[0] >= myParam.getMaxZoom()) {
						dp.data[0] = (byte) myParam.getMaxZoom();
					}
					myParam.setZoom((int) (dp.data[0]));
					System.out.println("--------zoomRate=" + dp.data[0]);
					myCamera.setParameters(myParam);
					receiveCmd.setText("�Ŵ� ");

				}

			}
		}

	}

}
