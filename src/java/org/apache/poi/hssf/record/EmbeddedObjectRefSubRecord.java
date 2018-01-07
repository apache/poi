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

package org.apache.poi.hssf.record;

import java.io.ByteArrayInputStream;

import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPtg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.util.StringUtil;

/**
 * ftPictFmla (0x0009)<p>
 * A sub-record within the OBJ record which stores a reference to an object
 * stored in a separate entry within the OLE2 compound file.
 */
public final class EmbeddedObjectRefSubRecord extends SubRecord implements Cloneable {
	private static POILogger logger = POILogFactory.getLogger(EmbeddedObjectRefSubRecord.class);
	//arbitrarily selected; may need to increase
	private static final int MAX_RECORD_LENGTH = 100_000;

	public static final short sid = 0x0009;

	private static final byte[] EMPTY_BYTE_ARRAY = { };

	private int field_1_unknown_int;
	/** either an area or a cell ref */
	private Ptg field_2_refPtg;
	/** for when the 'formula' doesn't parse properly */
	private byte[] field_2_unknownFormulaData;
	/** note- this byte is not present in the encoding if the string length is zero */
	private boolean field_3_unicode_flag;  // Flags whether the string is Unicode.
	private String  field_4_ole_classname; // Classname of the embedded OLE document (e.g. Word.Document.8)
	/** Formulas often have a single non-zero trailing byte.
	 * This is in a similar position to he pre-streamId padding
	 * It is unknown if the value is important (it seems to mirror a value a few bytes earlier)
	 *  */
	private Byte  field_4_unknownByte;
	private Integer field_5_stream_id;	 // ID of the OLE stream containing the actual data.
	private byte[] field_6_unknown;


	// currently for testing only - needs review
	public EmbeddedObjectRefSubRecord() {
		field_2_unknownFormulaData = new byte[] { 0x02, 0x6C, 0x6A, 0x16, 0x01, }; // just some sample data.  These values vary a lot
		field_6_unknown = EMPTY_BYTE_ARRAY;
		field_4_ole_classname = null;
	}

	public short getSid() {
		return sid;
	}

	public EmbeddedObjectRefSubRecord(LittleEndianInput in, int size) {

		// Much guess-work going on here due to lack of any documentation.
		// See similar source code in OOO:
		// http://svn.services.openoffice.org/ooo/trunk/sc/source/filter/excel/xiescher.cxx
		// 1223 void XclImpOleObj::ReadPictFmla( XclImpStream& rStrm, sal_uInt16 nRecSize )

		int streamIdOffset = in.readShort(); // OOO calls this 'nFmlaLen'
		int remaining = size - LittleEndian.SHORT_SIZE;

		int dataLenAfterFormula = remaining - streamIdOffset;
		int formulaSize = in.readUShort();
		remaining -= LittleEndian.SHORT_SIZE;
		field_1_unknown_int = in.readInt();
		remaining -= LittleEndian.INT_SIZE;
		byte[] formulaRawBytes = readRawData(in, formulaSize);
		remaining -= formulaSize;
		field_2_refPtg = readRefPtg(formulaRawBytes);
		if (field_2_refPtg == null) {
			// common case
			// field_2_n16 seems to be 5 here
			// The formula almost looks like tTbl but the row/column values seem like garbage.
			field_2_unknownFormulaData = formulaRawBytes;
		} else {
			field_2_unknownFormulaData = null;
		}

		int stringByteCount;
		if (remaining >= dataLenAfterFormula + 3) {
			int tag = in.readByte();
			stringByteCount = LittleEndian.BYTE_SIZE;
			if (tag != 0x03) {
				throw new RecordFormatException("Expected byte 0x03 here");
			}
			int nChars = in.readUShort();
			stringByteCount += LittleEndian.SHORT_SIZE;
			if (nChars > 0) {
				 // OOO: the 4th way Xcl stores a unicode string: not even a Grbit byte present if length 0
				field_3_unicode_flag = ( in.readByte() & 0x01 ) != 0;
				stringByteCount += LittleEndian.BYTE_SIZE;
				if (field_3_unicode_flag) {
					field_4_ole_classname = StringUtil.readUnicodeLE(in, nChars);
					stringByteCount += nChars * 2;
				} else {
					field_4_ole_classname = StringUtil.readCompressedUnicode(in, nChars);
					stringByteCount += nChars;
				}
			} else {
				field_4_ole_classname = "";
			}
		} else {
			field_4_ole_classname = null;
			stringByteCount = 0;
		}
		remaining -= stringByteCount;
		// Pad to next 2-byte boundary
		if (((stringByteCount + formulaSize) % 2) != 0) {
			int b = in.readByte();
			remaining -= LittleEndian.BYTE_SIZE;
			if (field_2_refPtg != null && field_4_ole_classname == null) {
				field_4_unknownByte = Byte.valueOf((byte)b);
			}
		}
		int nUnexpectedPadding = remaining - dataLenAfterFormula;

		if (nUnexpectedPadding > 0) {
			logger.log( POILogger.ERROR, "Discarding " + nUnexpectedPadding + " unexpected padding bytes ");
			readRawData(in, nUnexpectedPadding);
			remaining-=nUnexpectedPadding;
		}

		// Fetch the stream ID
		if (dataLenAfterFormula >= 4) {
			field_5_stream_id = Integer.valueOf(in.readInt());
			remaining -= LittleEndian.INT_SIZE;
		} else {
			field_5_stream_id = null;
		}
		field_6_unknown = readRawData(in, remaining);
	}

