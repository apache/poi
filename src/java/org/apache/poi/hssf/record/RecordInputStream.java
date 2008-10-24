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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianInputStream;

/**
 * Title:  Record Input Stream<P>
 * Description:  Wraps a stream and provides helper methods for the construction of records.<P>
 *
 * @author Jason Height (jheight @ apache dot org)
 */
public final class RecordInputStream extends InputStream implements LittleEndianInput {
	/** Maximum size of a single record (minus the 4 byte header) without a continue*/
	public final static short MAX_RECORD_DATA_SIZE = 8224;
	private static final int INVALID_SID_VALUE = -1;
	private static final int DATA_LEN_NEEDS_TO_BE_READ = -1;
	private static final byte[] EMPTY_BYTE_ARRAY = { };

	private final InputStream _in;
	/** {@link LittleEndianInput} facet of field {@link #_in} */
	private final LittleEndianInput _le;
	private int currentSid;
	private int _currentDataLength;
	private int nextSid;
	private int recordOffset;
	private boolean autoContinue; // TODO - remove this

	public RecordInputStream(InputStream in) throws RecordFormatException {
		_in = in;
		if (in instanceof LittleEndianInput) {
			// accessing directly is an optimisation
			_le = (LittleEndianInput) in;
		} else {
			// less optimal, but should work OK just the same. Often occurs in junit tests.
			_le = new LittleEndianInputStream(in);
		}
		try {
		      if (_in.available() < LittleEndian.SHORT_SIZE) {
		          nextSid = INVALID_SID_VALUE;
		      } else {
		    	  nextSid = LittleEndian.readShort(in);
		      }
		} catch (IOException ex) {
			throw new RecordFormatException("Error reading bytes", ex);
		}
		_currentDataLength = DATA_LEN_NEEDS_TO_BE_READ;
		autoContinue = true;
	}

	public int read() {
		checkRecordPosition(LittleEndian.BYTE_SIZE);
		recordOffset += LittleEndian.BYTE_SIZE;
		return _le.readUByte();
	}
	public int read(byte[] b, int off, int len) {
		int limit = Math.min(len, remaining());
		if (limit == 0) {
			return 0;
		}
		readFully(b, off,limit);
		return limit;
	}

  public short getSid() {
    return (short) currentSid;
  }

  public short getLength() { // TODO - remove
    return (short) _currentDataLength;
  }


	/**
	 * Note - this method is expected to be called only when completed reading the current BIFF record.
	 * Calling this before reaching the end of the current record will cause all remaining data to be
	 * discarded
	 */
	public boolean hasNextRecord() {
		if (_currentDataLength != -1 && _currentDataLength != recordOffset) {
			System.out.println("WARN. Unread "+remaining()+" bytes of record 0x"+Integer.toHexString(currentSid));
			// discard unread data
			while (recordOffset < _currentDataLength) {
				readByte();
			}
		}
		if (_currentDataLength != DATA_LEN_NEEDS_TO_BE_READ) {
			nextSid = readNextSid();
			_currentDataLength = DATA_LEN_NEEDS_TO_BE_READ;
		}
		return nextSid != INVALID_SID_VALUE;
	}

	/**
	 * 
	 * @return the sid of the next record or {@link #INVALID_SID_VALUE} if at end of stream
	 */
	private int readNextSid() {
		int nAvailable;
		try {
			nAvailable = _in.available();
		} catch (IOException e) {
			throw new RecordFormatException("Error checking stream available bytes", e);
		}
		if (nAvailable < EOFRecord.ENCODED_SIZE) {
			if (nAvailable > 0) {
				// some scrap left over?
				// ex45582-22397.xls has one extra byte after the last record
				// Excel reads that file OK
			}
			return INVALID_SID_VALUE;
		}
		int result = _le.readUShort();
		if (result == INVALID_SID_VALUE) {
			throw new RecordFormatException("Found invalid sid (" + result + ")");
		}
		return result;
	}

