package com.wirelesscamera.monitor;

import java.io.Serializable;


public class Device implements Serializable {
	public String Name;
	public String ip;
	public String port;
	public int maxMissNumber=5;
	public int type = netType.tcp;
	
	
	public void parseDeviceInfo(byte[] data)
	{

		int length=data.length;
		type = data[0];
		int portNum=TypeCovert.bitLeftMove(data[2], 8) | TypeCovert.bitLeftMove(data[1], 0);
		port=portNum+"";
		byte[] nameData=new byte[length-3];
		System.arraycopy(data, 3, nameData, 0, length-3);
		Name=TypeCovert.byte2string(nameData, "GBK");
		ip=ip.substring(1);

		
	}

}
