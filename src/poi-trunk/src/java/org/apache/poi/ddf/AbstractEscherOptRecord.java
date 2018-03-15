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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.util.LittleEndian;

/**
 * Common abstract class for {@link EscherOptRecord} and
 * {@link EscherTertiaryOptRecord}
 */
public abstract class AbstractEscherOptRecord extends EscherRecord
{
    private List<EscherProperty> properties = new ArrayList<>();

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
        properties.sort(new Comparator<EscherProperty>() {
            @Override
            public int compare(EscherProperty p1, EscherProperty p2) {
                short s1 = p1.getPropertyNumber();
                short s2 = p2.getPropertyNumber();
                return Short.compare(s1, s2);
            }
        });
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

    @Override
    protected Object[][] getAttributeMap() {
        List<Object> attrList = new ArrayList<>(properties.size() * 2 + 2);
        attrList.add("properties");
        attrList.add(properties.size());
        for ( EscherProperty property : properties ) {
            attrList.add(property.getName());
            attrList.add(property);
        }
        
        return new Object[][]{
            { "isContainer", isContainerRecord() },
            { "numchildren", getChildRecords().size() },
            attrList.toArray()
        };
    }
}
