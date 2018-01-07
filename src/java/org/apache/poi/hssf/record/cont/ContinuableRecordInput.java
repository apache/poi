/*
 *  ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.apache.poi.hssf.record.cont;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.util.LittleEndianInput;

/**
 * A decorated {@link RecordInputStream} that can read primitive data types
 * (short, int, long, etc.) spanned across a {@link ContinueRecord } boundary.
 *
 * <p>
 *  Most records construct themselves from {@link RecordInputStream}.
 *  This class assumes that a {@link ContinueRecord} record break always occurs at the type boundary,
 *  however, it is not always so.
 * </p>
 *  Two  attachments to <a href="https://issues.apache.org/bugzilla/show_bug.cgi?id=50779">Bugzilla 50779</a>
 *  demonstrate that a CONTINUE break can appear right in between two bytes of a unicode character
 *  or between two bytes of a <code>short</code>. The problematic portion of the data is
 *  in a Asian Phonetic Settings Block (ExtRst) of a UnicodeString.
 * <p>
 *  {@link RecordInputStream} greedily requests the bytes to be read and stumbles on such files with a
 *  "Not enough data (1) to read requested (2) bytes" exception.  The <code>ContinuableRecordInput</code>
 *   class circumvents this "type boundary" rule and reads data byte-by-byte rolling over CONTINUE if necessary.
 * </p>
 *
 * <p>
 * YK: For now (March 2011) this class is only used to read
 *   @see org.apache.poi.hssf.record.common.UnicodeString.ExtRst blocks of a UnicodeString.
 *
 * </p>
 */
public class ContinuableRecordInput implements LittleEndianInput {
    private final RecordInputStream _in;

    public ContinuableRecordInput(RecordInputStream in){
        _in = in;
    }
    @Override
    public int available(){
        return _in.available();
    }

    @Override
    public byte readByte(){
        return _in.readByte();
    }

    @Override
    public int readUByte(){
        return _in.readUByte();
    }

    @Override
    public short readShort(){
        return _in.readShort();
    }

    @Override
    public int readUShort(){
        int ch1 = readUByte();
        int ch2 = readUByte();
        return (ch2 << 8) + (ch1 << 0);
    }

    @Override
    public int readInt(){
        int ch1 = _in.readUByte();
        int ch2 = _in.readUByte();
        int ch3 = _in.readUByte();
        int ch4 = _in.readUByte();
        return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
    }

    @Override
    public long readLong(){
        int b0 = _in.readUByte();
        int b1 = _in.readUByte();
        int b2 = _in.readUByte();
        int b3 = _in.readUByte();
        int b4 = _in.readUByte();
        int b5 = _in.readUByte();
        int b6 = _in.readUByte();
        int b7 = _in.readUByte();
        return (((long)b7 << 56) +
                ((long)b6 << 48) +
                ((long)b5 << 40) +
                ((long)b4 << 32) +
                ((long)b3 << 24) +
                (b2 << 16) +
                (b1 <<  8) +
                (b0 <<  0));
    }

    @Override
    public double readDouble(){
        return _in.readDouble();
    }

    @Override
    public void readFully(byte[] buf){
        _in.readFully(buf);
    }

    @Override
    public void readFully(byte[] buf, int off, int len){
        _in.readFully(buf, off, len);
    }
    
    @Override
    public void readPlain(byte[] buf, int off, int len) {
        readFully(buf, off, len);
    }
}
