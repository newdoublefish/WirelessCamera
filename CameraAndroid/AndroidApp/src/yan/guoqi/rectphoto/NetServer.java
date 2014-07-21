package yan.guoqi.rectphoto;

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
	private Socket clientSocket = null;//实际上只支持一个设备连接，因为这个客户端socket总是会被替代，主要是用于再连接。
	private ServerSocket mServerSocket = null;

	private Handler mHandler = null;

	private AcceptThread mAcceptThread = null;//此线程只有一个
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
                    //这里为收到的数据，以后可以用作分析命令
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
			// 获得Socket的输出流
			if(clientSocket.isConnected())
			{	
			  //outStream = clientSocket.getOutputStream(); 不应该这样做，每次都获取，增加操作
			/*  outStream.write(DataPackage.header); //发送协议头
			  //outStream.write(DataPackage.)
			  outStream.write(cmdType.pictureData);
			  outStream.write(DataPackage.int2bytes(b.length));//发送协议数据长度
			  outStream.write(b);//发送数据
			  outStream.write(DataPackage.tail);//发送协议尾部*/
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

	public void closeNet() {//如何正确的关闭socket，关闭线程
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
				// 实例化ServerSocket对象并设置端口号为8888
				mServerSocket = new ServerSocket();
				//设置为地址重复使用，这样的好处是再起这个线程的时候，不会出现绑定出错的情况，bind error
				//一般关闭socket不会马上的关闭，要间隔240s，为什么呢
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
				// 等待客户端的连接（阻塞）
				
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
				// 开启接收线程
				mReceiveThread.start();

				Message msg = new Message();
				msg.what = 0;
				// 获取客户端IP
				msg.obj = clientSocket.getInetAddress().getHostAddress();
				// 发送消息
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
				// 获得输入流
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
				
				// 读取输入的数据(阻塞读)
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
