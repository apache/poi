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
package org.apache.poi.hpsf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.poi.util.CodePageUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.StringUtil;

@Internal
public class UnicodeString {
    private static final POILogger LOG = POILogFactory.getLogger( UnicodeString.class );
    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    private byte[] _value;

    public void read(LittleEndianByteArrayInputStream lei) {
        final int length = lei.readInt();
        final int unicodeBytes = length*2;
        _value = IOUtils.safelyAllocate(unicodeBytes, MAX_RECORD_LENGTH);
        
        // If Length is zero, this field MUST be zero bytes in length. If Length is
        // nonzero, this field MUST be a null-terminated array of 16-bit Unicode characters, followed by
        // zero padding to a multiple of 4 bytes. The string represented by this field SHOULD NOT
        // contain embedded or additional trailing null characters.
        
        if (length == 0) {
            return;
        }

        final int offset = lei.getReadIndex();
        
        lei.readFully(_value);

        if (_value[unicodeBytes-2] != 0 || _value[unicodeBytes-1] != 0) {
            String msg = "UnicodeString started at offset #" + offset + " is not NULL-terminated";
            throw new IllegalPropertySetDataException(msg);
        }
        
        TypedPropertyValue.skipPadding(lei);
    }
    
    public byte[] getValue() {
        return _value;
    }

    public String toJavaString() {
        if ( _value.length == 0 ) {
            return null;
        }

        String result = StringUtil.getFromUnicodeLE( _value, 0, _value.length >> 1 );

        final int terminator = result.indexOf( '\0' );
        if ( terminator == -1 ) {
            String msg =
                "String terminator (\\0) for UnicodeString property value not found. " +
                "Continue without trimming and hope for the best.";
            LOG.log(POILogger.WARN, msg);
            return result;
        }
        
        if ( terminator != result.length() - 1 ) {
            String msg =
                "String terminator (\\0) for UnicodeString property value occured before the end of string. " +
                "Trimming and hope for the best.";
            LOG.log(POILogger.WARN, msg);
        }
        return result.substring( 0, terminator );
    }

    public void setJavaValue( String string ) throws UnsupportedEncodingException {
        _value = CodePageUtil.getBytesInCodePage(string + "\0", CodePageUtil.CP_UNICODE);
    }

    public int write( OutputStream out ) throws IOException {
        LittleEndian.putUInt( _value.length / 2, out );
        out.write( _value );
        return LittleEndianConsts.INT_SIZE + _value.length;
    }
}
