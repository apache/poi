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

import java.io.UnsupportedEncodingException;

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

/**
 * The TXO record (0x01B6) is used to define the properties of a text box. It is
 * followed by two or more continue records unless there is no actual text. The
 * first continue records contain the text data and the last continue record
 * contains the formatting runs.<p/>
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TextObjectRecord extends Record {
	public final static short sid = 0x01B6;

	private static final int FORMAT_RUN_ENCODED_SIZE = 8; // 2 shorts and 4 bytes reserved

	private static final BitField HorizontalTextAlignment = BitFieldFactory.getInstance(0x000E);
	private static final BitField VerticalTextAlignment = BitFieldFactory.getInstance(0x0070);
	private static final BitField textLocked = BitFieldFactory.getInstance(0x0200);

	public final static short HORIZONTAL_TEXT_ALIGNMENT_LEFT_ALIGNED = 1;
	public final static short HORIZONTAL_TEXT_ALIGNMENT_CENTERED = 2;
	public final static short HORIZONTAL_TEXT_ALIGNMENT_RIGHT_ALIGNED = 3;
	public final static short HORIZONTAL_TEXT_ALIGNMENT_JUSTIFIED = 4;
	public final static short VERTICAL_TEXT_ALIGNMENT_TOP = 1;
	public final static short VERTICAL_TEXT_ALIGNMENT_CENTER = 2;
	public final static short VERTICAL_TEXT_ALIGNMENT_BOTTOM = 3;
	public final static short VERTICAL_TEXT_ALIGNMENT_JUSTIFY = 4;

	public final static short TEXT_ORIENTATION_NONE = 0;
	public final static short TEXT_ORIENTATION_TOP_TO_BOTTOM = 1;
	public final static short TEXT_ORIENTATION_ROT_RIGHT = 2;
	public final static short TEXT_ORIENTATION_ROT_LEFT = 3;

	private int field_1_options;
	private int field_2_textOrientation;
	private int field_3_reserved4;
	private int field_4_reserved5;
	private int field_5_reserved6;
	private int field_8_reserved7;

	private HSSFRichTextString _text;

	/*
	 * Note - the next three fields are very similar to those on
	 * EmbededObjectRefSubRecord(ftPictFmla 0x0009)
	 * 
	 * some observed values for the 4 bytes preceding the formula: C0 5E 86 03
	 * C0 11 AC 02 80 F1 8A 03 D4 F0 8A 03
	 */
	private int _unknownPreFormulaInt;
	/** expect tRef, tRef3D, tArea, tArea3D or tName */
	private Ptg _linkRefPtg;
	/**
	 * Not clear if needed .  Excel seems to be OK if this byte is not present. 
	 * Value is often the same as the earlier firstColumn byte. */
	private Byte _unknownPostFormulaByte;

	public TextObjectRecord() {
	}

	public TextObjectRecord(RecordInputStream in) {
		field_1_options = in.readUShort();
		field_2_textOrientation = in.readUShort();
		field_3_reserved4 = in.readUShort();
		field_4_reserved5 = in.readUShort();
		field_5_reserved6 = in.readUShort();
		int field_6_textLength = in.readUShort();
		int field_7_formattingDataLength = in.readUShort();
		field_8_reserved7 = in.readInt();

		if (in.remaining() > 0) {
			// Text Objects can have simple reference formulas
			// (This bit not mentioned in the MS document)
			if (in.remaining() < 11) {
				throw new RecordFormatException("Not enough remaining data for a link formula");
			}
			int formulaSize = in.readUShort();
			_unknownPreFormulaInt = in.readInt();
			Ptg[] ptgs = Ptg.readTokens(formulaSize, in);
			if (ptgs.length != 1) {
				throw new RecordFormatException("Read " + ptgs.length
						+ " tokens but expected exactly 1");
			}
			_linkRefPtg = ptgs[0];
			if (in.remaining() > 0) {
				_unknownPostFormulaByte = new Byte(in.readByte());
			} else {
				_unknownPostFormulaByte = null;
			}
		} else {
			_linkRefPtg = null;
		}
		if (in.remaining() > 0) {
			throw new RecordFormatException("Unused " + in.remaining() + " bytes at end of record");
		}

		String text;
		if (field_6_textLength > 0) {
			text = readRawString(in, field_6_textLength);
		} else {
			text = "";
		}
		_text = new HSSFRichTextString(text);

		if (field_7_formattingDataLength > 0) {
			if (in.isContinueNext() && in.remaining() == 0) {
				in.nextRecord();
				processFontRuns(in, _text, field_7_formattingDataLength);
			} else {
				throw new RecordFormatException(
						"Expected Continue Record to hold font runs for TextObjectRecord");
			}
		}
	}

	private static String readRawString(RecordInputStream in, int textLength) {
		byte compressByte = in.readByte();
		boolean isCompressed = (compressByte & 0x01) == 0;
		if (isCompressed) {
			return in.readCompressedUnicode(textLength);
		}
		return in.readUnicodeLEString(textLength);
	}

	private static void processFontRuns(RecordInputStream in, HSSFRichTextString str,
			int formattingRunDataLength) {
		if (formattingRunDataLength % FORMAT_RUN_ENCODED_SIZE != 0) {
			throw new RecordFormatException("Bad format run data length " + formattingRunDataLength
					+ ")");
		}
		if (in.remaining() != formattingRunDataLength) {
			throw new RecordFormatException("Expected " + formattingRunDataLength
					+ " bytes but got " + in.remaining());
		}
		int nRuns = formattingRunDataLength / FORMAT_RUN_ENCODED_SIZE;
		for (int i = 0; i < nRuns; i++) {
			short index = in.readShort();
			short iFont = in.readShort();
			in.readInt(); // skip reserved.
			str.applyFont(index, str.length(), iFont);
		}
	}

	public short getSid() {
		return sid;
	}

	/**
	 * Only for the current record. does not include any subsequent Continue
	 * records
	 */
	private int getDataSize() {
		int result = 2 + 2 + 2 + 2 + 2 + 2 + 2 + 4;
		if (_linkRefPtg != null) {
			result += 2 // formula size
				+ 4  // unknownInt
				+_linkRefPtg.getSize();
			if (_unknownPostFormulaByte != null) {
				result += 1;
			}
		}
		return result;
	}

	private int serializeTXORecord(int offset, byte[] data) {
		int dataSize = getDataSize();
		
		LittleEndian.putUShort(data, 0 + offset, TextObjectRecord.sid);
		LittleEndian.putUShort(data, 2 + offset, dataSize);

		
		LittleEndian.putUShort(data, 4 + offset, field_1_options);
		LittleEndian.putUShort(data, 6 + offset, field_2_textOrientation);
		LittleEndian.putUShort(data, 8 + offset, field_3_reserved4);
		LittleEndian.putUShort(data, 10 + offset, field_4_reserved5);
		LittleEndian.putUShort(data, 12 + offset, field_5_reserved6);
		LittleEndian.putUShort(data, 14 + offset, _text.length());
		LittleEndian.putUShort(data, 16 + offset, getFormattingDataLength());
		LittleEndian.putInt(data, 18 + offset, field_8_reserved7);
		
		if (_linkRefPtg != null) {
			int pos = offset+22;
			int formulaSize = _linkRefPtg.getSize();
			LittleEndian.putUShort(data, pos, formulaSize);
			pos += LittleEndian.SHORT_SIZE;
			LittleEndian.putInt(data, pos, _unknownPreFormulaInt);
			pos += LittleEndian.INT_SIZE;
			_linkRefPtg.writeBytes(data, pos);
			pos += formulaSize;
			if (_unknownPostFormulaByte != null) {
				LittleEndian.putByte(data, pos, _unknownPostFormulaByte.byteValue());
				pos += LittleEndian.BYTE_SIZE;
			}
		}
		
		return 4 + dataSize;
	}

	private int serializeTrailingRecords(int offset, byte[] data) {
		byte[] textBytes;
		try {
			textBytes = _text.getString().getBytes("UTF-16LE");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		int remainingLength = textBytes.length;

		int countTextBytesWritten = 0;
		int pos = offset;
		// (regardless what was read, we always serialize double-byte
		// unicode characters (UTF-16LE).
		Byte unicodeFlag = new Byte((byte)1);
		while (remainingLength > 0) {
			int chunkSize = Math.min(RecordInputStream.MAX_RECORD_DATA_SIZE - 2, remainingLength);
			remainingLength -= chunkSize;
			pos += ContinueRecord.write(data, pos, unicodeFlag, textBytes, countTextBytesWritten, chunkSize);
			countTextBytesWritten += chunkSize;
		}

		byte[] formatData = createFormatData(_text);
		pos += ContinueRecord.write(data, pos, null, formatData);
		return pos - offset;
	}

	private int getTrailingRecordsSize() {
		if (_text.length() < 1) {
			return 0;
		}
		int encodedTextSize = 0;
		int textBytesLength = _text.length() * LittleEndian.SHORT_SIZE;
		while (textBytesLength > 0) {
			int chunkSize = Math.min(RecordInputStream.MAX_RECORD_DATA_SIZE - 2, textBytesLength);
			textBytesLength -= chunkSize;

			encodedTextSize += 4;           // +4 for ContinueRecord sid+size
			encodedTextSize += 1+chunkSize; // +1 for compressed unicode flag, 
		}

		int encodedFormatSize = (_text.numFormattingRuns() + 1) * FORMAT_RUN_ENCODED_SIZE
			+ 4;  // +4 for ContinueRecord sid+size
		return encodedTextSize + encodedFormatSize;
	}


	public int serialize(int offset, byte[] data) {

		int expectedTotalSize = getRecordSize();
		int totalSize = serializeTXORecord(offset, data);
		
		if (_text.getString().length() > 0) {
			totalSize += serializeTrailingRecords(offset+totalSize, data);
		} 
		
		if (totalSize != expectedTotalSize)
			throw new RecordFormatException(totalSize
					+ " bytes written but getRecordSize() reports " + expectedTotalSize);
		return totalSize;
	}

	/**
	 * Note - this total size includes all potential {@link ContinueRecord}s written
	 */
	public int getRecordSize() {
		int baseSize = 4 + getDataSize();
		return baseSize + getTrailingRecordsSize();
	}

	
	private int getFormattingDataLength() {
		if (_text.length() < 1) {
			// important - no formatting data if text is empty 
			return 0;
		}
		return (_text.numFormattingRuns() + 1) * FORMAT_RUN_ENCODED_SIZE;
	}

	private static byte[] createFormatData(HSSFRichTextString str) {
		int nRuns = str.numFormattingRuns();
		byte[] result = new byte[(nRuns + 1) * FORMAT_RUN_ENCODED_SIZE];
		int pos = 0;
		for (int i = 0; i < nRuns; i++) {
			LittleEndian.putUShort(result, pos, str.getIndexOfFormattingRun(i));
			pos += 2;
			int fontIndex = str.getFontOfFormattingRun(i);
			LittleEndian.putUShort(result, pos, fontIndex == str.NO_FONT ? 0 : fontIndex);
			pos += 2;
			pos += 4; // skip reserved
		}
		LittleEndian.putUShort(result, pos, str.length());
		pos += 2;
		LittleEndian.putUShort(result, pos, 0);
		pos += 2;
		pos += 4; // skip reserved

		return result;
	}

	/**
	 * Sets the Horizontal text alignment field value.
	 */
	public void setHorizontalTextAlignment(int value) {
		field_1_options = HorizontalTextAlignment.setValue(field_1_options, value);
	}

	/**
	 * @return the Horizontal text alignment field value.
	 */
	public int getHorizontalTextAlignment() {
		return HorizontalTextAlignment.getValue(field_1_options);
	}

	/**
	 * Sets the Vertical text alignment field value.
	 */
	public void setVerticalTextAlignment(int value) {
		field_1_options = VerticalTextAlignment.setValue(field_1_options, value);
	}

	/**
	 * @return the Vertical text alignment field value.
	 */
	public int getVerticalTextAlignment() {
		return VerticalTextAlignment.getValue(field_1_options);
	}

	/**
	 * Sets the text locked field value.
	 */
	public void setTextLocked(boolean value) {
		field_1_options = textLocked.setBoolean(field_1_options, value);
	}

	/**
	 * @return the text locked field value.
	 */
	public boolean isTextLocked() {
		return textLocked.isSet(field_1_options);
	}

	/**
	 * Get the text orientation field for the TextObjectBase record.
	 * 
	 * @return One of TEXT_ORIENTATION_NONE TEXT_ORIENTATION_TOP_TO_BOTTOM
	 *         TEXT_ORIENTATION_ROT_RIGHT TEXT_ORIENTATION_ROT_LEFT
	 */
	public int getTextOrientation() {
		return field_2_textOrientation;
	}

	/**
	 * Set the text orientation field for the TextObjectBase record.
	 * 
	 * @param textOrientation
	 *            One of TEXT_ORIENTATION_NONE TEXT_ORIENTATION_TOP_TO_BOTTOM
	 *            TEXT_ORIENTATION_ROT_RIGHT TEXT_ORIENTATION_ROT_LEFT
	 */
	public void setTextOrientation(int textOrientation) {
		this.field_2_textOrientation = textOrientation;
	}

	public HSSFRichTextString getStr() {
		return _text;
	}

	public void setStr(HSSFRichTextString str) {
		_text = str;
	}
	
	public Ptg getLinkRefPtg() {
		return _linkRefPtg;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("[TXO]\n");
		sb.append("    .options        = ").append(HexDump.shortToHex(field_1_options)).append("\n");
		sb.append("         .isHorizontal = ").append(getHorizontalTextAlignment()).append('\n');
		sb.append("         .isVertical   = ").append(getVerticalTextAlignment()).append('\n');
		sb.append("         .textLocked   = ").append(isTextLocked()).append('\n');
		sb.append("    .textOrientation= ").append(HexDump.shortToHex(getTextOrientation())).append("\n");
		sb.append("    .reserved4      = ").append(HexDump.shortToHex(field_3_reserved4)).append("\n");
		sb.append("    .reserved5      = ").append(HexDump.shortToHex(field_4_reserved5)).append("\n");
		sb.append("    .reserved6      = ").append(HexDump.shortToHex(field_5_reserved6)).append("\n");
		sb.append("    .textLength     = ").append(HexDump.shortToHex(_text.length())).append("\n");
		sb.append("    .reserved7      = ").append(HexDump.intToHex(field_8_reserved7)).append("\n");

		sb.append("    .string = ").append(_text).append('\n');

		for (int i = 0; i < _text.numFormattingRuns(); i++) {
			sb.append("    .textrun = ").append(_text.getFontOfFormattingRun(i)).append('\n');

		}
		sb.append("[/TXO]\n");
		return sb.toString();
	}

	public Object clone() {

		TextObjectRecord rec = new TextObjectRecord();
		rec._text = _text;

		rec.field_1_options = field_1_options;
		rec.field_2_textOrientation = field_2_textOrientation;
		rec.field_3_reserved4 = field_3_reserved4;
		rec.field_4_reserved5 = field_4_reserved5;
		rec.field_5_reserved6 = field_5_reserved6;
		rec.field_8_reserved7 = field_8_reserved7;

		rec._text = _text; // clone needed?

		if (_linkRefPtg != null) {
			rec._unknownPreFormulaInt = _unknownPreFormulaInt;
			rec._linkRefPtg = _linkRefPtg.copy();
			rec._unknownPostFormulaByte = rec._unknownPostFormulaByte;
		}
		return rec;
	}
}
