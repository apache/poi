/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hpbf.model.qcbits;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;


/**
 * A "PLC " (PLC) based bit of Quill Contents. The exact
 *  format is determined by the type of the PLCs.
 */
public abstract class QCPLCBit extends QCBit {
	protected int numberOfPLCs;
	protected int typeOfPLCS;
	/**
	 * The data which goes before the main PLC entries.
	 * This is apparently always made up of 2 byte
	 *  un-signed ints..
	 */
	protected int[] preData;
	/** The first value of each PLC, normally 4 bytes */
	protected long[] plcValA;
	/** The second value of each PLC, normally 4 bytes */
	protected long[] plcValB;


	private QCPLCBit(String thingType, String bitType, byte[] data) {
		super(thingType, bitType, data);

		// First four bytes are the number
		numberOfPLCs = (int)LittleEndian.getUInt(data, 0);

		// Next four bytes are the type
		typeOfPLCS = (int)LittleEndian.getUInt(data, 4);

		// Init the arrays that we can
		plcValA = new long[numberOfPLCs];
		plcValB = new long[numberOfPLCs];
	}



	public int getNumberOfPLCs() {
		return numberOfPLCs;
	}
	public int getTypeOfPLCS() {
		return typeOfPLCS;
	}

	public int[] getPreData() {
		return preData;
	}

	public long[] getPlcValA() {
		return plcValA;
	}
	public long[] getPlcValB() {
		return plcValB;
	}



	public static QCPLCBit createQCPLCBit(String thingType, String bitType, byte[] data) {
		// Grab the type
		int type = (int)LittleEndian.getUInt(data, 4);
		switch(type) {
			case 0:
				return new Type0(thingType, bitType, data);
			case 4:
				return new Type4(thingType, bitType, data);
			case 8:
				return new Type8(thingType, bitType, data);
			case 12: // 0xc
				return new Type12(thingType, bitType, data);
			default:
				throw new IllegalArgumentException("Sorry, I don't know how to deal with PLCs of type " + type);
		}
	}


	/**
	 * Type 0 seem to be somewhat rare. They have 8 bytes of pre-data,
	 *  then 2x 2 byte values.
	 */
	public static class Type0 extends QCPLCBit {
		private Type0(String thingType, String bitType, byte[] data) {
			super(thingType, bitType, data);

			// Grab our 4x pre-data
			preData = new int[4];
			preData[0] = LittleEndian.getUShort(data, 8+0);
			preData[1] = LittleEndian.getUShort(data, 8+2);
			preData[2] = LittleEndian.getUShort(data, 8+4);
			preData[3] = LittleEndian.getUShort(data, 8+6);

			// And grab the 2 byte values
			for(int i=0; i<numberOfPLCs; i++) {
				plcValA[i] = LittleEndian.getUShort(data, 16+(4*i));
				plcValB[i] = LittleEndian.getUShort(data, 16+(4*i)+2);
			}
		}
	}

	/**
	 * Type 4 is quite common. They have 8 bytes of pre-data,
	 *  then 2x 4 byte values.
	 */
	public static class Type4 extends QCPLCBit {
		private Type4(String thingType, String bitType, byte[] data) {
			super(thingType, bitType, data);

			// Grab our 4x pre-data
			preData = new int[4];
			preData[0] = LittleEndian.getUShort(data, 8+0);
			preData[1] = LittleEndian.getUShort(data, 8+2);
			preData[2] = LittleEndian.getUShort(data, 8+4);
			preData[3] = LittleEndian.getUShort(data, 8+6);

			// And grab the 4 byte values
			for(int i=0; i<numberOfPLCs; i++) {
				plcValA[i] = LittleEndian.getUInt(data, 16+(8*i));
				plcValB[i] = LittleEndian.getUInt(data, 16+(8*i)+4);
			}
		}
	}

