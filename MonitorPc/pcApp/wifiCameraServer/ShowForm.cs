using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Net.Sockets;
using System.IO;
using System.Net;

namespace wifiCameraServer
{
    public partial class ShowForm : Form
    {
        private communication c=null;
        private communicationUdp cUdp = null;
        private EndPoint idasEp;
        private IPEndPoint ipep;
        private PictureBox pb = new PictureBox();
        private int zoomRate=0;
        private int MaxZoomRate=30;
        private Boolean close = false;
        private int recvCount = 0;
        
        public ShowForm(Socket s)
        {

        }
        public ShowForm(deviceInfo info)
        {

            InitializeComponent();
            if (info.type == netType.TCP)
            {
                Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
                try
                {
                    idasEp = new IPEndPoint(IPAddress.Parse(info.ip), int.Parse(info.port));
                }
                catch
                {
                    MessageBox.Show("IP地址错误");
                    return;
                }
                socket.Connect(idasEp);
                c = new communication(socket);
                c.opdh += new onPictureDataHandler(getPictureDate);
            }
            else
            {
                //ipep = new IPEndPoint(IPAddress.Any, int.Parse(port));
                //Socket udpSocket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
                //udpSocket.Bind(ipep);
                cUdp = new communicationUdp(info.ip, int.Parse(info.port));
                cUdp.opdh += new onPictureDataHandler(getPictureDate);
                


            }
            this.label2.Text = info.ip;
            this.label4.Text = "" + info.port;
            //this.progressBar1.Value = 100;
 
        }

        public void getPictureDate(int index,PictureData pd)
        {
            this.Invoke(new Action(delegate
            {
                MemoryStream ms = new MemoryStream(pd.data);
                Image image = Image.FromStream(ms);
                this.pictureBox1.Image = image;
                recvCount++;
                this.label8.Text = recvCount + "";

            }));
        }

        private void button1_Click(object sender, EventArgs e)
        {
            if(c!=null)
              c.stopCommunication();
            if (cUdp != null)
            {
                cUdp.stopCommunication();
            }
            this.Close();
        }

        private void button2_Click(object sender, EventArgs e)
        {
            zoomRate += 5;
            if (zoomRate >= 30)
                zoomRate = 30;
            byte[] b = { 0xaa, 0x55, 1, 0, 0, 0, 1, (byte)zoomRate, 0xa5 };
            c.sendData(b);
            setRateTextView(zoomRate);
        }

        private void button3_Click(object sender, EventArgs e)
        {
            zoomRate -= 5;
            if (zoomRate <= 0)
                zoomRate = 0;
            byte[] b = { 0xaa, 0x55, 1, 0, 0, 0, 1, (byte)zoomRate, 0xa5 };
            c.sendData(b);
            setRateTextView(zoomRate);
        }

        public void setRateTextView(int zoomRate)
        {
            String Ratestr="1.0";
            switch (zoomRate)
            { 
                case 0:
                    Ratestr="1.0";
                    break;
                case 5:
                    Ratestr = "1.5";
                    break;
                case 10:
                    Ratestr = "2.0";
                    break;
                case 15:
                    Ratestr = "2.5";
                    break;
                case 20:
                    Ratestr = "3.0";
                    break;
                case 25:
                    Ratestr = "3.5";
                    break;
                case 30:
                    Ratestr = "4.0";
                    break;
 
            }
            this.label6.Text = Ratestr;
        }


    }
}
