package yan.guoqi.rectphoto;

import java.io.UnsupportedEncodingException;
/*
 * 类型转换
 * 
 * 
 * */

public class TypeCovert {

	public TypeCovert(byte b, int p) {

	}

	public static int bitLeftMove(byte b, int p) {
		int a = 0;
		a = b & 0xff;
		a <<= p;
		return a;
	}

	public static Long bitLeftMoveReturnLong(byte b, int p) {
		Long a = (long) 0;
		a = (long) (b & 0xff);
		a <<= p;
		return a;
	}

	public static int byte2int(byte[] b) {
		int mask = 0xff;
		int temp = 0;
		int res = 0;
		for (int i = 0; i < 4; i++) {
			res <<= 8;
			temp = b[i] & mask;
			res += temp;
		}
		return res;
	}

	public static byte[] int2bytes(int n) {
		byte[] b = new byte[4];
		int mask = 0xff;
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (n >> (24 - i * 8));
		}
		return b;
	}

	/*
	 * Long型（内存中，所有的处理器都一样，无所谓大端小端）
	 * 
	 * 高地址 低地址 __ __ __ __ __ __ __ __ ___________________________ __ __ __ __
	 * __ __ __ __ ____________________________
	 * 
	 * 高位 低位
	 * 
	 * 存储的情况（这个有所谓大端小端）
	 * 
	 * 低地址 高地址 高位 低位
	 * 
	 * 问题的关键在于，存储数据的情况
	 */
	public static byte[] Long2bytes(Long n) {
		byte[] b = new byte[8];
		int mask = 0xff;
		for (int i = 0; i < 8; i++) {
			b[i] = (byte) (n >> (56 - i * 8)); // 右移动，实际上应该叫做高位移向低位，
												// 为啥要将高位放在前面，低位放在后面，存储的时候，不如writebyte的时候，按此顺序存储，读的时候，readLong的时候按大端来读的。
		}
		return b;
	}

	public static byte[] string2Byte(String s, String codeType) {

		byte[] b = null;
		if (codeType == "GBK") {
			try {
				b = s.getBytes("GBK");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (codeType == "UTF-8") {
			try {
				b = s.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (codeType == "UTF-16LE") {
			try {
				b = s.getBytes("UTF-16LE");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return b;
	}

	public static String byte2string(byte[] b, String codeType) {
		String s = null;
		if (codeType == "GBK") {
			try {
				s = new String(b, "GBK");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (codeType == "UTF-8") {
			try {
				s = new String(b, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (codeType == "UTF-16LE") {
			try {
				s = new String(b, "UTF-16LE");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return s;
	}

	public static String byte2string(byte[] b, String codeType, int length) {
		byte[] t = new byte[length];
		System.arraycopy(b, 0, t, 0, t.length);
		String s = null;
		if (codeType == "GBK") {
			try {
				s = new String(t, "GBK");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (codeType == "UTF-8") {
			try {
				s = new String(t, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (codeType == "UTF-16LE") {
			try {
				s = new String(t, "UTF-16LE");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return s;
	}

	public static byte[] Byte4to2(byte[] bVlue) {
		// TODO Auto-generated method stub
		byte[] a = new byte[2];
		a[0] = bVlue[2];
		a[1] = bVlue[3];
		return a;
	}

	public static byte[] Byte4to1(byte[] bVlue) {
		// TODO Auto-generated method stub
		byte[] a = new byte[1];
		a[0] = bVlue[3];
		return a;
	}

	public static byte[] Byte8to4(byte[] bVlue) {
		byte[] a = new byte[4];
		a[0] = bVlue[4];
		a[1] = bVlue[5];
		a[2] = bVlue[6];
		a[3] = bVlue[7];
		return a;
	}

	public static byte[] Byte8to2(byte[] bVlue) {
		byte[] a = new byte[2];
		a[0] = bVlue[6];
		a[1] = bVlue[7];
		return a;
	}

	public static byte[] Byte8to1(byte[] bVlue) {
		byte[] a = new byte[1];
		a[0] = bVlue[7];
		return a;
	}

	public static Long int2Long(int i) {
		Long mask = Long.parseLong("4294967295");
		Long r = (long) i & mask;
		return r;

	}

	public static int short2int(short i) {
		int mask = 0xffff;
		int r = i & mask;
		return r;
	}

	public static Long byte2Long(byte[] bVlue) {

		Long mask = (long) 0xff;
		long temp;
		Long r = (long) 0;
		for (int i = 0; i < 8; i++) {
			r <<= 8;
			temp = bVlue[i] & mask;
			r += temp;
		}
		return r;
	}

	public static int LongToInt(Long num) {
		// TODO Auto-generated method stub
		long n = num;
		int a = (int) n;
		return a;
	}
}