	private static Ptg readRefPtg(byte[] formulaRawBytes) {
		LittleEndianInput in = new LittleEndianInputStream(new ByteArrayInputStream(formulaRawBytes));
		byte ptgSid = in.readByte();
		switch(ptgSid) {
			case AreaPtg.sid:   return new AreaPtg(in);
			case Area3DPtg.sid: return new Area3DPtg(in);
			case RefPtg.sid:	return new RefPtg(in);
			case Ref3DPtg.sid:  return new Ref3DPtg(in);
		}
		return null;
	}

	private static byte[] readRawData(LittleEndianInput in, int size) {
		if (size < 0) {
			throw new IllegalArgumentException("Negative size (" + size + ")");
		}
		if (size == 0) {
			return EMPTY_BYTE_ARRAY;
		}
		byte[] result = IOUtils.safelyAllocate(size, MAX_RECORD_LENGTH);
		in.readFully(result);
		return result;
	}

	private int getStreamIDOffset(int formulaSize) {
		int result = 2 + 4; // formulaSize + f2unknown_int
		result += formulaSize;

		int stringLen;
		if (field_4_ole_classname == null) {
			// don't write 0x03, stringLen, flag, text
			stringLen = 0;
		} else {
			result += 1 + 2;  // 0x03, stringLen
			stringLen = field_4_ole_classname.length();
			if (stringLen > 0) {
				result += 1; // flag
				if (field_3_unicode_flag) {
					result += stringLen * 2;
				} else {
					result += stringLen;
				}
			}
		}
		// pad to next 2 byte boundary
		if ((result % 2) != 0) {
			result ++;
		}
		return result;
	}

	private int getDataSize(int idOffset) {

		int result = 2 + idOffset; // 2 for idOffset short field itself
		if (field_5_stream_id != null) {
			result += 4;
		}
		return result +  field_6_unknown.length;
	}
	protected int getDataSize() {
		int formulaSize = field_2_refPtg == null ? field_2_unknownFormulaData.length : field_2_refPtg.getSize();
		int idOffset = getStreamIDOffset(formulaSize);
		return getDataSize(idOffset);
	}

