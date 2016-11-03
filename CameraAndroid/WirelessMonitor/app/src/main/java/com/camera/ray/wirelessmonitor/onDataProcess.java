package com.camera.ray.wirelessmonitor;


public interface onDataProcess {
		public void OnConnect(String ip);
		
		public void OnReceiveData(String data);
		
		public void OnSendDataSuccess(int i);
		
		public void onDisConnetct();

		public void onReceiveDataPackage(DataPackage dp);
		
		public void OnReceiveByte(byte[] obj);
		
		public void onSendDataSuccess(byte[] b, int count);
		
		public void onGetLocalAddress(String ip);
}
