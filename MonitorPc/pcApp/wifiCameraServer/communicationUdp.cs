using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;
using System.Net.Sockets;
using System.Threading;

namespace wifiCameraServer
{
    public class communicationUdp
    {
        public IPEndPoint ipep;
        public Socket newSocket;
        Thread receiveThread;
        private bool flag;
        string sendAddress;
        int portN;
        IPEndPoint receiver;
        IPEndPoint sender;
        EndPoint remote;
        public event onPictureDataHandler opdh;
        int id = 0;

        public communicationUdp(String ip,int port)
        {
            sendAddress = ip;
            portN = port;
            ipep = new IPEndPoint(IPAddress.Any,portN);
            newSocket = new Socket(AddressFamily.InterNetwork,SocketType.Dgram,ProtocolType.Udp);
            newSocket.ReceiveBufferSize = 1024 * 1024;
            newSocket.Bind(ipep);
            receiveThread = new Thread(receiveData);
            flag = true;
            receiveThread.Start();
            receiver = new IPEndPoint(IPAddress.Parse(sendAddress), portN);
            sender = new IPEndPoint(IPAddress.Any,0);
            remote = (EndPoint)sender;
            byte[] start ={ 0xaa,0x55,0,0,0,0,0,0xa5};
            sendDataByByte(start,start.Length);
        }

        public void sendDataByByte(byte[] sendData,int sendLen)
        {
            newSocket.SendTo(sendData,sendLen,SocketFlags.None,receiver);
        }

        public void stopCommunication()
        {
            opdh = null;
            flag = false;
            
            if (receiveThread != null)
                receiveThread.Abort();
            if (newSocket != null)
            {
                newSocket.Close();
            }
        }
        public void receiveData()
        {
            byte[] receiveBuffer = new byte[4096];
            parseType satus = parseType.wait0xaa;
            PictureData pd = null;
            while (flag)
            {
                if (remote != null)
                {
                    int recevCount = newSocket.ReceiveFrom(receiveBuffer, ref remote);
                    for (int i = 0; i < recevCount; i++)
                    {
                        byte data = receiveBuffer[i];
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
