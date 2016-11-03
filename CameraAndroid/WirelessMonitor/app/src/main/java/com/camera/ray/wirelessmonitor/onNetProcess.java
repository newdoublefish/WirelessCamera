package com.camera.ray.wirelessmonitor;


public interface onNetProcess {
		public void OnConnect(String ip);
		
		public void OnReceiveData(String data);
		
		public void OnSendDataSuccess(int i);
		
		public void onDisConnetct();

		public void OnReceiveByte(byte[] obj);
		
		public void onSendDataSuccess(byte[] b, int count);
		
		public void onGetLocalAddress(String ip);
}
