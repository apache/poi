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
import java.util.Locale;

import org.apache.poi.hssf.dev.BiffViewer;
import org.apache.poi.hssf.record.crypto.Biff8DecryptingStream;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.RecordFormatException;

/**
 * Title:  Record Input Stream
 *
 * Description:  Wraps a stream and provides helper methods for the construction of records.
 */
public final class RecordInputStream implements LittleEndianInput {


	/** Maximum size of a single record (minus the 4 byte header) without a continue*/
	public final static short MAX_RECORD_DATA_SIZE = 8224;
	private static final int INVALID_SID_VALUE = -1;
	//arbitrarily selected; may need to increase
	private static final int MAX_RECORD_LENGTH = 100_000;
	/**
	 * When {@link #_currentDataLength} has this value, it means that the previous BIFF record is
	 * finished, the next sid has been properly read, but the data size field has not been read yet.
	 */
	private static final int DATA_LEN_NEEDS_TO_BE_READ = -1;
	private static final byte[] EMPTY_BYTE_ARRAY = { };

	/**
	 * For use in {@link BiffViewer} which may construct {@link Record}s that don't completely
	 * read all available data.  This exception should never be thrown otherwise.
	 */
	@SuppressWarnings("serial")
	public static final class LeftoverDataException extends RuntimeException {
		public LeftoverDataException(int sid, int remainingByteCount) {
			super("Initialisation of record 0x" + Integer.toHexString(sid).toUpperCase(Locale.ROOT)
					+ "(" + getRecordName(sid) + ") left " + remainingByteCount 
					+ " bytes remaining still to be read.");
		}

        private static String getRecordName(int sid) {
            Class<? extends Record> recordClass = RecordFactory.getRecordClass(sid);
            if(recordClass == null) {
                return null;
            }
            return recordClass.getSimpleName();
        }
	}

	/** Header {@link LittleEndianInput} facet of the wrapped {@link InputStream} */
	private final BiffHeaderInput _bhi;
	/** Data {@link LittleEndianInput} facet of the wrapped {@link InputStream} */
	private final LittleEndianInput _dataInput;
	/** the record identifier of the BIFF record currently being read */
	private int _currentSid;
	/**
	 * Length of the data section of the current BIFF record (always 4 less than the total record size).
	 * When uninitialised, this field is set to {@link #DATA_LEN_NEEDS_TO_BE_READ}.
	 */
	private int _currentDataLength;
	/**
	 * The BIFF record identifier for the next record is read when just as the current record
	 * is finished.
	 * This field is only really valid during the time that ({@link #_currentDataLength} ==
	 * {@link #DATA_LEN_NEEDS_TO_BE_READ}).  At most other times its value is not really the
	 * 'sid of the next record'.  Wwhile mid-record, this field coincidentally holds the sid
	 * of the current record.
	 */
	private int _nextSid;
	/**
	 * index within the data section of the current BIFF record
	 */
	private int _currentDataOffset;
	/**
	 * index within the data section when mark() was called
	 */
	private int _markedDataOffset;

	private static final class SimpleHeaderInput implements BiffHeaderInput {

		private final LittleEndianInput _lei;

		private SimpleHeaderInput(LittleEndianInput lei) {
			_lei = lei;
		}
		@Override
        public int available() {
			return _lei.available();
		}
		@Override
        public int readDataSize() {
			return _lei.readUShort();
		}
		@Override
        public int readRecordSID() {
			return _lei.readUShort();
		}
	}

	public RecordInputStream(InputStream in) throws RecordFormatException {
		this (in, null, 0);
	}

	public RecordInputStream(InputStream in, EncryptionInfo key, int initialOffset) throws RecordFormatException {
		if (key == null) {
			_dataInput = (in instanceof LittleEndianInput)
				// accessing directly is an optimisation
				? (LittleEndianInput)in
				// less optimal, but should work OK just the same. Often occurs in junit tests.
				: new LittleEndianInputStream(in);
			_bhi = new SimpleHeaderInput(_dataInput);
		} else {
			Biff8DecryptingStream bds = new Biff8DecryptingStream(in, initialOffset, key);
            _dataInput = bds;
			_bhi = bds;
		}
		_nextSid = readNextSid();
	}

	static LittleEndianInput getLEI(InputStream is) {
		if (is instanceof LittleEndianInput) {
			// accessing directly is an optimisation
			return (LittleEndianInput) is;
		}
		// less optimal, but should work OK just the same. Often occurs in junit tests.
		return new LittleEndianInputStream(is);
	}

