using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace wifiCameraServer
{
    public partial class InfoShow : Form
    {
        public InfoShow()
        {
            InitializeComponent();
        }

        public void setInfo(deviceInfo info)
        {
            this.name.Text = info.name;
            this.ip.Text = info.ip;
            this.port.Text = info.port;
            this.type.Text = info.type+"";

        }
    }
}
