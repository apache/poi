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

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.StringUtil;

@Internal
class UnicodeString
{

    private final static POILogger logger = POILogFactory
            .getLogger( UnicodeString.class );

    private byte[] _value;

    UnicodeString( byte[] data, int offset )
    {
        int length = LittleEndian.getInt( data, offset );

        if ( length == 0 )
        {
            _value = new byte[0];
            return;
        }

        _value = LittleEndian.getByteArray( data, offset
                + LittleEndian.INT_SIZE, length * 2 );

        if ( _value[length * 2 - 1] != 0 || _value[length * 2 - 2] != 0 )
            throw new IllegalPropertySetDataException(
                    "UnicodeString started at offset #" + offset
                            + " is not NULL-terminated" );
    }

    int getSize()
    {
        return LittleEndian.INT_SIZE + _value.length;
    }

    byte[] getValue()
    {
        return _value;
    }

    String toJavaString()
    {
        if ( _value.length == 0 )
            return null;

        String result = StringUtil.getFromUnicodeLE( _value, 0,
                _value.length >> 1 );

        final int terminator = result.indexOf( '\0' );
        if ( terminator == -1 )
        {
            logger.log(
                    POILogger.WARN,
                    "String terminator (\\0) for UnicodeString property value not found."
                            + "Continue without trimming and hope for the best." );
            return result;
        }
        if ( terminator != result.length() - 1 )
        {
            logger.log(
                    POILogger.WARN,
                    "String terminator (\\0) for UnicodeString property value occured before the end of string. "
                            + "Trimming and hope for the best." );
        }
        return result.substring( 0, terminator );
    }
}
