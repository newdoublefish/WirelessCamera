using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.IO;

namespace wifiCameraServer
{
    
    class LocalSave
    {
        string fileName = Application.StartupPath + "\\savepath\\picture.dat";
        FileStream fs;
        StreamWriter sw;

        public LocalSave()
       {
           if (!File.Exists(fileName))
           {
               if (!File.Exists(fileName))
               {
                   fs = File.Create(fileName);
                   fs.Close();
               }
           }
           fs = File.Create(fileName);
           //sw = new StreamWriter(fs);


       }

        public void writeToFile(byte[] data,int length)
        {
            fs.Write(data, 0, length);
           // fs.w
        }

        public void closeFs()
        {
            fs.Close();
        }
    }
}