	/**
	 * @return the number of bytes available in the current BIFF record
	 * @see #remaining()
	 */
	@Override
    public int available() {
		return remaining();
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
		return (short) _currentSid;
	}

	/**
	 * Note - this method is expected to be called only when completed reading the current BIFF
	 * record.
	 * 
	 * @return true, if there's another record in the stream
	 * 
	 * @throws LeftoverDataException if this method is called before reaching the end of the
	 * current record.
	 */
	public boolean hasNextRecord() throws LeftoverDataException {
		if (_currentDataLength != -1 && _currentDataLength != _currentDataOffset) {
			throw new LeftoverDataException(_currentSid, remaining());
		}
		if (_currentDataLength != DATA_LEN_NEEDS_TO_BE_READ) {
			_nextSid = readNextSid();
		}
		return _nextSid != INVALID_SID_VALUE;
	}

	/**
	 * @return the sid of the next record or {@link #INVALID_SID_VALUE} if at end of stream
	 */
	private int readNextSid() {
		int nAvailable  = _bhi.available();
		if (nAvailable < EOFRecord.ENCODED_SIZE) {
			// some scrap left over, if nAvailable > 0?
			// ex45582-22397.xls has one extra byte after the last record
			// Excel reads that file OK
			return INVALID_SID_VALUE;
		}
		int result = _bhi.readRecordSID();
		if (result == INVALID_SID_VALUE) {
			throw new RecordFormatException("Found invalid sid (" + result + ")");
		}
		_currentDataLength = DATA_LEN_NEEDS_TO_BE_READ;
		return result;
	}

	/** Moves to the next record in the stream.
	 *
	 * <i>Note: The auto continue flag is reset to true</i>
	 */
	public void nextRecord() throws RecordFormatException {
		if (_nextSid == INVALID_SID_VALUE) {
			throw new IllegalStateException("EOF - next record not available");
		}
		if (_currentDataLength != DATA_LEN_NEEDS_TO_BE_READ) {
			throw new IllegalStateException("Cannot call nextRecord() without checking hasNextRecord() first");
		}
		_currentSid = _nextSid;
		_currentDataOffset = 0;
		_currentDataLength = _bhi.readDataSize();
		if (_currentDataLength > MAX_RECORD_DATA_SIZE) {
			throw new RecordFormatException("The content of an excel record cannot exceed "
					+ MAX_RECORD_DATA_SIZE + " bytes");
		}
	}

	private void checkRecordPosition(int requiredByteCount) {

		int nAvailable = remaining();
		if (nAvailable >= requiredByteCount) {
			// all OK
			return;
		}
		if (nAvailable == 0 && isContinueNext()) {
			nextRecord();
			return;
		}
		throw new RecordFormatException("Not enough data (" + nAvailable
				+ ") to read requested (" + requiredByteCount +") bytes");
	}

	/**
	 * Reads an 8 bit, signed value
	 */
	@Override
    public byte readByte() {
		checkRecordPosition(LittleEndianConsts.BYTE_SIZE);
		_currentDataOffset += LittleEndianConsts.BYTE_SIZE;
		return _dataInput.readByte();
	}

	/**
	 * Reads a 16 bit, signed value
	 */
	@Override
    public short readShort() {
		checkRecordPosition(LittleEndianConsts.SHORT_SIZE);
		_currentDataOffset += LittleEndianConsts.SHORT_SIZE;
		return _dataInput.readShort();
	}

	/**
	 * Reads a 32 bit, signed value 
	 */
	@Override
    public int readInt() {
		checkRecordPosition(LittleEndianConsts.INT_SIZE);
		_currentDataOffset += LittleEndianConsts.INT_SIZE;
		return _dataInput.readInt();
	}

	/**
	 * Reads a 64 bit, signed value
	 */
	@Override
    public long readLong() {
		checkRecordPosition(LittleEndianConsts.LONG_SIZE);
		_currentDataOffset += LittleEndianConsts.LONG_SIZE;
		return _dataInput.readLong();
	}

	/**
	 * Reads an 8 bit, unsigned value
	 */
	@Override
    public int readUByte() {
		return readByte() & 0x00FF;
	}

