package com.wirelesscamera.monitor;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class WirelessMonitor extends Activity {

	private ListView list;
	private List<Device> deviceList = new ArrayList<Device>();
	private static final int MONITOR_ID = Menu.FIRST;
	private static final int DETAIL_ID = Menu.FIRST + 1;
	private Button setButton;
	MyAdapter mAdapter;
	ScanServer scanServer=new ScanServer();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		list = (ListView) this.findViewById(R.id.list);
		//initDeviceList();
		mAdapter=new MyAdapter(this);
		list.setAdapter(mAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				System.out.println("postion=" + arg2);

			}

		});
		list.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu arg0, View arg1,
					ContextMenuInfo arg2) {
				// TODO Auto-generated method stub
				arg0.setHeaderTitle("设备信息");
				arg0.add(0, MONITOR_ID, 0, "监控");
				arg0.add(0, DETAIL_ID, 0, "详情");

			}

		});
		
		setButton=(Button)this.findViewById(R.id.start);
		setButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				deviceList.clear();
				refreshListView();
			}
			
		});
		// showListView();
		scanServer.setonDeviceDiscovery(new DeviceDiscoveryHandler());
		scanServer.startScan();
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int index = info.position;

		switch (item.getItemId()) {
		case MONITOR_ID:
			ShowDevice(deviceList.get(index));
			break;
		case DETAIL_ID:
			showDetail(deviceList.get(index));
			break;
		}
		return super.onContextItemSelected(item);
	}

	private void ShowDevice(Device device) {
		// TODO Auto-generated method stub
		Intent intent=new Intent(WirelessMonitor.this,MonitorShow.class);
		Bundle bundle=new Bundle();
		bundle.putSerializable("device", device);
		//intent.s
		intent.putExtras(bundle);
		startActivity(intent);
		
	}


	private void showDetail(Device device) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = LayoutInflater.from(this);
		final View addView = inflater.inflate(R.layout.deviceinfo, null);
		TextView name = (TextView) addView.findViewById(R.id.name);
		TextView ip = (TextView) addView.findViewById(R.id.ip);
		TextView port = (TextView) addView.findViewById(R.id.port);
		name.setText(device.Name);
		ip.setText(device.ip);
		port.setText(device.port);
		new AlertDialog.Builder(this).setTitle("设备详情").setView(addView)
				.setPositiveButton("确定", null).show();
	}


	private void refreshListView() {
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public class DeviceDiscoveryHandler implements onDeviceDiscovery
	{

		@Override
		public void onReportDevice(List<Device> dList) {
			// TODO Auto-generated method stub
			deviceList=dList;
			refreshListView();
		}
	}
	
	public final class ViewHolder {
		public TextView deviceName;
	}

	public class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return deviceList.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder viewholder = null;
			if (convertView == null) {
				viewholder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.list_view, null);
				viewholder.deviceName = (TextView) convertView
						.findViewById(R.id.deviceName);
				convertView.setTag(viewholder);
			} else {
				// viewholder.deviceName.setText(deviceList.get(position).Name);
				viewholder = (ViewHolder) convertView.getTag();

			}
			viewholder.deviceName.setText(deviceList.get(position).Name);
			return convertView;
		}
	}
}
