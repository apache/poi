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

package org.apache.poi.hssf.record.cont;

import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.util.DelayableLittleEndianOutput;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.StringUtil;

/**
 * An augmented {@link LittleEndianOutput} used for serialization of {@link ContinuableRecord}s.
 * This class keeps track of how much remaining space is available in the current BIFF record and
 * can start new {@link ContinueRecord}s as required.
 *
 * @author Josh Micich
 */
public final class ContinuableRecordOutput implements LittleEndianOutput {

	private final LittleEndianOutput _out;
	private UnknownLengthRecordOutput _ulrOutput;
	private int _totalPreviousRecordsSize;

	public ContinuableRecordOutput(LittleEndianOutput out, int sid) {
		_ulrOutput = new UnknownLengthRecordOutput(out, sid);
		_out = out;
		_totalPreviousRecordsSize = 0;
	}

	public static ContinuableRecordOutput createForCountingOnly() {
		return new ContinuableRecordOutput(NOPOutput, -777); // fake sid
	}

	/**
	 * @return total number of bytes written so far (including all BIFF headers)
	 */
	public int getTotalSize() {
		return _totalPreviousRecordsSize + _ulrOutput.getTotalSize();
	}
	/**
	 * Terminates the last record (also updates its 'ushort size' field)
	 */
	void terminate() {
		_ulrOutput.terminate();
	}
	/**
	 * @return number of remaining bytes of space in current record
	 */
	public int getAvailableSpace() {
		return _ulrOutput.getAvailableSpace();
	}

	/**
	 * Terminates the current record and starts a new {@link ContinueRecord} (regardless
	 * of how much space is still available in the current record).
	 */
	public void writeContinue() {
		_ulrOutput.terminate();
		_totalPreviousRecordsSize += _ulrOutput.getTotalSize();
		_ulrOutput = new UnknownLengthRecordOutput(_out, ContinueRecord.sid);
	}
	/**
	 * Will terminate the current record and start a new {@link ContinueRecord}
	 *  if there isn't space for the requested number of bytes
	 * @param requiredContinuousSize The number of bytes that need to be written
	 */
	public void writeContinueIfRequired(int requiredContinuousSize) {
		if (_ulrOutput.getAvailableSpace() < requiredContinuousSize) {
			writeContinue();
		}
	}

	/**
	 * Writes the 'optionFlags' byte and encoded character data of a unicode string.  This includes:
	 * <ul>
	 * <li>byte optionFlags</li>
	 * <li>encoded character data (in "ISO-8859-1" or "UTF-16LE" encoding)</li>
	 * </ul>
	 *
	 * Notes:
	 * <ul>
	 * <li>The value of the 'is16bitEncoded' flag is determined by the actual character data
	 * of <tt>text</tt></li>
	 * <li>The string options flag is never separated (by a {@link ContinueRecord}) from the
	 * first chunk of character data it refers to.</li>
	 * <li>The 'ushort length' field is assumed to have been explicitly written earlier.  Hence,
	 * there may be an intervening {@link ContinueRecord}</li>
	 * </ul>
	 */
	public void writeStringData(String text) {
		boolean is16bitEncoded = StringUtil.hasMultibyte(text);
		// calculate total size of the header and first encoded char
		int keepTogetherSize = 1 + 1; // ushort len, at least one character byte
		int optionFlags = 0x00;
		if (is16bitEncoded) {
			optionFlags |= 0x01;
			keepTogetherSize += 1; // one extra byte for first char
		}
		writeContinueIfRequired(keepTogetherSize);
		writeByte(optionFlags);
		writeCharacterData(text, is16bitEncoded);
	}
	/**
	 * Writes a unicode string complete with header and character data.  This includes:
	 * <ul>
	 * <li>ushort length</li>
	 * <li>byte optionFlags</li>
	 * <li>ushort numberOfRichTextRuns (optional)</li>
	 * <li>ushort extendedDataSize (optional)</li>
	 * <li>encoded character data (in "ISO-8859-1" or "UTF-16LE" encoding)</li>
	 * </ul>
	 *
	 * The following bits of the 'optionFlags' byte will be set as appropriate:
	 * <table border='1'>
	 * <tr><th>Mask</th><th>Description</th></tr>
	 * <tr><td>0x01</td><td>is16bitEncoded</td></tr>
	 * <tr><td>0x04</td><td>hasExtendedData</td></tr>
	 * <tr><td>0x08</td><td>isRichText</td></tr>
	 * </table>
	 * Notes:
	 * <ul>
	 * <li>The value of the 'is16bitEncoded' flag is determined by the actual character data
	 * of <tt>text</tt></li>
	 * <li>The string header fields are never separated (by a {@link ContinueRecord}) from the
	 * first chunk of character data (i.e. the first character is always encoded in the same
	 * record as the string header).</li>
	 * </ul>
	 */
	public void writeString(String text, int numberOfRichTextRuns, int extendedDataSize) {
		boolean is16bitEncoded = StringUtil.hasMultibyte(text);
		// calculate total size of the header and first encoded char
		int keepTogetherSize = 2 + 1 + 1; // ushort len, byte optionFlags, at least one character byte
		int optionFlags = 0x00;
		if (is16bitEncoded) {
			optionFlags |= 0x01;
			keepTogetherSize += 1; // one extra byte for first char
		}
		if (numberOfRichTextRuns > 0) {
			optionFlags |= 0x08;
			keepTogetherSize += 2;
		}
		if (extendedDataSize > 0) {
			optionFlags |= 0x04;
			keepTogetherSize += 4;
		}
		writeContinueIfRequired(keepTogetherSize);
		writeShort(text.length());
		writeByte(optionFlags);
		if (numberOfRichTextRuns > 0) {
			writeShort(numberOfRichTextRuns);
		}
		if (extendedDataSize > 0) {
			writeInt(extendedDataSize);
		}
		writeCharacterData(text, is16bitEncoded);
	}


