package yan.guoqi.rectphoto;

import java.io.ByteArrayOutputStream;
import java.util.List;

import yan.guoqi.rectphoto.RectPhoto.NetProcess;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class WifiCamera extends Activity{
	private EditText ipEdit;
	private EditText portEdit;
	private NetClient netClient;
	private Boolean sendflag=false;
	private Boolean sendfinish=true;
	private Button connectButton;
	private Boolean open=false;
	private Camera myCamera = null;
	private Camera.PreviewCallback streamIt = new Camera.PreviewCallback()
	{
		public void onPreviewFrame(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
		    System.out.println("---------onPreviewFrame-------\n");
		    Size size=camera.getParameters().getPreviewSize();
		    YuvImage yuv=new YuvImage(data,ImageFormat.NV21,size.width,size.height,null);
		    ByteArrayOutputStream oS=new ByteArrayOutputStream();

		    
		    yuv.compressToJpeg(new Rect(0,0,size.width,size.height),50,oS);
		    byte[] jdata = oS.toByteArray();
		    if(sendflag && sendfinish)
		    {
		    	System.out.println("----send----------------\n");
		    	sendfinish=false;
		    	netClient.sendDataByByte(jdata);
		    }  
		}
	};	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_camera);
		ipEdit=(EditText)this.findViewById(R.id.ip_edit);
		portEdit=(EditText)this.findViewById(R.id.duankou_edit);
		
		connectButton=(Button)this.findViewById(R.id.connect);
		connectButton.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(!open)
				{	
				  netClient=new NetClient();
				  netClient.connect(ipEdit.getText().toString(),Integer.parseInt(portEdit.getText().toString()));
				  netClient.setOnNetProcess(new NetProcess());
				  openCamera();
				  connectButton.setText("断开");
				}else
				{
					netClient.closeNet();
					connectButton.setText("连接");
					closeCamera();
					open=false;

				}	
			}
		});
	}
	
	private void openCamera()
	{
		myCamera = Camera.open();
		if(null != myCamera){			
			Camera.Parameters myParam = myCamera.getParameters();	
			myCamera.setPreviewCallback(streamIt);
		    if(myParam.isAutoExposureLockSupported())
		    {
		    	System.out.println("AutoExposureLockSupprt");
		    	if(myParam.getAutoExposureLock())  
	            {  
	                Log.d("wifiCamera", "Auto Exposure is Locked");   
	            }  
	            else   
	            {  
	                Log.d("wifiCamera", "Auto Exposure is not Locked");   
	            }  
		    }else
		    {
		    	System.out.println("AutoExposureLockSupprt not");
		    }	
		    List<String> focusModesList = myParam.getSupportedFocusModes();
	        if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {  
	              
	            Log.d("wifiCamera", "Camera Support AUTO FOCUS MODE");  
	          
	        }else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {  
	              
	            Log.d("wifiCamera", "Current Focus Mode is FOCUS_MODE_CONTINUOUS_PICTURE");  
	          
	        }else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {  
	              
	            Log.d("wifiCamera", "Current Focus Mode is FOCUS_MODE_CONTINUOUS_VIDEO");  
	          
	        }else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {  
	              
	            Log.d("wifiCamera", "Current Focus Mode is FOCUS_MODE_INFINITY");  
	          
	        }else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_MACRO)) {  
	              
	            Log.d("wifiCamera", "Current Focus Mode is FOCUS_MODE_MACRO");  
	          
	        }else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {  
	              
	            Log.d("wifiCamera", "Current Focus Mode is FOCUS_MODE_FIXED");  
	          
	        }else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_EDOF)) {  
	              
	            Log.d("wifiCamera", "Current Focus Mode is FOCUS_MODE_EDOF");  
	        }  			
	        myParam.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
	       // myParam.setAutoExposureLock(false);
	        //myParam.setAutoWhiteBalanceLock(false);
	        //myParam.set
			myCamera.setParameters(myParam);			
			myCamera.startPreview();
			//myCamera.autoFocus(myAutoFocusCallback);
			
		}
		
	}
	
	private void closeCamera()
	{
		if(null != myCamera)
		{
			myCamera.setPreviewCallback(null); /*在启动PreviewCallback时这个必须在前不然退出出错。
			这里实际上注释掉也没关系*/
			
			myCamera.stopPreview(); 
			//isPreview = false; 
			myCamera.release();
			myCamera = null;   
			//netProcess.closeNet();
		}		
	}
	private class NetProcess implements onNetProcess
	{

		public void OnConnect(String ip) {
			// TODO Auto-generated method stub
		    if(!open)
		    {
		    	openCamera();
		    }	
		}

		public void OnReceiveData(String data) {
			// TODO Auto-generated method stub
			
		}

		public void OnSendDataSuccess(int i) {
			// TODO Auto-generated method stub
			sendfinish=true;
		}

		public void onDisConnetct() {
			// TODO Auto-generated method stub
			
		}

		public void OnReceiveByte(byte[] obj) {
			// TODO Auto-generated method stub
			
		}

		public void onSendDataSuccess(byte[] b, int count) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetLocalAddress(String ip) {
			// TODO Auto-generated method stub
			
		}
		
	}	
}
