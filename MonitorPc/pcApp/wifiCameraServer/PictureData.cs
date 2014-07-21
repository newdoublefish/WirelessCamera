using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace wifiCameraServer
{
    public class PictureData
    {
        public int dataLength=0;
        public byte[] data;
        public byte cmd;
        public int dataLengthstep = 3;
        public int dataIndex = 0;
    }
}
