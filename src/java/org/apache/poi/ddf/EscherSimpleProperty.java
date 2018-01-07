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

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

/**
 * A simple property is of fixed length and as a property number in addition
 * to a 32-bit value.  Properties that can't be stored in only 32-bits are
 * stored as EscherComplexProperty objects.
 */
public class EscherSimpleProperty extends EscherProperty
{
    private int propertyValue;

    /**
     * The id is distinct from the actual property number.  The id includes the property number the blip id
     * flag and an indicator whether the property is complex or not.
     * 
     * @param id the property id
     * @param propertyValue the property value
     */
    public EscherSimpleProperty( short id, int propertyValue )
    {
        super( id );
        this.propertyValue = propertyValue;
    }

    /**
     * Constructs a new escher property.  The three parameters are combined to form a property id.
     * 
     * @param propertyNumber the property number
     * @param isComplex true, if its a complex property
     * @param isBlipId true, if its a blip
     * @param propertyValue the property value
     */
    public EscherSimpleProperty( short propertyNumber, boolean isComplex, boolean isBlipId, int propertyValue )
    {
        super( propertyNumber, isComplex, isBlipId );
        this.propertyValue = propertyValue;
    }

    /**
     * Serialize the simple part of the escher record.
     *
     * @return the number of bytes serialized.
     */
    @Override
    public int serializeSimplePart( byte[] data, int offset )
    {
        LittleEndian.putShort(data, offset, getId());
        LittleEndian.putInt(data, offset + 2, propertyValue);
        return 6;
    }

    /**
     * Escher properties consist of a simple fixed length part and a complex variable length part.
     * The fixed length part is serialized first.
     */
    @Override
    public int serializeComplexPart( byte[] data, int pos )
    {
        return 0;
    }

    /**
     * @return  Return the 32 bit value of this property.
     */
    public int getPropertyValue()
    {
        return propertyValue;
    }

    /**
     * Returns true if one escher property is equal to another.
     */
    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof EscherSimpleProperty ) ) {
            return false;
        }

        final EscherSimpleProperty escherSimpleProperty = (EscherSimpleProperty) o;

        if ( propertyValue != escherSimpleProperty.propertyValue ) {
            return false;
        }
        if ( getId() != escherSimpleProperty.getId() ) {
            return false;
        }

        return true;
    }

    /**
     * Returns a hashcode so that this object can be stored in collections that
     * require the use of such things.
     */
    @Override
    public int hashCode()
    {
        return propertyValue;
    }

    /**
     * @return the string representation of this property.
     */
    @Override
    public String toString()
    {
        return "propNum: " + getPropertyNumber()
                + ", RAW: 0x" + HexDump.toHex( getId() )
                + ", propName: " + EscherProperties.getPropertyName( getPropertyNumber() )
                + ", complex: " + isComplex()
                + ", blipId: " + isBlipId()
                + ", value: " + propertyValue + " (0x" + HexDump.toHex(propertyValue) + ")";
    }

    @Override
    public String toXml(String tab){
        StringBuilder builder = new StringBuilder();
        builder.append(tab).append("<").append(getClass().getSimpleName()).append(" id=\"0x").append(HexDump.toHex(getId()))
                .append("\" name=\"").append(getName()).append("\" blipId=\"")
                .append(isBlipId()).append("\" complex=\"").append(isComplex()).append("\" value=\"").append("0x")
                .append(HexDump.toHex(propertyValue)).append("\"/>");
        return builder.toString();
    }
}