	/**
	 * Reads a 16 bit, unsigned value.
	 */
	@Override
    public int readUShort() {
		checkRecordPosition(LittleEndianConsts.SHORT_SIZE);
		_currentDataOffset += LittleEndianConsts.SHORT_SIZE;
		return _dataInput.readUShort();
	}

	@Override
    public double readDouble() {
        // YK: Excel doesn't write NaN but instead converts the cell type into {@link CellType#ERROR}.
		return Double.longBitsToDouble(readLong());
	}
	
	public void readPlain(byte[] buf, int off, int len) {
	    readFully(buf, 0, buf.length, true);
	}
	
	@Override
    public void readFully(byte[] buf) {
		readFully(buf, 0, buf.length, false);
	}

    @Override
    public void readFully(byte[] buf, int off, int len) {
        readFully(buf, off, len, false);
    }
	
    private void readFully(byte[] buf, int off, int len, boolean isPlain) {
	    int origLen = len;
	    if (buf == null) {
	        throw new NullPointerException();
	    } else if (off < 0 || len < 0 || len > buf.length - off) {
	        throw new IndexOutOfBoundsException();
	    }
	    
	    while (len > 0) {
	        int nextChunk = Math.min(available(),len);
	        if (nextChunk == 0) {
	            if (!hasNextRecord()) {
	                throw new RecordFormatException("Can't read the remaining "+len+" bytes of the requested "+origLen+" bytes. No further record exists.");
	            } else {
	                nextRecord();
	                nextChunk = Math.min(available(),len);
	                assert(nextChunk > 0);
	            }
	        }
	        checkRecordPosition(nextChunk);
	        if (isPlain) {
                _dataInput.readPlain(buf, off, nextChunk);
	        } else {
                _dataInput.readFully(buf, off, nextChunk);
	        }
	        _currentDataOffset+=nextChunk;
	        off += nextChunk;
	        len -= nextChunk;
	    }
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
			int availableChars =isCompressedEncoding ?  remaining() : remaining() / LittleEndianConsts.SHORT_SIZE;
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
            assert(compressFlag == 0 || compressFlag == 1);
			isCompressedEncoding = (compressFlag == 0);
		}
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
		byte[] result = IOUtils.safelyAllocate(size, MAX_RECORD_LENGTH);
		readFully(result);
		return result;
	}

    /**
     * Reads all byte data for the current record, including any that overlaps
     * into any following continue records.
     *
     * @return all byte data for the current record
     * 
     * @deprecated POI 2.0 Best to write a input stream that wraps this one
     *             where there is special sub record that may overlap continue
     *             records.
     */
    @Deprecated
    public byte[] readAllContinuedRemainder() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(2 * MAX_RECORD_DATA_SIZE);

        while (true) {
            byte[] b = readRemainder();
            out.write(b, 0, b.length);
            if (!isContinueNext()) {
                break;
            }
            nextRecord();
        }
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
		return _currentDataLength - _currentDataOffset;
	}

	/**
	 *
	 * @return <code>true</code> when a {@link ContinueRecord} is next.
	 */
	private boolean isContinueNext() {
		if (_currentDataLength != DATA_LEN_NEEDS_TO_BE_READ && _currentDataOffset != _currentDataLength) {
			throw new IllegalStateException("Should never be called before end of current record");
		}
		if (!hasNextRecord()) {
			return false;
		}
		// At what point are records continued?
		//  - Often from within the char data of long strings (caller is within readStringCommon()).
		//  - From UnicodeString construction (many different points - call via checkRecordPosition)
		//  - During TextObjectRecord construction (just before the text, perhaps within the text,
		//    and before the formatting run data)
		return _nextSid == ContinueRecord.sid;
	}

    /**
     @return sid of next record. Can be called after hasNextRecord()
     */
    public int getNextSid() {
        return _nextSid;
    }

    /**
     * Mark the stream position - experimental function 
     *
     * @param readlimit the read ahead limit
     * 
     * @see InputStream#mark(int)
     */
    @Internal
    public void mark(int readlimit) {
        ((InputStream)_dataInput).mark(readlimit);
        _markedDataOffset = _currentDataOffset;
    }
    
    /**
     * Resets the stream position to the previously marked position.
     * Experimental function - this only works, when nextRecord() wasn't called in the meantime.
     *
     * @throws IOException if marking is not supported
     * 
     * @see InputStream#reset()
     */
    @Internal
    public void reset() throws IOException {
        ((InputStream)_dataInput).reset();
        _currentDataOffset = _markedDataOffset;
    }
}
