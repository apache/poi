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
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndian;

/**
 * Common abstract class for {@link EscherOptRecord} and
 * {@link EscherTertiaryOptRecord}
 */
public abstract class AbstractEscherOptRecord extends EscherRecord {
    private final List<EscherProperty> properties = new ArrayList<>();

    protected AbstractEscherOptRecord() {}

    protected AbstractEscherOptRecord(AbstractEscherOptRecord other) {
        super(other);
        properties.addAll(other.properties);
    }


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
        if (bytesRemaining < 0) {
            throw new IllegalStateException("Invalid value for bytesRemaining: " + bytesRemaining);
        }
        short propertiesCount = readInstance( data, offset );
        int pos = offset + 8;

        EscherPropertyFactory f = new EscherPropertyFactory();
        properties.clear();
        properties.addAll( f.createProperties( data, pos, propertiesCount ) );
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

    public <T extends EscherProperty> T lookup( EscherPropertyTypes propType ) {
        return lookup(propType.propNumber);
    }

    @SuppressWarnings( "unchecked" )
    public <T extends EscherProperty> T lookup( int propId ) {
        return (T)properties.stream().filter(p -> p.getPropertyNumber() == propId).findFirst().orElse(null);
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
    public void sortProperties() {
        properties.sort(Comparator.comparingInt(EscherProperty::getPropertyNumber));
    }

    /**
     * Set an escher property. If a property with given propId already exists it is replaced.
     *
     * @param value the property to set.
     */
    public void setEscherProperty(EscherProperty value){
        properties.removeIf(prop -> prop.getId() == value.getId());
        properties.add( value );
        sortProperties();
    }

    public void removeEscherProperty(EscherPropertyTypes type){
        properties.removeIf(prop -> prop.getPropertyNumber() == type.propNumber);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "base", super::getGenericProperties,
            "isContainer", this::isContainerRecord,
            "properties", this::getEscherProperties
        );
    }
}
