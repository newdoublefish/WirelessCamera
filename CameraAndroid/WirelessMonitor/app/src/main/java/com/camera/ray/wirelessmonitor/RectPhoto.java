package com.camera.ray.wirelessmonitor;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class RectPhoto extends Activity implements SurfaceHolder.Callback{
	private static final String tag="yan";
	private boolean isPreview = false;
	private SurfaceView mPreviewSV = null; //Ԥ��SurfaceView
	private SurfaceHolder mySurfaceHolder = null;
	private ImageButton mPhotoImgBtn = null;
	private Camera myCamera = null;
	private Bitmap mBitmap = null;
	private AutoFocusCallback myAutoFocusCallback = null;
	private int index=0;
	private Boolean sendflag=false;
	private EditText ipEdit;
	private EditText duankou;
	private Button connectButton;
	private NetClient netProcess=null;
	private Boolean sendfinish=true;
	//private NetProcess netResponce;
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
		    	netProcess.sendDataByByte(jdata);
		    }
		    /*
		    if(camera.getParameters().getPreviewFormat()==ImageFormat.NV21)
		    {
		    	System.out.println("-----nv21---------width="+size.width+",height="+size.height);
		    }	
			if(null != data){
				 System.out.println("---------onPreviewFrame-------1\n");
				mBitmap = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);//data���ֽ����ݣ����������λͼ
			}
			
			//����FOCUS_MODE_CONTINUOUS_VIDEO)֮��myParam.set("rotation", 90)ʧЧ��ͼƬ��Ȼ������ת�ˣ�������Ҫ��ת��
			Matrix matrix = new Matrix();
			matrix.postRotate((float)90.0);
			Bitmap rotaBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
			//����ͼƬ��sdcard
			 System.out.println("---------onPreviewFrame-------2\n");
			if(null != rotaBitmap)
			{
				saveJpeg(rotaBitmap);
			}	*/	    
		}
	};	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//����ȫ���ޱ���
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		Window myWindow = this.getWindow();
		myWindow.setFlags(flag, flag);

		setContentView(R.layout.activity_rect_photo);
		this.ipEdit=(EditText)this.findViewById(R.id.ip_edit);
		this.duankou=(EditText)this.findViewById(R.id.duankou_edit);
		this.connectButton=(Button)this.findViewById(R.id.connect);

		//��ʼ��SurfaceView
		mPreviewSV = (SurfaceView)findViewById(R.id.previewSV);
		mySurfaceHolder = mPreviewSV.getHolder();
		mySurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);//translucent��͸�� transparent͸��
		mySurfaceHolder.addCallback(this);
		mySurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		//�Զ��۽������ص�
		myAutoFocusCallback = new AutoFocusCallback() {

			public void onAutoFocus(boolean success, Camera camera) {
				// TODO Auto-generated method stub
				if(success)//success��ʾ�Խ��ɹ�
				{
					Log.i(tag, "myAutoFocusCallback: success...");
					//myCamera.setOneShotPreviewCallback(null);

				}
				else
				{
					//δ�Խ��ɹ�
					Log.i(tag, "myAutoFocusCallback: ʧ����...");

				}


			}
		};

		mPhotoImgBtn = (ImageButton)findViewById(R.id.photoImgBtn);
		//�ֶ���������ImageButton�Ĵ�СΪ120��120,ԭͼƬ��С��64��64
		LayoutParams lp = mPhotoImgBtn.getLayoutParams();
		lp.width = 120;
		lp.height = 120;		
		mPhotoImgBtn.setLayoutParams(lp);				
		mPhotoImgBtn.setOnClickListener(new PhotoOnClickListener());
		mPhotoImgBtn.setOnTouchListener(new MyOnTouchListener());

        this.connectButton.setOnClickListener(new OnClickListener()
        {

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				netProcess=new NetClient();
				netProcess.connect(ipEdit.getText().toString(),Integer.parseInt(duankou.getText().toString()));
				netProcess.setOnNetProcess(new NetProcess());
			}
        	
        });
	}


	/*����������SurfaceHolder.Callback�����Ļص�����*/
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) 
	// ��SurfaceView/Ԥ������ĸ�ʽ�ʹ�С�����ı�ʱ���÷���������
	{
		// TODO Auto-generated method stub		
		Log.i(tag, "SurfaceHolder.Callback:surfaceChanged!");
		initCamera();

	}


	public void surfaceCreated(SurfaceHolder holder) 
	// SurfaceView����ʱ/����ʵ������Ԥ�����汻����ʱ���÷��������á�
	{
		// TODO Auto-generated method stub		
		myCamera = Camera.open();
		try {
			myCamera.setPreviewDisplay(mySurfaceHolder);
			Log.i(tag, "SurfaceHolder.Callback: surfaceCreated!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if(null != myCamera){
				myCamera.release();
				myCamera = null;
			}
			e.printStackTrace();
		}



	}


	public void surfaceDestroyed(SurfaceHolder holder) 
	//����ʱ������
	{
		// TODO Auto-generated method stub
		Log.i(tag, "SurfaceHolder.Callback��Surface Destroyed");
		if(null != myCamera)
		{
			myCamera.setPreviewCallback(null); /*������PreviewCallbackʱ���������ǰ��Ȼ�˳�����
			����ʵ����ע�͵�Ҳû��ϵ*/
			
			myCamera.stopPreview(); 
			isPreview = false; 
			myCamera.release();
			myCamera = null;   
			netProcess.closeNet();
		}

	}

	//��ʼ�����
	public void initCamera(){
		if(isPreview){
			myCamera.stopPreview();
		}
		if(null != myCamera){			
			Camera.Parameters myParam = myCamera.getParameters();
			//			//��ѯ��Ļ�Ŀ�͸�
			//			WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
			//			Display display = wm.getDefaultDisplay();
			//			Log.i(tag, "��Ļ��ȣ�"+display.getWidth()+" ��Ļ�߶�:"+display.getHeight());

			//myParam.setPictureFormat(PixelFormat.JPEG);//�������պ�洢��ͼƬ��ʽ

			//			//��ѯcamera֧�ֵ�picturesize��previewsize
			//			List<Size> pictureSizes = myParam.getSupportedPictureSizes();
			//			List<Size> previewSizes = myParam.getSupportedPreviewSizes();
			//			for(int i=0; i<pictureSizes.size(); i++){
			//				Size size = pictureSizes.get(i);
			//				Log.i(tag, "initCamera:����ͷ֧�ֵ�pictureSizes: width = "+size.width+"height = "+size.height);
			//			}
			//			for(int i=0; i<previewSizes.size(); i++){
			//				Size size = previewSizes.get(i);
			//				Log.i(tag, "initCamera:����ͷ֧�ֵ�previewSizes: width = "+size.width+"height = "+size.height);
			//
			//			}


			//���ô�С�ͷ���Ȳ���
			///myParam.setPictureSize(1280, 960);
			//myParam.setPreviewSize(960, 720);		
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
			myCamera.autoFocus(myAutoFocusCallback);
			isPreview = true;
		}
	}

	/*Ϊ��ʵ�����յĿ������������ձ�����Ƭ��Ҫ���������ص�����*/
	ShutterCallback myShutterCallback = new ShutterCallback() 
	//���Ű��µĻص������������ǿ����������Ʋ��š����ꡱ��֮��Ĳ�����Ĭ�ϵľ������ꡣ
	{

		public void onShutter() {
			// TODO Auto-generated method stub
			Log.i(tag, "myShutterCallback:onShutter...");

		}
	};
	PictureCallback myRawCallback = new PictureCallback() 
	// �����δѹ��ԭ���ݵĻص�,����Ϊnull
	{

		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.i(tag, "myRawCallback:onPictureTaken...");

		}
	};
	PictureCallback myJpegCallback = new PictureCallback() 
	//��jpegͼ�����ݵĻص�,����Ҫ��һ���ص�
	{

		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.i(tag, "myJpegCallback:onPictureTaken...");
			if(null != data){
				mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);//data���ֽ����ݣ����������λͼ
				myCamera.stopPreview();
				isPreview = false;
			}
			//����FOCUS_MODE_CONTINUOUS_VIDEO)֮��myParam.set("rotation", 90)ʧЧ��ͼƬ��Ȼ������ת�ˣ�������Ҫ��ת��
			Matrix matrix = new Matrix();
			matrix.postRotate((float)90.0);
			Bitmap rotaBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
			//����ͼƬ��sdcard
			if(null != rotaBitmap)
			{
				saveJpeg(rotaBitmap);
			}

			//�ٴν���Ԥ��
			myCamera.startPreview();
			isPreview = true;
		}
	};
	//���հ����ļ���
	public class PhotoOnClickListener implements OnClickListener{

		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(isPreview && myCamera!=null){
				myCamera.takePicture(myShutterCallback, null, myJpegCallback);	
			}

		}

	}
	/*����һ��Bitmap�����б���*/
	public void saveJpeg(Bitmap bm){
		
		String savePath = "/mnt/sdcard/rectPhoto/";
		File folder = new File(savePath);
		if(!folder.exists()) //����ļ��в������򴴽�
		{
			folder.mkdir();
		}
		long dataTake = System.currentTimeMillis();
		String jpegName = savePath + index +".jpg";
		index++;
		Log.i(tag, "saveJpeg:jpegName--" + jpegName);
		//File jpegFile = new File(jpegName);
		try {
			FileOutputStream fout = new FileOutputStream(jpegName);
			BufferedOutputStream bos = new BufferedOutputStream(fout);

			//			//�����Ҫ�ı��С(Ĭ�ϵ��ǿ�960����1280),��ĳɿ�600����800
			//			Bitmap newBM = bm.createScaledBitmap(bm, 600, 800, false);

			bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
			Log.i(tag, "saveJpeg���洢��ϣ�");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i(tag, "saveJpeg:�洢ʧ�ܣ�");
			e.printStackTrace();
		}
	}

	/*Ϊ��ʹͼƬ��ť���º͵���״̬��ͬ�����ù�����ɫ�ķ���.���µ�ʱ����ͼƬ��ɫ�䵭*/
	public class MyOnTouchListener implements OnTouchListener{

		public final  float[] BT_SELECTED=new float[]
				{ 2, 0, 0, 0, 2,
			0, 2, 0, 0, 2,
			0, 0, 2, 0, 2,
			0, 0, 0, 1, 0 };			    

		public final float[] BT_NOT_SELECTED=new float[]
				{ 1, 0, 0, 0, 0,
			0, 1, 0, 0, 0,
			0, 0, 1, 0, 0,
			0, 0, 0, 1, 0 };
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				v.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_SELECTED));
				v.setBackgroundDrawable(v.getBackground());
			}
			else if(event.getAction() == MotionEvent.ACTION_UP){
				v.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_NOT_SELECTED));
				v.setBackgroundDrawable(v.getBackground());
				
			}
			return false;
		}

	}
	
	@Override
	public void onBackPressed()
	//�����а����ؼ�ʱҪ�ͷ��ڴ�
	{
		// TODO Auto-generated method stub
		super.onBackPressed();
		RectPhoto.this.finish();
	}
	
	class NetProcess implements onNetProcess
	{

		public void OnConnect(String ip) {
			// TODO Auto-generated method stub
			sendflag=true;
			/*ipEdit.setEnabled(false);
			ipEdit.setTextColor(Color.GREEN);
			duankou.setEnabled(false);
			duankou.setTextColor(Color.GREEN);
			connectButton.setEnabled(false);*/
			
			
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
