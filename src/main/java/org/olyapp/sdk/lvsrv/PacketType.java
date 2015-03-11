package org.olyapp.sdk.lvsrv;

import org.olyapp.sdk.utils.StringUtils;

public enum PacketType {

	START(0x9060) {
		@Override
		public int getImageStartingPint(byte[] data, int length) throws InvalidPacket {
			return findBytes(0xFF,0xD8,data,length,this);
		}

		@Override
		public int getImageEndingPint(byte[] data, int length) {
			return length;
		}

	}, 
	
	
	MIDDLE(0x8060) {
		@Override
		public int getImageStartingPint(byte[] data, int length) {
			return 12;
		}

		@Override
		public int getImageEndingPint(byte[] data, int length) {
			return length;
		}
	}, 
	
	
	END(0x80e0) {
		@Override
		public int getImageStartingPint(byte[] data, int length) {
			return 12;
		}

		@Override
		public int getImageEndingPint(byte[] data, int length) throws InvalidPacket {
			return findBytes(0xFF,0xD9,data,length,this)+2;
		}
	};
	
	private static int findBytes(int value1, int value2, byte[] data, int length, PacketType type) throws InvalidPacket {
		int startingPoint = -1;
		for (int i = 0 ; i<length && startingPoint==-1; i++) {
			if (i+1<length && (data[i] & 0xFF)==value1  && (data[i+1] & 0xff)==value2 ) {
				startingPoint = i;
			}
		}
		if (startingPoint==-1) {
			throw new InvalidPacket(type + " packet contains no " + StringUtils.toHex((value1<<8) | value2));
		}
		return startingPoint; // throw exception if -1
	}
	
	int code;

	int startingPoint = -1;
	int endingPoint = -1;
	
	
	PacketType(int code) {
		this.code = code;
	}
	
	int getCode() {
		return code;
	}
	
	boolean isType(byte[] data) {
		return (((data[0] & 0xff) << 8) | (data[1] & 0xff)) ==getCode();
	}
	
	public static PacketType parseCode(int type) throws InvalidPacket {
		PacketType ret = null;
		for (PacketType eType : values()) {
			if (eType.getCode()==type) {
				ret = eType;
			}
		}
		if (ret==null) {
			throw new InvalidPacket("Unknown type of packet: " + StringUtils.toHex(type));
		}
		return ret;
	}

	public abstract int getImageStartingPint(byte[] data, int length) throws InvalidPacket;
	public abstract int getImageEndingPint(byte[] data, int length) throws InvalidPacket;
	
}
