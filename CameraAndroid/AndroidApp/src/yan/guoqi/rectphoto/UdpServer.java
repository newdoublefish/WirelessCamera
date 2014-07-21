package yan.guoqi.rectphoto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

public class UdpServer {
	private DatagramSocket ds=null;
	private AcceptThread aThread;
	private sendThread sThread;
	private onNetProcess process;
	private InetAddress sAddress;
	private int port;
	private Boolean flag;
	private int portNum=8888;
	public Boolean sendFlag=false;
	public void setOnNetProcess(onNetProcess listener)
	{
		this.process=listener;
	} 	
    public UdpServer(int num)
    {
    	portNum=num;
    	try {
			ds=new DatagramSocket(portNum);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	flag=true;
    	aThread=new AcceptThread();
    	aThread.start();
    	sThread=new sendThread();
    	sThread.start();
    	
    }
    
	public void sendDataByByte(byte[] b) {
		/*
      try {
    	//DatagramSocket ds=new DatagramSocket(8888);
		DatagramPacket sendDp=new DatagramPacket(b,b.length,sAddress);
		ds.send(sendDp);
		ds.close();
	} catch (SocketException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
   
      //sendFlag=true;
		//System.out.println("--sendDat");
		sThread.SendOrder(b);
		
	}   
	
	public void closeNet()
	{
		flag=false;
		ds.close();
		
	}
	
	private class sendThread extends Thread
	{
		private Queue<byte[]> sendQueue=new LinkedList<byte[]>();
 		public  Boolean SendOrder(byte[] b)
		{
 			sendQueue.offer(b);
			return false;
		}
    	@Override
    	public void run()
    	{   byte[] sendByte;
    	    while(flag)
    	    {
                 if((sendByte=sendQueue.poll())!=null)
                 {
                	 System.out.println("-----------queque---");
             		
            		try {
            			//if(ds.isConnected())
            			int i=sendByte.length;
            			int index=0;
            			while(i>4096)
            			{
            				byte[] send=new byte[1024];
            				System.arraycopy(sendByte, index, send, 0, 1024);
            				index+=1024;
            				i-=1024;
            				DatagramPacket sendDp=new DatagramPacket(send,send.length,sAddress,portNum);
            				ds.send(sendDp);

            				//sThread.SendOrder(send);
            			}	
            			if(i>0)
            			{
            				byte[] send=new byte[i];
            				System.arraycopy(sendByte, index, send, 0, i);
            				//sThread.SendOrder(send);
            				DatagramPacket sendDp=new DatagramPacket(send,send.length,sAddress,portNum);
            				ds.send(sendDp);
            				
            			}	            			
						
						process.OnSendDataSuccess(0);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                 }	 
    	    
    	    }
    	}
	}
    private class AcceptThread extends Thread
    {
    	DatagramPacket dp;
    	byte[] receiveBuffer=new byte[1024];
    	//byte[] buf;
    	byte[] send;
    	public AcceptThread()
    	{
    		 dp=new DatagramPacket(receiveBuffer,receiveBuffer.length);
    	}
    	@Override
    	public void run()
    	{
    		 while(flag)
    		 {
    			 try {
					ds.receive(dp);
					int receiveCount=dp.getLength();
	                send=new byte[receiveCount];
	                System.arraycopy(receiveBuffer, 0, send, 0, receiveCount); 	
	                //process.OnReceiveByte(send);
	                //process.OnConnect(ip);
	                InetAddress address=dp.getAddress();
	                port=dp.getPort();
	                sAddress=dp.getAddress();
	                process.OnConnect(address.toString());	 
	                //sendDataByByte(send);
	                System.out.println("connet ip ="+address.toString()+",port="+port);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		 }	 
    	}
    }
}
