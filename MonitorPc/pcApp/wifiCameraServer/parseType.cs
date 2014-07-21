using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace wifiCameraServer
{
    enum parseType
    {
        wait0xaa=0,
        wait0x55=1,
        waitdatalen=2,
        waitdata=3,
        waittail=4,
        dataready=5
    }
}
