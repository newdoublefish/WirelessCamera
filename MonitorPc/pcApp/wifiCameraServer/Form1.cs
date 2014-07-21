using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.IO;
using System.Net.Sockets;

namespace wifiCameraServer
{
    public partial class Form1 : Form
    {
        private Boolean open = false;
        TcpS tcps;
        public PictureParse p;
        public Boolean show = true;
        public Form1()
        {
            InitializeComponent();
            init();
            
        }

        private void init()
        {
            open = false;
            this.button1.Text = "开始监视";
        }
        private void button1_Click(object sender, EventArgs e)
        {
#if true
            if (!open)
            {
                tcps = new TcpS();
                tcps.opdh += new onPictureDataHandler(showPicture);
                tcps.occ+=new onConnectClient(onConnect);
                open = true;
                this.button1.Text = "停止监视";
            }else
            {
                this.button1.Text="开始监视";
                tcps.stopNet();
                open = false;
                
            }
#else
            p = new PictureParse();
            p.opdh+=new onPictureDataHandler(showPicture);

            p.parse();
#endif
        }

        public void onConnect(Socket socket)
        {
            ShowForm sf = new ShowForm(socket);
            sf.Show();

        }
        public void showPicture(int index,PictureData pd)
        {
            this.Invoke(new Action(delegate
            {
                MemoryStream ms = new MemoryStream(pd.data);
                Image image = Image.FromStream(ms);
                if (index == 0)
                    this.pictureBox1.Image = image;
                else if (index == 1)
                    this.pictureBox2.Image = image;
            }));
        }


    }
}
