using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace wifiCameraServer
{
    public enum parseType
    {
        wait0xaa=0,
        wait0x55=1,
        waitCmd=2,
        waitdatalen=3,
        waitdata=4,
        waittail=5,
        dataready=6
    }
}