	/** Moves to the next record in the stream.
	 *
	 * <i>Note: The auto continue flag is reset to true</i>
	 */
	public void nextRecord() throws RecordFormatException {
		if (nextSid == INVALID_SID_VALUE) {
			throw new IllegalStateException("EOF - next record not available");
		}
		currentSid = nextSid;
		autoContinue = true;
		recordOffset = 0;
		_currentDataLength = _le.readUShort();
		if (_currentDataLength > MAX_RECORD_DATA_SIZE) {
			throw new RecordFormatException("The content of an excel record cannot exceed "
					+ MAX_RECORD_DATA_SIZE + " bytes");
		}
	}

  public void setAutoContinue(boolean enable) {
    this.autoContinue = enable;
  }

	private void checkRecordPosition(int requiredByteCount) {

		if (remaining() < requiredByteCount) {
			if (isContinueNext() && autoContinue) {
				nextRecord();
			} else {
			   throw new ArrayIndexOutOfBoundsException();
			}
		}
	}

	/**
	 * Reads an 8 bit, signed value
	 */
	public byte readByte() {
		checkRecordPosition(LittleEndian.BYTE_SIZE);
		recordOffset += LittleEndian.BYTE_SIZE;
		return _le.readByte();
	}

	/**
	 * Reads a 16 bit, signed value
	 */
	public short readShort() {
		checkRecordPosition(LittleEndian.SHORT_SIZE);
		recordOffset += LittleEndian.SHORT_SIZE;
		return _le.readShort();
	}

	public int readInt() {
		checkRecordPosition(LittleEndian.INT_SIZE);
		recordOffset += LittleEndian.INT_SIZE;
		return _le.readInt();
	}

	public long readLong() {
		checkRecordPosition(LittleEndian.LONG_SIZE);
		recordOffset += LittleEndian.LONG_SIZE;
		return _le.readLong();
	}

	/**
	 * Reads an 8 bit, unsigned value
	 */
	public int readUByte() {
		return readByte() & 0x00FF;
	}

	/**
	 * Reads a 16 bit, unsigned value.
	 * @return
	 */
	public int readUShort() {
		checkRecordPosition(LittleEndian.SHORT_SIZE);
		recordOffset += LittleEndian.SHORT_SIZE;
		return _le.readUShort();
	}

	public double readDouble() {
		checkRecordPosition(LittleEndian.DOUBLE_SIZE);
		recordOffset += LittleEndian.DOUBLE_SIZE;
		long valueLongBits = _le.readLong();
		double result = Double.longBitsToDouble(valueLongBits);
		if (Double.isNaN(result)) {
			throw new RuntimeException("Did not expect to read NaN"); // (Because Excel typically doesn't write NaN
		}
		return result;
	}
	public void readFully(byte[] buf) {
		readFully(buf, 0, buf.length);
	}

	public void readFully(byte[] buf, int off, int len) {
		checkRecordPosition(len);
		_le.readFully(buf, off, len);
		recordOffset+=len;
	}

	public String readString() {
		int requestedLength = readUShort();
		byte compressFlag = readByte();
		return readStringCommon(requestedLength, compressFlag == 0);
	}
	/**
	 *  given a byte array of 16-bit unicode characters, compress to 8-bit and
	 *  return a string
	 *
	 * { 0x16, 0x00 } -0x16
	 *
	 * @param requestedLength the length of the final string
	 * @return                                     the converted string
	 * @exception  IllegalArgumentException        if len is too large (i.e.,
	 *      there is not enough data in string to create a String of that
	 *      length)
	 */
	public String readUnicodeLEString(int requestedLength) {
		return readStringCommon(requestedLength, false);
	}

	public String readCompressedUnicode(int requestedLength) {
		return readStringCommon(requestedLength, true);
	}

