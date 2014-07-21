using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net.Sockets;
using System.Threading;
using System.Net;
using System.IO;

namespace wifiCameraServer
{
    class TcpS
    {
        public Socket srskt;
        public Socket clskt;
        public int port = 3333;
        public Thread athread;
        public NetworkStream ns = null;
        List<communication> cList = new List<communication>();
        //public LocalSave ls;
        public event onPictureDataHandler opdh;
        public event onConnectClient occ;
        private int index = 0;
        public TcpS()
        {
            srskt = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            IPAddress serverIp = GetServerIp();
            IPEndPoint server = new IPEndPoint(serverIp, port);
            //IPEndPoint server = new IPEndPoint(IPAddress.None, port);
            srskt.Bind(server);
            srskt.Listen(10);
            athread = new Thread(acceptThread);
            athread.Start();
        }

        void acceptThread()
        {
            while (true)
            {
                clskt = srskt.Accept();
                if (clskt != null)
                {
#if true
                    communication c = new communication(clskt, index);
                    c.opdh += new onPictureDataHandler(onGetPicture);
                    index++;
                    cList.Add(c);
#else
                    if (occ != null)
                    {
                        occ(clskt);
                    }
#endif
                }
            }
        }

        public void onGetPicture(int index,PictureData pd)
        {
            if (opdh != null)
            {
                opdh(index,pd);
            }
        }

        public IPAddress GetServerIp()
        {
            IPHostEntry eh = Dns.GetHostByName(Dns.GetHostName());
            return eh.AddressList[0];
        }

        public void stopNet()
        {
            athread.Abort();
            if(srskt!=null)
              srskt.Close();
           // if (clskt != null)           
           //     clskt.Close();
            //ls.closeFs();
            for (int i = 0; i < cList.Count; i++)
            {
                cList[i].stopCommunication();
            }
        }

    }
}
