package com.camera.ray.wirelessmonitor;

public class DataPackage {
	public static final byte[] header={(byte) 0xAA,0x55};//ͷ
	public byte cmd=0;//����
	public int dataLen;
	public byte[] data;//����
	public static final byte[] tail={(byte) 0xa5};//β
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
		index+=2;//ͷ
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
 * Э��ͷ AA55 2���ֽ�
 * ������ 1���ֽ�
 * ����   4���ֽ�
 * ����
 * ��β  1���ֽ�
 * 
 * 
 * */
}
