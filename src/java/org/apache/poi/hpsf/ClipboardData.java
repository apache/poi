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

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

@Internal
public class ClipboardData {
    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000_000;

    private static final POILogger LOG = POILogFactory.getLogger( ClipboardData.class );

    private int _format;
    private byte[] _value;

    public void read( LittleEndianByteArrayInputStream lei ) {
        int offset = lei.getReadIndex();
        int size = lei.readInt();

        if ( size < 4 ) {
            String msg = 
                "ClipboardData at offset "+offset+" size less than 4 bytes "+
                "(doesn't even have format field!). Setting to format == 0 and hope for the best";
            LOG.log( POILogger.WARN, msg);
            _format = 0;
            _value = new byte[0];
            return;
        }

        _format = lei.readInt();
        _value = IOUtils.safelyAllocate(size - LittleEndianConsts.INT_SIZE, MAX_RECORD_LENGTH);
        lei.readFully(_value);
    }

    public byte[] getValue() {
        return _value;
    }

    public byte[] toByteArray() {
        byte[] result = new byte[LittleEndianConsts.INT_SIZE*2+_value.length];
        LittleEndianByteArrayOutputStream bos = new LittleEndianByteArrayOutputStream(result,0);
        try {
            bos.writeInt(LittleEndianConsts.INT_SIZE + _value.length);
            bos.writeInt(_format);
            bos.write(_value);
            return result;
        } finally {
            IOUtils.closeQuietly(bos);
        }
    }

    public void setValue( byte[] value ) {
        _value = value.clone();
    }
}
