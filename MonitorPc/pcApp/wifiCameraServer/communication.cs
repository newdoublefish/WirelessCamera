using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net.Sockets;
using System.Threading;

namespace wifiCameraServer
{
    public class communication
    {
        private Socket socket;
        private Thread athread=null;
        public event onPictureDataHandler opdh;
        private int id=0;
        public communication(Socket s,int index) 
        {
            socket = s;
            id = index;
            athread = new Thread(recvThread);
            athread.Start();
        }

        public communication(Socket s)
        {
            socket = s;
            
            athread = new Thread(recvThread);
            athread.Start();
        }

        public void stopCommunication()
        {
            if (athread != null)
                athread.Abort();
            if (socket != null)
                socket.Close();
        }

        public void sendData(byte [] b)
        {
            socket.Send(b);

        }

        void recvThread()
        {
            byte[] recvBuffer = new byte[1024];
            parseType satus = parseType.wait0xaa;
            PictureData pd = null;
            //这里通知上一层有新的连接
            if (socket.Connected)
            {
                while (true)
                {
                    int receiveCount = socket.Receive(recvBuffer);

                    for (int i = 0; i < receiveCount; i++)
                    {
                        byte data = recvBuffer[i];
                        switch (satus)
                        {
                            case parseType.wait0xaa:
                                if (data == 0xaa)
                                {
                                    satus = parseType.wait0x55;
                                    pd = new PictureData();
                                }
                                else
                                {
                                    satus = parseType.wait0xaa;
                                }
                                break;
                            case parseType.wait0x55:
                                if (data == 0x55)
                                {
                                    satus = parseType.waitCmd;
                                }
                                else
                                {
                                    satus = parseType.wait0xaa;
                                }
                                break;
                            case parseType.waitCmd:
                                pd.cmd = data;
                                satus = parseType.waitdatalen;
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
                                else
                                {
                                    satus = parseType.wait0xaa;
                                }
                                break;
                            default:                          
                                    satus = parseType.wait0xaa;
                                 break;

                        }
                        if (satus == parseType.dataready)
                        {
                            satus = parseType.wait0xaa;
                            if (opdh != null)
                                opdh(id, pd);
                        }
                    }
                }
            }
        }
    }
}
