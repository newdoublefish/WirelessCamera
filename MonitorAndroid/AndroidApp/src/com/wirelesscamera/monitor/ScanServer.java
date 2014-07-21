package com.wirelesscamera.monitor;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;

public class ScanServer {
	private List<Device> deviceList = new ArrayList<Device>();
	public byte parseStatus = parseType.wait0xaa;
	private List<Device> newDeviceList = null;
	public DataPackage pD;
	private onDeviceDiscovery odd;
	Handler handler = new Handler();
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			// send scan cmd
			String s = "hello from monitor!";
			reportList();
			newDeviceList = new ArrayList<Device>();
			

			udpServer.sendDataByByte(s.getBytes());
			handler.postDelayed(this, 2000);
		}
	};
	private udpDiscoverServer udpServer;
	private int scanPort = 8866;
	public void setonDeviceDiscovery(onDeviceDiscovery listener)
	{
		this.odd=listener;
	} 	
	public ScanServer() {
		udpServer = new udpDiscoverServer(scanPort);
		udpServer.setOnNetProcess(new NetProcessListener());
	}

	public void reportList() {
		if (deviceList != null && newDeviceList != null) {
			if ((deviceList.size() == 0) && (newDeviceList.size() != 0)) {
				deviceList = newDeviceList;
                //TODO:report deviceList;
				odd.onReportDevice(deviceList);
			} else {
				if (needToFreshDeviceList(deviceList, newDeviceList)) {
					//TODO:report deviceList;
					odd.onReportDevice(deviceList);
				}
			}

		}
	}

	public Boolean needToFreshDeviceList(List<Device> list1, List<Device> list2)
    {
	
        Boolean flag = false;
        for(Device d1 : list2)
        {
            if (!isInList(d1, deviceList))
            {
            	deviceList.add(d1);
                flag = true;
            }
        }
        for (Device d1 : deviceList)
        {
            if (isInList(d1,list2))
            {
                d1.maxMissNumber = 5;
            }
            else
            {
                d1.maxMissNumber--;
            }
        }

        List<Device> dList = new ArrayList<Device>();
        for(Device d1 : deviceList)
        {
            if (d1.maxMissNumber <= 0)
            {
                flag = true;
            }
            else
            {
                dList.add(d1);
            }
        }
        deviceList = dList;
        return flag;
    }

	public Boolean isInList(Device d,List<Device> list)
    {
        if (list.size() == 0)
        {	
            return false;
        }

        for(Device device : list)
        {
            if ((d.ip.equals(device.ip) ) && (d.type==device.type) && (d.Name.equals(device.Name))
                &&(d.port.equals(device.port)))
            {	
                return true;
            }
        }
        return false;
    }

	public void startScan() {
		handler.postDelayed(runnable, 2000);
	}

	class NetProcessListener implements onNetProcess {

		@Override
		public void OnConnect(String ip) {
			// TODO Auto-generated method stub

		}

		@Override
		public void OnReceiveData(String data) {
			// TODO Auto-generated method stub

		}

		@Override
		public void OnSendDataSuccess(int i) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onDisConnetct() {
			// TODO Auto-generated method stub

		}

		@Override
		public void OnReceiveByte(byte[] obj, int port,String ip) {
			// TODO Auto-generated method stub
			handleReceiveByte(obj, port,ip);
		}

		@Override
		public void onSendDataSuccess(byte[] b, int count) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetLocalAddress(String ip) {
			// TODO Auto-generated method stub

		}


		@Override
		public void OnReceiveByte(byte[] obj, int count) {
			// TODO Auto-generated method stub
			
		}

	}

	public void handleReceiveByte(byte[] b, int port,String ip) {

		for (int i = 0; i < b.length; i++) {

			byte data = b[i];
			switch (parseStatus) {
			case parseType.wait0xaa:
				if ((byte) 0xaa == data) {
					parseStatus = parseType.wait0x55;
					// System.out.println("------------wait0xaa b="+data);
				} else {
					//System.out.println("error 1");
					parseStatus = parseType.wait0xaa;
				}
				break;
			case parseType.wait0x55:
				if ((byte) 0x55 == data) {
					parseStatus = parseType.waitCmd;
					pD = new DataPackage();
					// System.out.println("------------wait0x55 b="+data);
				} else {
					//System.out.println("error 2");
					parseStatus = parseType.wait0xaa;
				}
				break;
			case parseType.waitCmd:
				//System.out.println("------------waitCmd b=" + data);
				pD.cmd = data;
				parseStatus = parseType.waitDataLen;
				break;
			case parseType.waitDataLen:
				pD.dataLen += (data << (pD.dataLengthStep * 8));
				pD.dataLengthStep--;
				// System.out.println("------------waitDataLen b="+data);
				if (pD.dataLengthStep < 0) {
					pD.data = new byte[pD.dataLen];
					parseStatus = parseType.waitData;
				}
				break;
			case parseType.waitData:
				// System.out.println("------------waitData b="+data);
				pD.data[pD.dataIndex++] = data;
				if (pD.dataIndex >= pD.dataLen) {
					parseStatus = parseType.waittail;
				}
				break;
			case parseType.waittail:
				if (data == (byte) 0xA5) {
					// System.out.println("------------waittail b="+data);
					parseStatus = parseType.dataready;
				} else {
					System.out.println("error 3");
					parseStatus = parseType.wait0xaa;
				}
				break;
			default:
				parseStatus = parseType.wait0xaa;
				break;
			}

		}
		if (parseStatus == parseType.dataready) {
			parseStatus = parseType.wait0xaa;
			Device d=new Device();
			d.ip=ip;
			d.parseDeviceInfo(pD.data);
			System.out.println("ip="+d.ip);
			if(newDeviceList!=null)
				addToList(d,newDeviceList);
		}
	}
	
    public void addToList(Device d,List<Device> list)
    {
        if (list.size() == 0)
        {
            list.add(d);
        }
        else { 
           for(Device di : list)
           {
              if(di.ip==d.ip)
              {
                  return;
              }

           }
           list.add(d);
        }
    }

}
