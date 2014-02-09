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

package org.apache.poi.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wraps an {@link InputStream} providing {@link LittleEndianInput}<p/>
 *
 * This class does not buffer any input, so the stream read position maintained
 * by this class is consistent with that of the inner stream.
 *
 * @author Josh Micich
 */
public class LittleEndianInputStream extends FilterInputStream implements LittleEndianInput {
    private byte _buf[] = new byte[LittleEndianConsts.LONG_SIZE];
    
	public LittleEndianInputStream(InputStream is) {
		super(is);
	}
	
	public int available() {
		try {
			return super.available();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public byte readByte() {
	    readFully(_buf, 0, LittleEndianConsts.BYTE_SIZE);
	    return _buf[0];
	}
	
	public int readUByte() {
        readFully(_buf, 0, LittleEndianConsts.BYTE_SIZE);
        return LittleEndian.getUByte(_buf);
	}
	
    public short readShort() {
        readFully(_buf, 0, LittleEndianConsts.SHORT_SIZE);
        return LittleEndian.getShort(_buf);
    }
    
    public int readUShort() {
        readFully(_buf, 0, LittleEndianConsts.SHORT_SIZE);
        return LittleEndian.getUShort(_buf);
    }
    
	public int readInt() {
        readFully(_buf, 0, LittleEndianConsts.INT_SIZE);
	    return LittleEndian.getInt(_buf);
	}

    public long readUInt() {
        readFully(_buf, 0, LittleEndianConsts.INT_SIZE);
        return LittleEndian.getUInt(_buf);
    }
	
	public long readLong() {
        readFully(_buf, 0, LittleEndianConsts.LONG_SIZE);
        return LittleEndian.getLong(_buf);
	}
	
    public double readDouble() {
        readFully(_buf, 0, LittleEndianConsts.LONG_SIZE);
        return LittleEndian.getDouble(_buf);
    }
    
	public void readFully(byte[] buf) {
		readFully(buf, 0, buf.length);
	}

	public void readFully(byte[] buf, int off, int len) {
	    if (buf == null || buf.length == 0) {
	        return;
	    }
	    
	    int readBytes;
	    try {
	        readBytes = super.read(buf, off, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
	    }
	    
        if (readBytes == -1 || readBytes < len ) {
            throw new RuntimeException("Unexpected end-of-file");
        }
	}
}
