package yan.guoqi.rectphoto;

public class DataPackage {
	public static final byte[] header={(byte) 0xAA,0x55};//头
	public byte cmd=0;//命令
	public int dataLen;
	public byte[] data;//数据
	public static final byte[] tail={(byte) 0xa5};//尾
    public int  dataLengthStep=3;
    public int dataIndex=0;
	public static byte[] int2bytes(int n) {
		byte[] b = new byte[4];
		int mask = 0xff;
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (n >> (24 - i * 8));
		}
		return b;
	}	
	
	public byte[] SerialDataPackage()
	{
		byte[] Buffer=new byte[2+1+4+dataLen+1];
		int index=0;
		System.arraycopy(header, 0, Buffer, index, 2);
		index+=2;//头
		Buffer[index]=cmd;
		index+=1;
		System.arraycopy(int2bytes(dataLen),0, Buffer, index, 4);
		index+=4;
		System.arraycopy(data, 0, Buffer, index, dataLen);
		index+=dataLen;
		System.arraycopy(tail, 0, Buffer, index, 1);
		index+=1;
		return Buffer;	
	}
/*
 * 协议头 AA55 2个字节
 * 命令字 1个字节
 * 包长   4个字节
 * 数据
 * 包尾  1个字节
 * 
 * 
 * */
}
