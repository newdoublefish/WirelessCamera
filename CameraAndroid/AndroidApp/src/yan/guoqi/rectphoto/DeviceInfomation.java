package yan.guoqi.rectphoto;

public class DeviceInfomation {
	public String name;
	public String port="8888";
	public int netType;
	public String ip;
	public String scanString="8866";
	public DeviceInfomation()
	{
		
	}

	public byte[] SerialDataPackage()
	{
		byte[] nameByte=TypeCovert.string2Byte(name, "GBK");
		byte[] rData=new byte[nameByte.length+3];
		rData[0]=(byte)netType;
		int portNum=Integer.parseInt(port);
		rData[1]=(byte)(portNum&0xff);
		rData[2]=(byte)((portNum>>8)&0xff);
		System.arraycopy(nameByte,0, rData, 3, nameByte.length);
		return rData;
	}
}
