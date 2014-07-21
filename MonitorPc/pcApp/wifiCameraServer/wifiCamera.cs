using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Net.Sockets;
using System.Net;
using System.Threading;
using System.IO;
using System.Net.NetworkInformation;

namespace wifiCameraServer
{
    public partial class wifiCamera : Form
    {
        private EndPoint idasEp;
        List<communication> cList;
        private DataTable ds;
        private List<string> ipList=new List<string>();
        private List<deviceInfo> infoList = new List<deviceInfo>();
        DeviceDiscoverServer dds;
        private String ipRange="";
        public wifiCamera()
        {

            InitializeComponent();
            this.textBox3.Enabled = false;
            this.button2.Text = "修改";
            this.FormClosing += new FormClosingEventHandler(Form1_FormClosing);
            cList = new List<communication>();
            dds = new DeviceDiscoverServer();
            dds.ordl += new onRefreshDeviceList(reFreshListBox);
        }

        private void reFreshListBox(List<deviceInfo> list)
        {
            this.Invoke(new Action(delegate
            {
                infoList = list;
                this.listBox1.Items.Clear();
                if (list.Count > 0)
                {
                    foreach (deviceInfo di in list)
                    {
                        this.listBox1.Items.Add(di.name + ":" + di.ip);
                    }
                }
                else
                {
                    this.listBox1.Items.Clear();
                }
            }));           
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            dds.closeDeviceDiscoverServer();
        }
        private void EnumComputers()
        {
            ipList.Clear();
            try
            {
                for (int i = 1; i <= 255; i++)
                {
                    Ping myPing;
                    myPing = new Ping();
                    myPing.PingCompleted += new PingCompletedEventHandler(_myPing_PingCompleted);

                    string pingIP = "192.168.2." + i.ToString();
                    myPing.SendAsync(pingIP, 2000, null);
                }
            }
            catch
            {
            }
        }

        private void _myPing_PingCompleted(object sender, PingCompletedEventArgs e)
        { 
             if (e.Reply.Status == IPStatus.Success)
             {
                 string ip = e.Reply.Address.ToString();
                 this.listBox1.Items.Add(ip);
                 ipList.Add(ip);
                 textBox2.Text = "8888";
              }
  
        }
        public void fillDataSet()
        { 
           //ds.Columns.Add("ip");
           //DataRow dr=ds.NewRow();
           //dr["ip"]="192.168.1.1";
           ///ds.Rows.Add(dr);
           //this.listBox1.DataSource = ds;
            this.listBox1.Items.Add("192.168.1.1");
            this.listBox1.Items.Add("192.168.1.1");
            this.listBox1.Items.Add("192.168.1.1");
            this.listBox1.Items.Add("192.168.1.1");
            this.listBox1.Items.Add("192.168.1.1");

        }

        private void button1_Click(object sender, EventArgs e)
        {

            if(this.checkBox2.Checked)
            {
               //ShowForm sf = new ShowForm(textBox1.Text.ToString(),textBox2.Text.ToString(),netType.UDP);
               //sf.Show();
            }
            else if (this.checkBox1.Checked)
            {
                //ShowForm sf = new ShowForm(textBox1.Text.ToString(), textBox2.Text.ToString(), netType.TCP);
               // sf.Show();
            }

            
        }



        private void checkBox1_CheckedChanged(object sender, EventArgs e)
        {
            if (this.checkBox1.Checked)
            {
                this.checkBox2.Checked = false;
            }
        }

        private void checkBox2_CheckedChanged(object sender, EventArgs e)
        {
            if (this.checkBox2.Checked)
            {
                this.checkBox1.Checked = false;
            }
        }

        private void listBox1_SelectedIndexChanged(object sender, EventArgs e)
        {
            //this.textBox1.Text = ipList[this.listBox1.SelectedIndex];
            //MessageBox.Show(ipList[this.listBox1.SelectedIndex]);
            if(this.listBox1.SelectedIndex>=0)
              this.textBox1.Text = infoList[this.listBox1.SelectedIndex].ip;
        }


        private void wifiCamera_Load(object sender, EventArgs e)
        {

        }

        private void button2_Click(object sender, EventArgs e)
        {
            if (this.button2.Text.ToString() == "修改")
            {
                this.button2.Text = "确定";
                this.textBox3.Enabled = true;
                this.ipRange = this.textBox3.Text.ToString();
            }
            else //sure
            {
                this.button2.Text = "修改";
                
                string scanRange = this.textBox3.Text.ToString();
                try
                {
                    IPAddress.Parse(scanRange);
                }
                catch
                {
                    MessageBox.Show("请输入正确的网段");
                    this.textBox3.Text = this.ipRange;
                    this.textBox3.Enabled = false;
                    return;
                }
                this.textBox3.Enabled = false;
                this.ipRange = this.textBox3.Text.ToString();
                dds.reStartSendThread(scanRange);
            }                
        }
    }
}
