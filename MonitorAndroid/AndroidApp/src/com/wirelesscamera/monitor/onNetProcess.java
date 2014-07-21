package com.wirelesscamera.monitor;


public interface onNetProcess {
		public void OnConnect(String ip);
		
		public void OnReceiveData(String data);
		
		public void OnSendDataSuccess(int i);
		
		public void onDisConnetct();

		public void OnReceiveByte(byte[] obj,int port,String ip);
		
		public void OnReceiveByte(byte[] obj,int count);
		
		public void onSendDataSuccess(byte[] b,int count);
		
		public void onGetLocalAddress(String ip);
	
}
