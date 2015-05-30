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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.crypto.Cipher;

import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.FilePassRecord;
import org.apache.poi.hssf.record.InterfaceHdrRecord;

public class Biff8XOR implements Biff8Cipher {

    private final Biff8XORKey _key;
    private ByteBuffer _buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
    private boolean _shouldSkipEncryptionOnCurrentRecord;
    private final int _initialOffset;
    private int _dataLength = 0;
    private int _xorArrayIndex = 0;
    
    public Biff8XOR(int initialOffset, Biff8XORKey key) {
        _key = key;
        _initialOffset = initialOffset;

    }
    
    public void startRecord(int currentSid) {
        _shouldSkipEncryptionOnCurrentRecord = isNeverEncryptedRecord(currentSid);
    }

    public void setNextRecordSize(int recordSize) {
        /*
         * From: http://social.msdn.microsoft.com/Forums/en-US/3dadbed3-0e68-4f11-8b43-3a2328d9ebd5
         * 
         * The initial value for XorArrayIndex is as follows:
         * XorArrayIndex = (FileOffset + Data.Length) % 16
         * 
         * The FileOffset variable in this context is the stream offset into the Workbook stream at
         * the time we are about to write each of the bytes of the record data.
         * This (the value) is then incremented after each byte is written. 
         */
        _xorArrayIndex = (_initialOffset+_dataLength+recordSize) % 16;
    }
    
    
    /**
     * TODO: Additionally, the lbPlyPos (position_of_BOF) field of the BoundSheet8 record MUST NOT be encrypted.
     *
     * @return <code>true</code> if record type specified by <tt>sid</tt> is never encrypted
     */
    private static boolean isNeverEncryptedRecord(int sid) {
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
        }
        return false;
    }

    /**
     * Used when BIFF header fields (sid, size) are being read. The internal
     * {@link Cipher} instance must step even when unencrypted bytes are read
     */
    public void skipTwoBytes() {
        _dataLength += 2;
    }

    /**
     * Decrypts a xor obfuscated byte array.
     * The data is decrypted in-place
     * 
     * @see <a href="http://msdn.microsoft.com/en-us/library/dd908506.aspx">2.3.7.3 Binary Document XOR Data Transformation Method 1</a>
     */
    public void xor(byte[] buf, int pOffset, int pLen) {
        if (_shouldSkipEncryptionOnCurrentRecord) {
            _dataLength += pLen;
            return;
        }
        
        // The following is taken from the Libre Office implementation
        // It seems that the encrypt and decrypt method is mixed up
        // in the MS-OFFCRYPTO docs

        byte xorArray[] = _key._secretKey.getEncoded();
        
        for (int i=0; i<pLen; i++) {
            byte value = buf[pOffset+i];
            value = rotateLeft(value, 3);
            value ^= xorArray[_xorArrayIndex];
            buf[pOffset+i] = value;
            _xorArrayIndex = (_xorArrayIndex + 1) % 16;
            _dataLength++;
        }
    }
    
    private static byte rotateLeft(byte bits, int shift) {
        return (byte)(((bits & 0xff) << shift) | ((bits & 0xff) >>> (8 - shift)));
    }
    
    public int xorByte(int rawVal) {
        _buffer.put(0, (byte)rawVal);
        xor(_buffer.array(), 0, 1);
        return _buffer.get(0);
    }

    public int xorShort(int rawVal) {
        _buffer.putShort(0, (short)rawVal);
        xor(_buffer.array(), 0, 2);
        return _buffer.getShort(0);
    }

    public int xorInt(int rawVal) {
        _buffer.putInt(0, rawVal);
        xor(_buffer.array(), 0, 4);
        return _buffer.getInt(0);
    }

    public long xorLong(long rawVal) {
        _buffer.putLong(0, rawVal);
        xor(_buffer.array(), 0, 8);
        return _buffer.getLong(0);
    }
}
