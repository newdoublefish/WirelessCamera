using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.IO;

namespace wifiCameraServer
{
    public class PictureParse
    {
        public string fileName = Application.StartupPath + "\\savepath\\picture1.dat";
        public FileStream fs;
        public parseType satus = parseType.wait0xaa;
        public PictureData pd;
        public PictureParse()
        {
            fs = new FileStream(fileName, FileMode.Open);
        }

        public void parse()
        {
            int index=0;
            if (fs != null)
            {
                while (index < fs.Length)
                {
                    byte[] buffer = new byte[1024];
                    int count = fs.Read(buffer, 0, 1024);
                    for (int i = 0; i < count; i++)
                    {
                        DataHandle(buffer[i]);
                    }
                        index += count;

                }
            }
            fs.Close();
        }

        public void DataHandle(byte data)
        {
            switch (satus)
            {
                case parseType.wait0xaa:
                    if (data == 0xaa)
                    {
                        satus = parseType.wait0x55;
                        pd = new PictureData();
                    }
                    break;
                case parseType.wait0x55:
                    if (data == 0x55)
                    {
                        satus = parseType.waitdatalen;
                    }
                    break;
                case parseType.waitdatalen:
                    pd.dataLength += (data << (pd.dataLengthstep * 8));
                    pd.dataLengthstep--;
                    if (pd.dataLengthstep < 0)
                    {
                        pd.data = new byte[pd.dataLength];
                        satus = parseType.waitdata;
                    }
                    break;
                case parseType.waitdata:
                    pd.data[pd.dataIndex++] = data;
                    if (pd.dataIndex == pd.dataLength)
                    {
                        satus = parseType.waittail;
                    }
                    break;
                case parseType.waittail:
                    if (data == 0xa5)
                    {
                        satus = parseType.dataready;
                    }
                    break;
                default:
                    break;

            }
            if (satus == parseType.dataready)
            {
                //接受到完整数据包
                satus = parseType.wait0xaa;
            }

        }

    }
}
