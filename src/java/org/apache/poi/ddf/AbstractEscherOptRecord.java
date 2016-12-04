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
import java.util.Iterator;
import java.util.List;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

/**
 * Common abstract class for {@link EscherOptRecord} and
 * {@link EscherTertiaryOptRecord}
 */
public abstract class AbstractEscherOptRecord extends EscherRecord
{
    private List<EscherProperty> properties = new ArrayList<EscherProperty>();

    /**
     * Add a property to this record.
     * 
     * @param prop the escher property to add
     */
    public void addEscherProperty( EscherProperty prop )
    {
        properties.add( prop );
    }

    @Override
    public int fillFields( byte[] data, int offset,
            EscherRecordFactory recordFactory )
    {
        int bytesRemaining = readHeader( data, offset );
        short propertiesCount = readInstance( data, offset );
        int pos = offset + 8;

        EscherPropertyFactory f = new EscherPropertyFactory();
        properties = f.createProperties( data, pos, propertiesCount );
        return bytesRemaining + 8;
    }

    /**
     * The list of properties stored by this record.
     * 
     * @return the list of properties
     */
    public List<EscherProperty> getEscherProperties()
    {
        return properties;
    }

    /**
     * The list of properties stored by this record.
     * 
     * @param index the ordinal index of the property
     * @return the escher property
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

    @Override
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
            @Override
            public int compare( EscherProperty p1, EscherProperty p2 )
            {
                short s1 = p1.getPropertyNumber();
                short s2 = p2.getPropertyNumber();
                return s1 < s2 ? -1 : s1 == s2 ? 0 : 1;
            }
        } );
    }

    /**
     * Set an escher property. If a property with given propId already
     exists it is replaced.
     *
     * @param value the property to set.
     */
    public void setEscherProperty(EscherProperty value){
        for ( Iterator<EscherProperty> iterator =
                      properties.iterator(); iterator.hasNext(); ) {
            EscherProperty prop = iterator.next();
            if (prop.getId() == value.getId()){
                iterator.remove();
            }
        }
        properties.add( value );
        sortProperties();
    }

    public void removeEscherProperty(int num){
        for ( Iterator<EscherProperty> iterator = getEscherProperties().iterator(); iterator.hasNext(); ) {
            EscherProperty prop = iterator.next();
            if (prop.getPropertyNumber() == num){
                iterator.remove();
            }
        }
    }

    /**
     * Retrieve the string representation of this record.
     */
    @Override
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
        stringBuilder.append( "  version: 0x" );
        stringBuilder.append( HexDump.toHex( getVersion() ) );
        stringBuilder.append( nl );
        stringBuilder.append( "  instance: 0x" );
        stringBuilder.append( HexDump.toHex( getInstance() ) );
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
            stringBuilder.append("    ").append(property.toString()).append(nl);
        }

        return stringBuilder.toString();
    }

    @Override
    public String toXml(String tab) {
        StringBuilder builder = new StringBuilder();
        builder.append(tab).append(formatXmlRecordHeader(getClass().getSimpleName(),
                HexDump.toHex(getRecordId()), HexDump.toHex(getVersion()), HexDump.toHex(getInstance())));
        for (EscherProperty property: getEscherProperties()){
            builder.append(property.toXml(tab+"\t"));
        }
        builder.append(tab).append("</").append(getClass().getSimpleName()).append(">\n");
        return builder.toString();
    }
}
