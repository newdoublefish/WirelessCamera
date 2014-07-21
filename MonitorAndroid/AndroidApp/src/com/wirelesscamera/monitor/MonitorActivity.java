package com.wirelesscamera.monitor;

import android.app.Activity;
import android.os.Bundle;

public class MonitorActivity extends Activity{
	MonitorSurfaceView mSurfaceView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		mSurfaceView=new MonitorSurfaceView(this);
		setContentView(mSurfaceView);
		
	}

}
