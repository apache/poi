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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

/**
 * Common abstract class for {@link EscherOptRecord} and
 * {@link EscherTertiaryOptRecord}
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 * @author Glen Stampoultzis
 */
public abstract class AbstractEscherOptRecord extends EscherRecord
{
    protected List<EscherProperty> properties = new ArrayList<EscherProperty>();

    /**
     * Add a property to this record.
     */
    public void addEscherProperty( EscherProperty prop )
    {
        properties.add( prop );
    }

    public int fillFields( byte[] data, int offset,
            EscherRecordFactory recordFactory )
    {
        int bytesRemaining = readHeader( data, offset );
        int pos = offset + 8;

        EscherPropertyFactory f = new EscherPropertyFactory();
        properties = f.createProperties( data, pos, getInstance() );
        return bytesRemaining + 8;
    }

    /**
     * The list of properties stored by this record.
     */
    public List<EscherProperty> getEscherProperties()
    {
        return properties;
    }

    /**
     * The list of properties stored by this record.
     */
    public EscherProperty getEscherProperty( int index )
    {
        return properties.get( index );
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

    @Override
    public int getRecordSize()
    {
        return 8 + getPropertiesSize();
    }

    public <T extends EscherProperty> T lookup( int propId )
    {
        for ( EscherProperty prop : properties )
        {
            if ( prop.getPropertyNumber() == propId )
            {
                @SuppressWarnings( "unchecked" )
                final T result = (T) prop;
                return result;
            }
        }
        return null;
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
     * Records should be sorted by property number before being stored.
     */
    public void sortProperties()
    {
        Collections.sort( properties, new Comparator<EscherProperty>()
        {
            public int compare( EscherProperty p1, EscherProperty p2 )
            {
                short s1 = p1.getPropertyNumber();
                short s2 = p2.getPropertyNumber();
                return s1 < s2 ? -1 : s1 == s2 ? 0 : 1;
            }
        } );
    }

    /**
     * Retrieve the string representation of this record.
     */
    public String toString()
    {
        String nl = System.getProperty( "line.separator" );

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( getClass().getName() );
        stringBuilder.append( ":" );
        stringBuilder.append( nl );
        stringBuilder.append( "  isContainer: " );
        stringBuilder.append( isContainerRecord() );
        stringBuilder.append( nl );
        stringBuilder.append( "  options: 0x" );
        stringBuilder.append( HexDump.toHex( getOptions() ) );
        stringBuilder.append( nl );
        stringBuilder.append( "  recordId: 0x" );
        stringBuilder.append( HexDump.toHex( getRecordId() ) );
        stringBuilder.append( nl );
        stringBuilder.append( "  numchildren: " );
        stringBuilder.append( getChildRecords().size() );
        stringBuilder.append( nl );
        stringBuilder.append( "  properties:" );
        stringBuilder.append( nl );

        for ( EscherProperty property : properties )
        {
            stringBuilder.append( "    " + property.toString() + nl );
        }

        return stringBuilder.toString();
    }

}
