using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Net;

namespace wifiCameraServer
{
    public partial class  ScanRangeEdit : Form
    {     
        static String oldIp="";
        public ScanRangeEdit(String ip)
        {
            InitializeComponent();
            this.textBox1.Text = ip;
            oldIp = ip;
        }

        private void button1_Click(object sender, EventArgs e)
        {
            String scanRange = this.textBox1.Text.ToString();
            try
            {
                IPAddress.Parse(scanRange);
            }
            catch
            {
                MessageBox.Show("请输入正确的网段");
                this.textBox1.Text = oldIp;
                return;
            }
            oldIp = this.textBox1.Text;
            this.Close();
            
        }
    }
}