	/**
	 * Type 8 is quite common. They have 14 bytes of pre-data,
	 *  then 2x 4 byte values.
	 */
	public static class Type8 extends QCPLCBit {
		private Type8(String thingType, String bitType, byte[] data) {
			super(thingType, bitType, data);

			// Grab our 7x pre-data
			preData = new int[7];
			preData[0] = LittleEndian.getUShort(data, 8+0);
			preData[1] = LittleEndian.getUShort(data, 8+2);
			preData[2] = LittleEndian.getUShort(data, 8+4);
			preData[3] = LittleEndian.getUShort(data, 8+6);
			preData[4] = LittleEndian.getUShort(data, 8+8);
			preData[5] = LittleEndian.getUShort(data, 8+10);
			preData[6] = LittleEndian.getUShort(data, 8+12);

			// And grab the 4 byte values
			for(int i=0; i<numberOfPLCs; i++) {
				plcValA[i] = LittleEndian.getUInt(data, 22+(8*i));
				plcValB[i] = LittleEndian.getUInt(data, 22+(8*i)+4);
			}
		}
	}

	/**
	 * Type 12 holds hyperlinks, and is very complex.
	 * There is normally one of these for each text
	 *  area that contains at least one hyperlinks.
	 * The character offsets are relative to the start
	 *  of the text area that this applies to.
	 */
	public static class Type12 extends QCPLCBit {
		private String[] hyperlinks;

		private static final int oneStartsAt = 0x4c;
		private static final int twoStartsAt = 0x68;
		private static final int threePlusIncrement = 22;

		private Type12(String thingType, String bitType, byte[] data) {
			super(thingType, bitType, data);

			// How many hyperlinks do we really have?
			// (zero hyperlinks gets numberOfPLCs=1)
			if(data.length == 0x34) {
				hyperlinks = new String[0];
			} else {
				hyperlinks = new String[numberOfPLCs];
			}

			// We have 4 bytes, then the start point of each
			//  hyperlink, then the end point of the text.
			preData = new int[1+numberOfPLCs+1];
			for(int i=0; i<preData.length; i++) {
				preData[i] = (int)LittleEndian.getUInt(data, 8+(i*4));
			}

			// Then we have a whole bunch of stuff, which grows
			//  with the number of hyperlinks
			// For now, we think these are shorts
			int at = 8+4+(numberOfPLCs*4)+4;
			int until = 0x34;
			if(numberOfPLCs == 1 && hyperlinks.length == 1) {
				until = oneStartsAt;
			} else if(numberOfPLCs >= 2) {
				until = twoStartsAt + (numberOfPLCs-2)*threePlusIncrement;
			}

			plcValA = new long[(until-at)/2];
			plcValB = new long[0];
			for(int i=0; i<plcValA.length; i++) {
				plcValA[i] = LittleEndian.getUShort(data, at+(i*2));
			}

			// Finally, we have a series of lengths + hyperlinks
			at = until;
			for(int i=0; i<hyperlinks.length; i++) {
				int len = LittleEndian.getUShort(data, at);
				int first = LittleEndian.getUShort(data, at+2);
				if(first == 0) {
					// Crazy special case
					// Length is in bytes, from the start
					// Hyperlink appears to be empty
					hyperlinks[i] = "";
					at += len;
				} else {
					// Normal case. Length is in characters
					hyperlinks[i] = StringUtil.getFromUnicodeLE(data, at+2, len);
					at += 2 + (2*len);
				}
			}
		}

		/**
		 * Returns the number of hyperlinks, which should
		 *  either be zero, or the number of PLC bits
		 */
		public int getNumberOfHyperlinks() {
			return hyperlinks.length;
		}

		/**
		 * Returns the URL of the hyperlink at the
		 *  given index.
		 * @param number The hyperlink number, zero based
		 */
		public String getHyperlink(int number) {
			return hyperlinks[number];
		}
		/**
		 * Returns where in the text (in characters) the
		 *  hyperlink at the given index starts
		 *  applying to.
		 * This position is relative to the text area that this
		 *  PLCBit applies to.
		 * @param number The hyperlink number, zero based
		 */
		public int getTextStartAt(int number) {
			return preData[1+number];
		}
		/**
		 * Returns where in the text that this block
		 *  of hyperlinks stops applying to. Normally,
		 *  but not always the end of the text.
		 * This position is relative to the text area that this
		 *  PLCBit applies to.
		 */
		public int getAllTextEndAt() {
			return preData[numberOfPLCs+1];
		}
	}
}
