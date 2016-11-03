package com.camera.ray.wirelessmonitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class NetClient {
	private TextView mTextView = null;
	private Socket clientSocket = null;
	private OutputStream outStream = null;
	private Handler mHandler = null;
	private ReceiveThread mReceiveThread = null;
	private requestNet mRequstNet;
	private boolean stop = true;
	private String ip;
	private String duankou;
	private byte[] sendBuf;
	private int sendIndex = 0;
	private onNetProcess onp=null;
	
	public void setOnNetProcess(onNetProcess listener)
	{
		this.onp=listener;
	}


	public NetClient() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// ��ʾ���յ�������
				if (msg.what == 0) {
					byte[] b = (byte[]) msg.obj;
					/*
					 * System.out.println("size=" + b.length); for (int i = 0; i
					 * < b.length; i++) System.out.println("b[i]=" + b[i]);
					 */
					// HandleData(b);
				} else if (msg.what == 1) {
				}
			}
		};

	}

	// for test need to delete definitily

	// for test
	public void sendDataByByte(byte[] b) {
		try {
			// ���Socket�������
			outStream = clientSocket.getOutputStream();
			outStream.write(DataPackage.header);
			outStream.write(DataPackage.int2bytes(b.length));
			outStream.write(b);
			outStream.write(DataPackage.tail);
			onp.OnSendDataSuccess(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void sendDataByByte(byte[] b, int count) {
		try {
			// ���Socket�������

			outStream = clientSocket.getOutputStream();
			// outStream.write(b);
			outStream.write(b, 0, count);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendData(String s) {

		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		byte[] msgBuffer = null;
		byte[] startMaintain = { (byte) 0xAA, 0x55, 0x01, 0x00, 0x00, 0x01,
				0x00, (byte) 0x99, 0x60, (byte) 0xa5 };
		// ���EditTex������
		try {
			// �ַ�����ת��
			msgBuffer = s.getBytes("GB2312");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			// ���Socket�������
			outStream = clientSocket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			// ��������msgBuffer
			outStream.write(msgBuffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void connect(String ip, int duankou) {
		this.ip = ip;
		this.duankou = duankou + "";
		mRequstNet = new requestNet(ip, duankou);
		mRequstNet.start();
	}

	private class ReceiveThread extends Thread {
		private InputStream inStream = null;
		private int count = 0;
		private byte[] buf;
		private String str = null;

		ReceiveThread(Socket s) {
			if (s.isBound()) {
				try {
					// ���������
					this.inStream = s.getInputStream();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		public void run() {
			while (!stop) {
				this.buf = new byte[1024];
				// ��ȡ���������(������)
				try {
					count = this.inStream.read(buf);
					// System.out.println("receive count="+count);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				byte[] send = new byte[count];
				System.arraycopy(buf, 0, send, 0, count);
				Message msg = new Message();
				msg.what = 0;
				msg.obj = send;
				mHandler.sendMessage(msg);
			}
		}

	}

	private class requestNet extends Thread {
		private String ip;
		private int duan;

		requestNet(String IP, int duankou) {
			this.ip = IP;
			this.duan = duankou;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				// ʵ�����������ӵ�������
				clientSocket = new Socket(this.ip, this.duan);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/*
			 * displayToast("���ӳɹ���"); // ���Ӱ�ťʹ�� connectButton.setEnabled(false);
			 * // ���Ͱ�ťʹ�� sendButton.setEnabled(true);
			 */

			mReceiveThread = new ReceiveThread(clientSocket);
			if(onp!=null)
			   onp.OnConnect(this.ip);
			stop = false;
			// �����߳�
			mReceiveThread.start();
			Message msg = new Message();
			msg.what = 1;
			// ������Ϣ
			mHandler.sendMessage(msg);
		}

	}

	public void closeNet() {
		if (mReceiveThread != null) {
			stop = true;
			mReceiveThread.interrupt();
		}
		if (mRequstNet != null) {
			mRequstNet.interrupt();
		}
		try {
			System.out.println("===============ondestroy===");
			if (outStream != null)
				outStream.close();
			if (clientSocket != null)
				clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
    /*
	private class mListener implements OnHandleListener {

		@Override
		public void OnHandleProtocol(ProtocolBase p) {
			// TODO Auto-generated method stub
			System.out.println("====finish=====");
			// printProtocol(p);
			oListener.OnReceiveProtocol(p);
		}

	}
	*/
}
