package com.wirelesscamera.monitor;

public class MonitorServer {
	private NetClient tcpNet;
	private Device device;
	
	public MonitorServer(Device d)
	{
		device=d;
	}

}
