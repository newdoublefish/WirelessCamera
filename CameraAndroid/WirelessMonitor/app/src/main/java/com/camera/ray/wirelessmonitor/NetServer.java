package com.camera.ray.wirelessmonitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;


public class NetServer {
	private OutputStream outStream = null;
	private Socket clientSocket = null;//ʵ����ֻ֧��һ���豸���ӣ���Ϊ����ͻ���socket���ǻᱻ�������Ҫ�����������ӡ�
	private ServerSocket mServerSocket = null;

	private Handler mHandler = null;

	private AcceptThread mAcceptThread = null;//���߳�ֻ��һ��
    private onNetProcess process;
    private String localIp;
    private List<Socket> cSocketList;
    public InputStream mInputStream;
    public Boolean AcceptThreadFlag=true;
    public Boolean ReceiveThreadFlag=true;
    public int portNum=8888;
	public void setOnNetProcess(onNetProcess listener)
	{
		this.process=listener;
	}    
	public NetServer(int num) {
		portNum=num;
		cSocketList=new ArrayList<Socket>();
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0: {
					System.out.println("connectIp=" + (msg.obj).toString());
					process.OnConnect((msg.obj).toString());
					break;
				}
				case 1: {
                    //����Ϊ�յ������ݣ��Ժ����������������
					byte[] b=(byte[]) msg.obj;
					process.OnReceiveByte(b);
					break;
				}
				case 2:{
					  process.onDisConnetct();
				}
				}

			}
		};
		mAcceptThread = new AcceptThread();
		mAcceptThread.start();
	}

	public void sendDataByByte(byte[] b) {
		if(clientSocket!=null)
		{	
		try {
			// ���Socket�������
			if(clientSocket.isConnected())
			{	
			  //outStream = clientSocket.getOutputStream(); ��Ӧ����������ÿ�ζ���ȡ�����Ӳ���
			/*  outStream.write(DataPackage.header); //����Э��ͷ
			  //outStream.write(DataPackage.)
			  outStream.write(cmdType.pictureData);
			  outStream.write(DataPackage.int2bytes(b.length));//����Э�����ݳ���
			  outStream.write(b);//��������
			  outStream.write(DataPackage.tail);//����Э��β��*/
			 /* DataPackage dp=new DataPackage();
			  dp.cmd=cmdType.pictureData;
			  dp.data=b;
			  dp.dataLen=b.length;*/
			  outStream.write(b);
			  process.OnSendDataSuccess(0);
			}  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}

	}

	public void closeNet() {//�����ȷ�Ĺر�socket���ر��߳�
		AcceptThreadFlag=false;
		ReceiveThreadFlag=false;
		try {
			if(mInputStream!=null)
			   mInputStream.close();
			if(outStream!=null)
			  outStream.close();
			mServerSocket.close();
			for(int i=0;i<cSocketList.size();i++)
			{
				if(cSocketList.get(i)!=null)
				{
					cSocketList.get(i).close();
				}	
			}	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private class AcceptThread extends Thread {
		@Override
		public void run() {

			try {
				// ʵ����ServerSocket�������ö˿ں�Ϊ8888
				mServerSocket = new ServerSocket();
				//����Ϊ��ַ�ظ�ʹ�ã������ĺô�����������̵߳�ʱ�򣬲�����ְ󶨳���������bind error
				//һ��ر�socket�������ϵĹرգ�Ҫ���240s��Ϊʲô��
				mServerSocket.setReuseAddress(true);
				mServerSocket.bind(new InetSocketAddress(portNum));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("================1");
			while(AcceptThreadFlag)
			{				
			try {
				// �ȴ��ͻ��˵����ӣ�������
				
				clientSocket = mServerSocket.accept();
				
				System.out.println("================2");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("================3");
			if(clientSocket!=null)
			{	
			if (clientSocket.isConnected() && !mServerSocket.isClosed()) {
				cSocketList.add(clientSocket);
				ReceiveThreadFlag=true;
				try {
					outStream = clientSocket.getOutputStream();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ReceiveThread mReceiveThread = new ReceiveThread(clientSocket);
				// ���������߳�
				mReceiveThread.start();

				Message msg = new Message();
				msg.what = 0;
				// ��ȡ�ͻ���IP
				msg.obj = clientSocket.getInetAddress().getHostAddress();
				// ������Ϣ
				mHandler.sendMessage(msg);
			}
			}}
			if(!mServerSocket.isClosed())
			{
				try {
					mServerSocket.close();
					System.out.println("---close--accept thread----\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
				

		}

	}

	private class ReceiveThread extends Thread {
		//private InputStream mInputStream = null;
		private byte[] buf;
		private Socket sock;
		private int count=0;

		public ReceiveThread(Socket s) {
			sock=s;
			if(sock.isConnected())
			{	
			  try {
				// ���������
				  mInputStream = sock.getInputStream();
			      } catch (IOException e) {
				  // TODO Auto-generated catch block
				  e.printStackTrace();
			      }
			}
			
		}

		@Override
		public void run() {
			while (ReceiveThreadFlag) {
				this.buf = new byte[1024];   
				
				// ��ȡ���������(������)
				try {
					count=mInputStream.read(buf);
					//System.out.println("receive count="+count);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(count>0)
				{	
                  byte[] send=new byte[count];
                  System.arraycopy(buf, 0, send, 0, count);            
				  Message msg = new Message();
				  msg.what = 1;
				  msg.obj = send;
				  mHandler.sendMessage(msg);
				}else
				{
					 
					 Message msg = new Message();
					 msg.what = 2;
					 mHandler.sendMessage(msg);
					 ReceiveThreadFlag=false;
					 try {
						sock.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}	

			}
			try {
				if(sock.isConnected())
				{	
				  sock.close();
				  sock=null;
				}  
				System.out.println("---close--receive thread----\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
