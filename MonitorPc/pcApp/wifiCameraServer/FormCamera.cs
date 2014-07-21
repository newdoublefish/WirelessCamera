using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Drawing.Drawing2D;

namespace wifiCameraServer
{
    public partial class FormCameara : Form
    {
        private int btnFlag = 0;
        ListView listView1 = new ListView();
        private List<deviceInfo> infoList = new List<deviceInfo>();
        DeviceDiscoverServer dds;
        public FormCameara()
        {
            InitializeComponent();
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            initForm();
            initListView();
            //起动扫描  
            dds = new DeviceDiscoverServer();
            dds.ordl += new onRefreshDeviceList(reFreshListBox); 
        }
        private void reFreshListBox(List<deviceInfo> list)
        {
            this.Invoke(new Action(delegate
            {
                listView1.Clear(); 
                infoList = list;
                if (list.Count > 0)
                {
                    int index = 0;
                    foreach (deviceInfo di in list)
                    {
                        //this.listBox1.Items.Add(di.name + ":" + di.ip);
                        ListViewItem item = new ListViewItem();
                        item.Text =di.name;
                        item.Tag = index++;
                        item.ImageIndex = 0;
                        listView1.Items.Add(item);
                    }
                }
                else
                {
                    listView1.Clear(); 
                }
                  
                /*for (int i = 0; i < 5; i++)
                {
                    ListViewItem item = new ListViewItem();
                    //item.Tag = (object)key;//-----------------
                    item.Text = "" + i;
                    item.ImageIndex = 0;
                    //

                    listView1.Items.Add(item);
                }*/
            }));
        }

        
        private void initForm()
        {
            this.FormClosing += new FormClosingEventHandler(Form1_FormClosing);
            string strImagePath = Application.StartupPath.Substring(0) + "\\images";
            this.Icon = System.Drawing.Icon.ExtractAssociatedIcon(strImagePath + "\\ico.ico");
            this.imageListPicture.Images.Add("0", Image.FromFile(strImagePath + "\\camera_single.png"));
            this.toolStripButtonSetting.Image = Image.FromFile(strImagePath + "\\desktop.png");
            this.toolStripButtonQuit.Image = Image.FromFile(strImagePath + "\\exit.ico");
            this.Width = 500;
            this.Height = 500;
          
        }

        private void initListView()
        {
            this.listView1.ItemSelectionChanged += new System.Windows.Forms.ListViewItemSelectionChangedEventHandler(this.listView1_ItemSelectionChanged);
            this.listView1.DoubleClick+=new EventHandler(listView1_DoubleClick);
            splitContainer1.Panel2.Controls.Add(listView1);
            listView1.MultiSelect = false;
            listView1.FullRowSelect = true;
           // listView1.ContextMenuStrip = this.contextMenuStrip1;

            listView1.BackgroundImageTiled = true;
            //listView1.Sorting = SortOrder.Ascending;
            listView1.HideSelection = false;
            listView1.Dock = DockStyle.Fill;
            listView1.Clear();

            listView1.Dock = DockStyle.Fill;
            listView1.LargeImageList = this.imageListPicture;
              
        }

        private void listView1_DoubleClick(object sender, EventArgs e)
        {
            ListViewItem li;
            li=listView1.FocusedItem;
            int i = (int)li.Tag;
            //MessageBox.Show(i+"");
           // ShowForm sf = new ShowForm(textBox1.Text.ToString(), textBox2.Text.ToString(), netType.UDP);
           // sf.Show();
            ShowForm sf = new ShowForm(infoList[i]);
            sf.Show();
        }

        private void contextMenuStrip1_Paint(object sender, PaintEventArgs e)
        {
            Graphics g = e.Graphics;
            Color FColor = Color.White;
            Color TColor = Color.White;
            Brush b = new LinearGradientBrush(this.contextMenuStrip1.ClientRectangle, FColor, TColor, LinearGradientMode.ForwardDiagonal);
            g.FillRectangle(b, this.contextMenuStrip1.ClientRectangle);
        }
        private void listView1_ItemSelectionChanged(object sender, ListViewItemSelectionChangedEventArgs e)
        {

            if (listView1.SelectedItems.Count > 0)
            {
                listView1.ContextMenuStrip = this.contextMenuStrip1;
            }
            else//未选中项
            {
                listView1.ContextMenuStrip = null;

            }
        }

        private void toolStripButtonSetting_Click(object sender, EventArgs e)
        {
            Form scanForm = new ScanRangeEdit(dds.scanRange);
            scanForm.Show();
        }



        private void toolStripButtonQuit_Click(object sender, EventArgs e)
        {
            dds.closeDeviceDiscoverServer();
            Application.Exit();
            this.Dispose();
        }

        private void 摄像头信息ToolStripMenuItem_Click(object sender, EventArgs e)
        {
            ListViewItem li;
            li = listView1.FocusedItem;
            int index = (int)li.Tag;
            InfoShow infoShow=new InfoShow();
            infoShow.Show();
            infoShow.setInfo(infoList[index]);
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            dds.closeDeviceDiscoverServer();
        }
    }
}
