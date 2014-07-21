using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace wifiCameraServer
{
    public class deviceInfo
    {
        public string name;
        public string ip;
        public int maxMissNumber = 5;
        public string port = "8888";
        public netType type = netType.TCP;

        public deviceInfo(string n,string i)
        {
            this.name = n;
            this.ip = i;
        }

        public deviceInfo(string i)
        {
            this.ip = i;
        }

        public void parseDeviceInfo(byte[] data)
        {
            int length = data.Length;
            type = (netType)data[0];
            int portNum = data[2] << 8 | data[1];
            port = portNum + "";
            byte[] nameData = new byte[length-3];
            Array.Copy(data,3,nameData,0,nameData.Length);
            name = Encoding.Default.GetString(nameData);
        }
    }
}
