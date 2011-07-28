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
package org.apache.poi.ddf;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

/**
 * "The OfficeArtTertiaryFOPT record specifies a table of OfficeArtRGFOPTE properties, as defined in section 2.3.1."
 * -- [MS-ODRAW] — v20110608; Office Drawing Binary File Format
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
public class EscherTertiaryOptRecord extends EscherRecord
{
    public static final short RECORD_ID = (short) 0xF122;

    private List<EscherProperty> properties = new ArrayList<EscherProperty>();

    public int fillFields( byte[] data, int offset,
            EscherRecordFactory recordFactory )
    {
        int bytesRemaining = readHeader( data, offset );
        int pos = offset + 8;

        EscherPropertyFactory f = new EscherPropertyFactory();
        properties = f.createProperties( data, pos, getInstance() );
        return bytesRemaining + 8;
    }

    private int getPropertiesSize()
    {
        int totalSize = 0;
        for ( EscherProperty property : properties )
        {
            totalSize += property.getPropertySize();
        }

        return totalSize;
    }

    public String getRecordName()
    {
        return "TertiaryOpt";
    }

    @Override
    public int getRecordSize()
    {
        return 8 + getPropertiesSize();
    }

    public int serialize( int offset, byte[] data,
            EscherSerializationListener listener )
    {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        LittleEndian.putShort( data, offset, getOptions() );
        LittleEndian.putShort( data, offset + 2, getRecordId() );
        LittleEndian.putInt( data, offset + 4, getPropertiesSize() );
        int pos = offset + 8;
        for ( EscherProperty property : properties )
        {
            pos += property.serializeSimplePart( data, pos );
        }
        for ( EscherProperty property : properties )
        {
            pos += property.serializeComplexPart( data, pos );
        }
        listener.afterRecordSerialize( pos, getRecordId(), pos - offset, this );
        return pos - offset;
    }

    /**
     * Retrieve the string representation of this record.
     */
    public String toString()
    {
        String nl = System.getProperty( "line.separator" );
        StringBuffer propertiesBuf = new StringBuffer();
        for ( EscherProperty property : properties )
        {
            propertiesBuf.append( "    " + property.toString() + nl );
        }

        return "org.apache.poi.ddf.EscherTertiaryOptRecord:" + nl
                + "  isContainer: " + isContainerRecord() + nl
                + "  options: 0x" + HexDump.toHex( getOptions() ) + nl
                + "  recordId: 0x" + HexDump.toHex( getRecordId() ) + nl
                + "  numchildren: " + getChildRecords().size() + nl
                + "  properties:" + nl + propertiesBuf.toString();
    }

}