	public void serialize(LittleEndianOutput out) {

		int formulaSize = field_2_refPtg == null ? field_2_unknownFormulaData.length : field_2_refPtg.getSize();
		int idOffset = getStreamIDOffset(formulaSize);
		int dataSize = getDataSize(idOffset);


		out.writeShort(sid);
		out.writeShort(dataSize);

		out.writeShort(idOffset);
		out.writeShort(formulaSize);
		out.writeInt(field_1_unknown_int);

		int pos = 12;

		if (field_2_refPtg == null) {
			out.write(field_2_unknownFormulaData);
		} else {
			field_2_refPtg.write(out);
		}
		pos += formulaSize;

		int stringLen;
		if (field_4_ole_classname == null) {
			// don't write 0x03, stringLen, flag, text
			stringLen = 0;
		} else {
			out.writeByte(0x03);
			pos+=1;
			stringLen = field_4_ole_classname.length();
			out.writeShort(stringLen);
			pos+=2;
			if (stringLen > 0) {
				out.writeByte(field_3_unicode_flag ? 0x01 : 0x00);
				pos+=1;

				if (field_3_unicode_flag) {
					StringUtil.putUnicodeLE(field_4_ole_classname, out);
					pos += stringLen * 2;
				} else {
					StringUtil.putCompressedUnicode(field_4_ole_classname, out);
					pos += stringLen;
				}
			}
		}

		// pad to next 2-byte boundary (requires 0 or 1 bytes)
		switch(idOffset - (pos - 6)) { // 6 for 3 shorts: sid, dataSize, idOffset
			case 1:
				out.writeByte(field_4_unknownByte == null ? 0x00 : field_4_unknownByte.intValue());
				pos++;
				break;
			case 0:
				break;
			default:
				throw new IllegalStateException("Bad padding calculation (" + idOffset + ", " + pos + ")");
		}

		if (field_5_stream_id != null) {
			out.writeInt(field_5_stream_id.intValue());
			pos += 4;
		}
		out.write(field_6_unknown);
	}

	/**
	 * Gets the stream ID containing the actual data.  The data itself
	 * can be found under a top-level directory entry in the OLE2 filesystem
	 * under the name "MBD<var>xxxxxxxx</var>" where <var>xxxxxxxx</var> is
	 * this ID converted into hex (in big endian order, funnily enough.)
	 *
	 * @return the data stream ID. Possibly <code>null</code>
	 */
	public Integer getStreamId() {
		return field_5_stream_id;
	}

	public String getOLEClassName() {
		return field_4_ole_classname;
	}

	public byte[] getObjectData() {
		return field_6_unknown;
	}

	@Override
	public EmbeddedObjectRefSubRecord clone() {
		return this; // TODO proper clone
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[ftPictFmla]\n");
		sb.append("    .f2unknown     = ").append(HexDump.intToHex(field_1_unknown_int)).append("\n");
		if (field_2_refPtg == null) {
			sb.append("    .f3unknown     = ").append(HexDump.toHex(field_2_unknownFormulaData)).append("\n");
		} else {
			sb.append("    .formula       = ").append(field_2_refPtg).append("\n");
		}
		if (field_4_ole_classname != null) {
			sb.append("    .unicodeFlag   = ").append(field_3_unicode_flag).append("\n");
			sb.append("    .oleClassname  = ").append(field_4_ole_classname).append("\n");
		}
		if (field_4_unknownByte != null) {
			sb.append("    .f4unknown   = ").append(HexDump.byteToHex(field_4_unknownByte.intValue())).append("\n");
		}
		if (field_5_stream_id != null) {
			sb.append("    .streamId      = ").append(HexDump.intToHex(field_5_stream_id.intValue())).append("\n");
		}
		if (field_6_unknown.length > 0) {
			sb.append("    .f7unknown     = ").append(HexDump.toHex(field_6_unknown)).append("\n");
		}
		sb.append("[/ftPictFmla]");
		return sb.toString();
	}
	
	public void setUnknownFormulaData(byte[] formularData) {
		field_2_unknownFormulaData = formularData;
	}
	
	public void setOleClassname(String oleClassname) {
		field_4_ole_classname = oleClassname;
	}
	
	public void setStorageId(int storageId) {
		field_5_stream_id = storageId;
	}
}
