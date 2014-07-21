using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net.Sockets;
using System.Net;
using System.Threading;

namespace wifiCameraServer
{
    public class DeviceDiscoverServer
    {
        public event onRefreshDeviceList ordl;
        private List<deviceInfo> deviceList = new List<deviceInfo>();
        private List<deviceInfo> newDeivceList=null;
        private Thread sndThread = null;
        private Thread recvThread = null;
        private Boolean runFlag = true;
        public String scanRange = "192.168.2.255";//

        public DeviceDiscoverServer()
        {
            deviceList.Clear();
            DiscoverDevice();
        }

        public void closeDeviceDiscoverServer()
        {
            runFlag = false;
            if (sndThread != null)
                sndThread.Abort();
            if (recvThread != null)
                recvThread.Abort();
        }

        public void reStartSendThread(string iprange)
        {
            scanRange = iprange;
            sndThread.Abort();
            sndThread = new Thread(new ThreadStart(sendThread));
            sndThread.IsBackground = true;
            sndThread.Start();
        }


        void sendThread()
        {

            UdpClient client = new UdpClient(new IPEndPoint(IPAddress.Any, 0));
            IPEndPoint endpoint = new IPEndPoint(IPAddress.Parse(scanRange), 8866);

            while (runFlag)
            {
                //refreshinfoList();
                //reportInfoList();
                reportList();
                newDeivceList = new List<deviceInfo>();
                byte[] buf = Encoding.Default.GetBytes("Hello from UDP broadcast");
                client.Send(buf, buf.Length, endpoint);
                Thread.Sleep(1000);
            }
        }

        public void reportList()
        {
            if (deviceList != null && newDeivceList != null)
            {
                if ((deviceList.Count == 0) && (newDeivceList.Count != 0))
                {
                    deviceList = newDeivceList;
                    if (ordl != null)
                    {
                        ordl(deviceList);
                    }
                }
                else
                {
                    if (needToFreshDeviceList(deviceList, newDeivceList))
                    {
                        if (ordl != null)
                        {
                            ordl(deviceList);
                        }
                    }
                }
                
            }
        }

        public Boolean needToFreshDeviceList(List<deviceInfo> list1, List<deviceInfo> list2)
        {
            Boolean flag = false;
            foreach (deviceInfo d1 in list2)
            {
                if (!isInList(d1, list1))
                {
                    list1.Add(d1);
                    flag = true;
                }
            }

            foreach (deviceInfo d1 in list1)
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

            List<deviceInfo> dList = new List<deviceInfo>();
            foreach (deviceInfo d1 in list1)
            {
                if (d1.maxMissNumber <= 0)
                {
                    flag = true;
                }
                else
                {
                    dList.Add(d1);
                }
            }
            deviceList = dList;
            return flag;
        }

        public Boolean isInList(deviceInfo d,List<deviceInfo> list)
        {
            if (list.Count == 0)
                return false;

            foreach (deviceInfo device in list)
            {
                if ((d.ip == device.ip) && (d.type==device.type) && (d.name==device.name)
                    &&(d.port==device.port))
                    return true;
            }
            return false;
        }
        public void reportInfoList()
        {
            if (deviceList != null && newDeivceList != null)
            {
                if ((deviceList.Count != 0) | (newDeivceList.Count != 0))
                {
                    if (deviceList.Count != newDeivceList.Count)
                    {
                        deviceList = newDeivceList;
                        if (ordl != null)
                        {
                            ordl(deviceList);
                        }
                    }
                    else
                    {
                        if (compareInfoList(deviceList, newDeivceList))
                        {
                            deviceList = newDeivceList;
                            if (ordl != null)
                            {
                                ordl(deviceList);
                            }
                        }
                    }
                }
            }
        
        }
        //如何两个表不一样，返回true，否则返回false
        private Boolean compareInfoList(List<deviceInfo> list1,List<deviceInfo> list2)
        {
            int Count = 0;
            foreach(deviceInfo info in list1)
            {
                foreach (deviceInfo info2 in list2)
                {
                    if (info.ip == info2.ip && info.name == info2.name)
                    {
                        Count++;
                    }
                }
            }
            if (Count != list1.Count) 
            {
                return true;
            }
            else
            {
                return false;
            }  
        } 

        private void DiscoverDevice()
        {
            recvThread = new Thread(new ThreadStart(RecvThread));
            recvThread.IsBackground = true;
            recvThread.Start();

            sndThread = new Thread(new ThreadStart(sendThread));
            sndThread.IsBackground = true;
            sndThread.Start();
        }

        void RecvThread()
        {
            UdpClient client = new UdpClient(new IPEndPoint(IPAddress.Any, 8866));
            IPEndPoint endpoint = new IPEndPoint(IPAddress.Any, 0);
            parseType satus = parseType.wait0xaa;
            PictureData pd = null;
            while (runFlag)
            {
                byte[] buf = client.Receive(ref endpoint);
                //string msg = Encoding.Default.GetString(buf);
                //int i=0;
                //refresh(msg+endpoint.Address);
                for (int i = 0; i < buf.Length; i++)
                {
                    byte data = buf[i];
                    switch (satus)
                    {
                        case parseType.wait0xaa:
                            if (data == 0xaa)
                            {
                                satus = parseType.wait0x55;
                                pd = new PictureData();
                            }
                            else
                            {
                                satus = parseType.wait0xaa;
                            }
                            break;
                        case parseType.wait0x55:
                            if (data == 0x55)
                            {
                                satus = parseType.waitCmd;
                            }
                            else
                            {
                                satus = parseType.wait0xaa;
                            }
                            break;
                        case parseType.waitCmd:
                            pd.cmd = data;
                            satus = parseType.waitdatalen;
                            break;
                        case parseType.waitdatalen:
                            pd.dataLength += (data << (pd.dataLengthstep * 8));
                            pd.dataLengthstep--;
                            if (pd.dataLengthstep < 0)
                            {
                                pd.data = new byte[pd.dataLength];
                                satus = parseType.waitdata;
                            }
                            break;
                        case parseType.waitdata:
                            pd.data[pd.dataIndex++] = data;
                            if (pd.dataIndex == pd.dataLength)
                            {
                                satus = parseType.waittail;
                            }
                            break;
                        case parseType.waittail:
                            if (data == 0xa5)
                            {
                                satus = parseType.dataready;
                            }
                            else
                            {
                                satus = parseType.wait0xaa;
                            }
                            break;
                        default:
                            satus = parseType.wait0xaa;
                            break;

                    }
                    if (satus == parseType.dataready)
                    {
                        satus = parseType.wait0xaa;
                        parseName(pd.data, endpoint.Address.ToString());
                    }
                }
            }
        }
        public void parseName(byte[] b, string ip)
        {
            //System.Text.UnicodeEncoding converter = new System.Text.UnicodeEncoding();
            //deviceInfo di = new deviceInfo(Encoding.Default.GetString(b), ip);
            deviceInfo di = new deviceInfo(ip);
            di.parseDeviceInfo(b);
            if (newDeivceList != null)
                addToList(di, newDeivceList);
        }

        public void addToList(deviceInfo d,List<deviceInfo> list)
        {
            if (list.Count == 0)
            {
                list.Add(d);
            }
            else { 
               foreach(deviceInfo di in list)
               {
                  if(di.ip==d.ip)
                  {
                      return;
                  }

               }
               list.Add(d);
            }
        }
    }
}
