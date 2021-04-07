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

package org.apache.poi.hssf.record.crypto;

import java.io.InputStream;
import java.io.PushbackInputStream;

import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BiffHeaderInput;
import org.apache.poi.hssf.record.FilePassRecord;
import org.apache.poi.hssf.record.InterfaceHdrRecord;
import org.apache.poi.poifs.crypt.ChunkedCipherInputStream;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.util.SuppressForbidden;

public final class Biff8DecryptingStream implements BiffHeaderInput, LittleEndianInput {

    public static final int RC4_REKEYING_INTERVAL = 1024;
    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    private ChunkedCipherInputStream ccis;
    private final byte[] buffer = new byte[LittleEndianConsts.LONG_SIZE];
    private boolean shouldSkipEncryptionOnCurrentRecord;

	public Biff8DecryptingStream(InputStream in, int initialOffset, EncryptionInfo info) throws RecordFormatException {
        try {
            byte[] initialBuf = IOUtils.safelyAllocate(initialOffset, MAX_RECORD_LENGTH);
    	    InputStream stream;
    	    if (initialOffset == 0) {
    	        stream = in;
    	    } else {
    	        stream = new PushbackInputStream(in, initialOffset);
    	        ((PushbackInputStream)stream).unread(initialBuf);
    	    }

            Decryptor dec = info.getDecryptor();
            dec.setChunkSize(RC4_REKEYING_INTERVAL);
            ccis = (ChunkedCipherInputStream)dec.getDataStream(stream, Integer.MAX_VALUE, 0);
            
            if (initialOffset > 0) {
                ccis.readFully(initialBuf);
            }
        } catch (Exception e) {
            throw new RecordFormatException(e);
        }
	}

	@Override
    @SuppressForbidden("just delegating")
    public int available() {
		return ccis.available();
	}

	/**
	 * Reads an unsigned short value without decrypting
	 */
	@Override
    public int readRecordSID() {
	    readPlain(buffer, 0, LittleEndianConsts.SHORT_SIZE);
		int sid = LittleEndian.getUShort(buffer, 0);
		shouldSkipEncryptionOnCurrentRecord = isNeverEncryptedRecord(sid);
		return sid;
	}

	/**
	 * Reads an unsigned short value without decrypting
	 */
	@Override
    public int readDataSize() {
        readPlain(buffer, 0, LittleEndianConsts.SHORT_SIZE);
        int dataSize = LittleEndian.getUShort(buffer, 0);
        ccis.setNextRecordSize(dataSize);
		return dataSize;
	}

	@Override
    public double readDouble() {
	    long valueLongBits = readLong();
		double result = Double.longBitsToDouble(valueLongBits);
		if (Double.isNaN(result)) {
		    // (Because Excel typically doesn't write NaN
		    throw new RuntimeException("Did not expect to read NaN");
		}
		return result;
	}

	@Override
    public void readFully(byte[] buf) {
	    readFully(buf, 0, buf.length);
	}

	@Override
    public void readFully(byte[] buf, int off, int len) {
        if (shouldSkipEncryptionOnCurrentRecord) {
            readPlain(buf, off, buf.length);
        } else {
            ccis.readFully(buf, off, len);
        }
	}

	@Override
    public int readUByte() {
	    return readByte() & 0xFF;
	}
	
	@Override
    public byte readByte() {
        if (shouldSkipEncryptionOnCurrentRecord) {
            readPlain(buffer, 0, LittleEndianConsts.BYTE_SIZE);
            return buffer[0];
        } else {
            return ccis.readByte();
        }
	}

	@Override
    public int readUShort() {
	    return readShort() & 0xFFFF;
	}
	
	@Override
    public short readShort() {
        if (shouldSkipEncryptionOnCurrentRecord) {
            readPlain(buffer, 0, LittleEndianConsts.SHORT_SIZE);
            return LittleEndian.getShort(buffer);
        } else {
            return ccis.readShort();
        }
	}

	@Override
    public int readInt() {
        if (shouldSkipEncryptionOnCurrentRecord) {
            readPlain(buffer, 0, LittleEndianConsts.INT_SIZE);
            return LittleEndian.getInt(buffer);
        } else {
            return ccis.readInt();
        }
	}

	@Override
    public long readLong() {
        if (shouldSkipEncryptionOnCurrentRecord) {
            readPlain(buffer, 0, LittleEndianConsts.LONG_SIZE);
            return LittleEndian.getLong(buffer);
        } else {
            return ccis.readLong();
        }
	}

	/**
	 * @return the absolute position in the stream
	 */
	public long getPosition() {
	    return ccis.getPos();
	}
	
    /**
     * TODO: Additionally, the lbPlyPos (position_of_BOF) field of the BoundSheet8 record MUST NOT be encrypted.
     *
     * @return <code>true</code> if record type specified by <tt>sid</tt> is never encrypted
     */
    public static boolean isNeverEncryptedRecord(int sid) {
        switch (sid) {
            case BOFRecord.sid:
                // sheet BOFs for sure
                // TODO - find out about chart BOFs

            case InterfaceHdrRecord.sid:
                // don't know why this record doesn't seem to get encrypted

            case FilePassRecord.sid:
                // this only really counts when writing because FILEPASS is read early

            // UsrExcl(0x0194)
            // FileLock
            // RRDInfo(0x0196)
            // RRDHead(0x0138)

                return true;

            default:
                return false;
        }
    }

    @Override
    public void readPlain(byte[] b, int off, int len) {
        ccis.readPlain(b, off, len);
    }

}
