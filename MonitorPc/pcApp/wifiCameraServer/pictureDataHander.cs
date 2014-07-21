using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net.Sockets;

namespace wifiCameraServer
{
        public delegate void onPictureDataHandler(int index,PictureData data);

        public delegate void onConnectClient(Socket socket);

        public delegate void onRefreshDeviceList(List<deviceInfo> deviceInfoList);

        public delegate void onScanRangeChange(String ip);
}
