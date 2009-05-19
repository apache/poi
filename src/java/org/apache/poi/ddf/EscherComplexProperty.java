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

import java.util.Arrays;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

/**
 * A complex property differs from a simple property in that the data can not fit inside a 32 bit
 * integer.  See the specification for more detailed information regarding exactly what is
 * stored here.
 *
 * @author Glen Stampoultzis
 */
public class EscherComplexProperty extends EscherProperty {
    // TODO - make private and final
    protected byte[] _complexData;

    /**
     * Create a complex property using the property id and a byte array containing the complex
     * data value.
     *
     * @param id          The id consists of the property number, a flag indicating whether this is a blip id and a flag
     *                    indicating that this is a complex property.
     * @param complexData The value of this property.
     */
    public EscherComplexProperty(short id, byte[] complexData) {
        super(id);
        _complexData = complexData;
    }

    /**
     * Create a complex property using the property number, a flag to indicate whether this is a
     * blip reference and the complex property data.
     *
     * @param propertyNumber The property number
     * @param isBlipId       Whether this is a blip id.  Should be false.
     * @param complexData    The value of this complex property.
     */
    public EscherComplexProperty(short propertyNumber, boolean isBlipId, byte[] complexData) {
        super(propertyNumber, true, isBlipId);
        _complexData = complexData;
    }

    /**
     * Serializes the simple part of this property.  i.e. the first 6 bytes.
     */
    public int serializeSimplePart(byte[] data, int pos) {
        LittleEndian.putShort(data, pos, getId());
        LittleEndian.putInt(data, pos + 2, _complexData.length);
        return 6;
    }

    /**
     * Serializes the complex part of this property
     *
     * @param data The data array to serialize to
     * @param pos  The offset within data to start serializing to.
     * @return The number of bytes serialized.
     */
    public int serializeComplexPart(byte[] data, int pos) {
        System.arraycopy(_complexData, 0, data, pos, _complexData.length);
        return _complexData.length;
    }

    /**
     * Get the complex data value.
     */
    public byte[] getComplexData() {
        return _complexData;
    }

    /**
     * Determine whether this property is equal to another property.
     *
     * @param o The object to compare to.
     * @return True if the objects are equal.
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EscherComplexProperty)) {
            return false;
        }

        EscherComplexProperty escherComplexProperty = (EscherComplexProperty) o;

        if ( !Arrays.equals( _complexData, escherComplexProperty._complexData ) ) return false;

        return true;
    }

    /**
     * Calculates the number of bytes required to serialize this property.
     *
     * @return Number of bytes
     */
    public int getPropertySize() {
        return 6 + _complexData.length;
    }

    public int hashCode() {
        return getId() * 11;
    }

    /**
     * Retrieves the string representation for this property.
     */
    public String toString() {
        String dataStr = HexDump.toHex( _complexData, 32);

        return "propNum: " + getPropertyNumber()
                + ", propName: " + EscherProperties.getPropertyName( getPropertyNumber() )
                + ", complex: " + isComplex()
                + ", blipId: " + isBlipId()
                + ", data: " + System.getProperty("line.separator") + dataStr;
    }

}
