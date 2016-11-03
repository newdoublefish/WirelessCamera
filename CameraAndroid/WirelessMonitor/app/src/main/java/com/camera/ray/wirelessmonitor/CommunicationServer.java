package com.camera.ray.wirelessmonitor;

import android.R.string;

public class CommunicationServer {

	public UdpServer udpServer;
	public NetServer tcpServer;
	private onDataProcess process;
	public byte parseStatus=parseType.wait0xaa;
	private DataPackage pD;
	private udpDiscoverServer discoverServer;
	private DeviceInfomation deviceInfo;
	public void setOnNetProcess(onDataProcess listener)
	{
		this.process=listener;
	}   
	public CommunicationServer(DeviceInfomation info)
	{
		deviceInfo=info;
		discoverServer=new udpDiscoverServer(deviceInfo);
		if(deviceInfo.netType==netType.udp)
		{	
		  udpServer=new UdpServer(Integer.parseInt(deviceInfo.port));
		  udpServer.setOnNetProcess(new NetProcess());
		}else if(deviceInfo.netType==netType.tcp)
		{
			tcpServer=new NetServer(Integer.parseInt(deviceInfo.port));
			tcpServer.setOnNetProcess(new NetProcess());
		}	
			
		
		
	}
	public CommunicationServer(String ip,int port)
	{
		
	}
	
	public void sendDataByByte(byte[] b)
	{
		DataPackage dp=new DataPackage();
		dp.cmd=cmdType.pictureData;
		dp.data=b;
		dp.dataLen=b.length;
		if(deviceInfo.netType==netType.udp)
		{	
			udpServer.sendDataByByte(dp.SerialDataPackage());
		}else if(deviceInfo.netType==netType.tcp)
		{
			tcpServer.sendDataByByte(dp.SerialDataPackage());
		}	
		

	}
	
	public void closeNet()
	{
		
		if(deviceInfo.netType==netType.udp)
		{	
			udpServer.closeNet();
		}else if(deviceInfo.netType==netType.tcp)
		{
			tcpServer.closeNet();
		}
		if(discoverServer!=null)
		discoverServer.closeNet();
	}
	
	private class NetProcess implements onNetProcess {

		public void OnConnect(String ip) {
			// TODO Auto-generated method stub
             process.OnConnect(ip);
		}

		public void OnReceiveData(String data) {
			// TODO Auto-generated method stub

		}

		public void OnSendDataSuccess(int i) {
			// TODO Auto-generated method stub
			process.OnSendDataSuccess(i);
		}

		public void onDisConnetct() {
			// TODO Auto-generated method stub
            process.onDisConnetct();
		}

		public void OnReceiveByte(byte[] obj) {
			// TODO Auto-generated method stub
              handleReceiveByte(obj);
		}

		public void onSendDataSuccess(byte[] b, int count) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetLocalAddress(String ip) {
			// TODO Auto-generated method stub

		}

	}	
	
	public void handleReceiveByte(byte[] b)
	{
		
		for(int i=0;i<b.length;i++)
		{
			
			byte data=b[i];
			//System.out.println("------11------receive b="+data);
			switch(parseStatus)
			{
			case parseType.wait0xaa:
				if((byte)0xaa==data)
				{
					parseStatus=parseType.wait0x55;
					//System.out.println("------------wait0xaa b="+data);
				}else
				{
					System.out.println("error 1");
					parseStatus=parseType.wait0xaa;
				}	
				break;
			case parseType.wait0x55:
				if((byte)0x55==data)
			    {
			    	parseStatus=parseType.waitCmd;
			    	pD=new DataPackage();
			    	//System.out.println("------------wait0x55 b="+data);
			    }else
				{
			    	System.out.println("error 2");
					parseStatus=parseType.wait0xaa;
				}	
				break;
			case parseType.waitCmd:
				System.out.println("------------waitCmd b="+data);
				pD.cmd=data;
				parseStatus=parseType.waitDataLen;
				break;
			case parseType.waitDataLen:
				pD.dataLen+=(data<<(pD.dataLengthStep*8));
				pD.dataLengthStep--;
				//System.out.println("------------waitDataLen b="+data);
				if(pD.dataLengthStep<0)
				{
					pD.data=new byte[pD.dataLen];
					parseStatus=parseType.waitData;
				}	
				break;
			case parseType.waitData:
				//System.out.println("------------waitData b="+data);
				pD.data[pD.dataIndex++]=data;
				if(pD.dataIndex>=pD.dataLen)
				{
					parseStatus=parseType.waittail;
				}	
				break;
			case parseType.waittail:
				if(data==(byte)0xA5)
				{
					//System.out.println("------------waittail b="+data);
					parseStatus=parseType.dataready;
				}else
				{
					System.out.println("error 3");
					parseStatus=parseType.wait0xaa;
				}	
				break;
			default:
				parseStatus=parseType.wait0xaa;
				break;
			}
					
		}	
		if(parseStatus==parseType.dataready)
		{
			parseStatus=parseType.wait0xaa;
			if(process!=null)
			{
				//System.out.println("------------dataready");
				// byte[] cmd=new byte[1];
				 //cmd[0]=pD.cmd;
				// process.OnReceiveByte(cmd);
				process.onReceiveDataPackage(pD);
			}	
		}	
	}
	
}