	private void writeCharacterData(String text, boolean is16bitEncoded) {
		int nChars = text.length();
		int i=0;
		if (is16bitEncoded) {
			while(true) {
				int nWritableChars = Math.min(nChars-i, _ulrOutput.getAvailableSpace() / 2);
				for ( ; nWritableChars > 0; nWritableChars--) {
					_ulrOutput.writeShort(text.charAt(i++));
				}
				if (i >= nChars) {
					break;
				}
				writeContinue();
				writeByte(0x01);
			}
		} else {
			while(true) {
				int nWritableChars = Math.min(nChars-i, _ulrOutput.getAvailableSpace() / 1);
				for ( ; nWritableChars > 0; nWritableChars--) {
					_ulrOutput.writeByte(text.charAt(i++));
				}
				if (i >= nChars) {
					break;
				}
				writeContinue();
				writeByte(0x00);
			}
		}
	}

	public void write(byte[] b) {
		writeContinueIfRequired(b.length);
		_ulrOutput.write(b);
	}
	public void write(byte[] b, int offset, int len) {
		writeContinueIfRequired(len);
		_ulrOutput.write(b, offset, len);
	}
	public void writeByte(int v) {
		writeContinueIfRequired(1);
		_ulrOutput.writeByte(v);
	}
	public void writeDouble(double v) {
		writeContinueIfRequired(8);
		_ulrOutput.writeDouble(v);
	}
	public void writeInt(int v) {
		writeContinueIfRequired(4);
		_ulrOutput.writeInt(v);
	}
	public void writeLong(long v) {
		writeContinueIfRequired(8);
		_ulrOutput.writeLong(v);
	}
	public void writeShort(int v) {
		writeContinueIfRequired(2);
		_ulrOutput.writeShort(v);
	}

	/**
	 * Allows optimised usage of {@link ContinuableRecordOutput} for sizing purposes only.
	 */
	private static final LittleEndianOutput NOPOutput = new DelayableLittleEndianOutput() {

		public LittleEndianOutput createDelayedOutput(int size) {
			return this;
		}
		public void write(byte[] b) {
			// does nothing
		}
		public void write(byte[] b, int offset, int len) {
			// does nothing
		}
		public void writeByte(int v) {
			// does nothing
		}
		public void writeDouble(double v) {
			// does nothing
		}
		public void writeInt(int v) {
			// does nothing
		}
		public void writeLong(long v) {
			// does nothing
		}
		public void writeShort(int v) {
			// does nothing
		}
	};
}