	private String readStringCommon(int requestedLength, boolean pIsCompressedEncoding) {
		// Sanity check to detect garbage string lengths
		if (requestedLength < 0 || requestedLength > 0x100000) { // 16 million chars?
			throw new IllegalArgumentException("Bad requested string length (" + requestedLength + ")");
		}
		char[] buf = new char[requestedLength];
		boolean isCompressedEncoding = pIsCompressedEncoding;
		int curLen = 0;
		while(true) {
			int availableChars =isCompressedEncoding ?  remaining() : remaining() / LittleEndian.SHORT_SIZE;
			if (requestedLength - curLen <= availableChars) {
				// enough space in current record, so just read it out
				while(curLen < requestedLength) {
					char ch;
					if (isCompressedEncoding) {
						ch = (char)readUByte();
					} else {
						ch = (char)readShort();
					}
					buf[curLen] = ch;
					curLen++;
				}
				return new String(buf);
			}
			// else string has been spilled into next continue record
			// so read what's left of the current record
			while(availableChars > 0) {
				char ch;
				if (isCompressedEncoding) {
					ch = (char)readUByte();
				} else {
					ch = (char)readShort();
				}
				buf[curLen] = ch;
				curLen++;
				availableChars--;
			}
			if (!isContinueNext()) {
				throw new RecordFormatException("Expected to find a ContinueRecord in order to read remaining " 
						+ (requestedLength-curLen) + " of " + requestedLength + " chars");
			}
			if(remaining() != 0) {
				throw new RecordFormatException("Odd number of bytes(" + remaining() + ") left behind");
			}
			nextRecord();
			// note - the compressed flag may change on the fly
			byte compressFlag = readByte();
			isCompressedEncoding = (compressFlag == 0); 
		}
	}

  /** Returns an excel style unicode string from the bytes reminaing in the record.
   * <i>Note:</i> Unicode strings differ from <b>normal</b> strings due to the addition of
   * formatting information.
   *
   * @return The unicode string representation of the remaining bytes.
   */
  public UnicodeString readUnicodeString() {
    return new UnicodeString(this);
  }

	/** Returns the remaining bytes for the current record.
	 *
	  * @return The remaining bytes of the current record.
	  */
	public byte[] readRemainder() {
		int size = remaining();
		if (size ==0) {
			return EMPTY_BYTE_ARRAY;
		}
		byte[] result = new byte[size];
		readFully(result);
		return result;
	}

  /** Reads all byte data for the current record, including any
   *  that overlaps into any following continue records.
   *
   *  @deprecated Best to write a input stream that wraps this one where there is
   *  special sub record that may overlap continue records.
   */
  public byte[] readAllContinuedRemainder() {
    //Using a ByteArrayOutputStream is just an easy way to get a
    //growable array of the data.
    ByteArrayOutputStream out = new ByteArrayOutputStream(2*MAX_RECORD_DATA_SIZE);

    while (isContinueNext()) {
      byte[] b = readRemainder();
      out.write(b, 0, b.length);
      nextRecord();
    }
    byte[] b = readRemainder();
    out.write(b, 0, b.length);

    return out.toByteArray();
  }

	/** The remaining number of bytes in the <i>current</i> record.
	 *
	 * @return The number of bytes remaining in the current record
	 */
	public int remaining() {
		if (_currentDataLength == DATA_LEN_NEEDS_TO_BE_READ) {
			// already read sid of next record. so current one is finished
			return 0;
		}
		return (_currentDataLength - recordOffset);
	}

	/**
	 *
	 * @return <code>true</code> when a {@link ContinueRecord} is next.
	 */
	public boolean isContinueNext() {
		if (_currentDataLength != DATA_LEN_NEEDS_TO_BE_READ && recordOffset != _currentDataLength) {
			throw new IllegalStateException("Should never be called before end of current record");
		}
		if (!hasNextRecord()) {
			return false;
		}
		return nextSid == ContinueRecord.sid;
	}
}
