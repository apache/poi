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
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * A complex property differs from a simple property in that the data can not fit inside a 32 bit
 * integer.  See the specification for more detailed information regarding exactly what is
 * stored here.
 */
public class EscherComplexProperty extends EscherProperty {
    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 100_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    private byte[] complexData;

    /**
     * @param length the max record length allowed for EscherComplexProperty
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for EscherComplexProperty
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    /**
     * Create a complex property using the property id and a byte array containing the complex
     * data value size.
     *
     * @param id          The id consists of the property number, a flag indicating whether this is a blip id and a flag
     *                    indicating that this is a complex property.
     * @param complexSize The byte size of this property.
     */
    public EscherComplexProperty(short id, int complexSize) {
        super((short)(id | IS_COMPLEX));
        complexData = IOUtils.safelyAllocate(complexSize, MAX_RECORD_LENGTH);
    }

    /**
     * Create a complex property using the property number, a flag to indicate whether this is a
     * blip reference and the complex property data size.
     *
     * @param propertyNumber The property number
     * @param isBlipId       Whether this is a blip id.  Should be false.
     * @param complexSize    The byte size of this property.
     */
    public EscherComplexProperty(short propertyNumber, boolean isBlipId, int complexSize) {
        this((short)(propertyNumber | (isBlipId ? IS_BLIP : 0)), complexSize);
    }

    /**
     * Create a complex property using the property type, a flag to indicate whether this is a
     * blip reference and the complex property data size.
     *
     * @param type           The property type
     * @param isBlipId       Whether this is a blip id.  Should be false.
     * @param complexSize    The byte size of this property.
     */
    public EscherComplexProperty(EscherPropertyTypes type, boolean isBlipId, int complexSize) {
        this((short)(type.propNumber | (isBlipId ? IS_BLIP : 0)), complexSize);
    }

    /**
     * Serializes the simple part of this property.  i.e. the first 6 bytes.
     */
    @Override
    public int serializeSimplePart(byte[] data, int pos) {
        LittleEndian.putShort(data, pos, getId());
        LittleEndian.putInt(data, pos + 2, complexData.length);
        return 6;
    }

    /**
     * Serializes the complex part of this property
     *
     * @param data The data array to serialize to
     * @param pos  The offset within data to start serializing to.
     * @return The number of bytes serialized.
     */
    @Override
    public int serializeComplexPart(byte[] data, int pos) {
        System.arraycopy(complexData, 0, data, pos, complexData.length);
        return complexData.length;
    }

    /**
     * Get the complex data value.
     *
     * @return the complex bytes
     */
    public byte[] getComplexData() {
        return complexData;
    }

    public int setComplexData(byte[] complexData) {
        return setComplexData(complexData, 0);
    }

    public int setComplexData(byte[] complexData, int offset) {
        if (complexData == null) {
            return 0;
        } else {
            int copySize = Math.max(0, Math.min(this.complexData.length, complexData.length - offset));
            System.arraycopy(complexData, offset, this.complexData, 0, copySize);
            return copySize;
        }
    }



    protected void resizeComplexData(int newSize) {
        resizeComplexData(newSize, Integer.MAX_VALUE);
    }

    protected void resizeComplexData(int newSize, int copyLen) {
        if (newSize == complexData.length) {
            return;
        }
        byte[] newArray = IOUtils.safelyAllocate(newSize, MAX_RECORD_LENGTH);
        System.arraycopy(complexData, 0, newArray, 0, Math.min(Math.min(complexData.length, copyLen),newSize));
        complexData = newArray;
    }

    /**
     * Determine whether this property is equal to another property.
     *
     * @param o The object to compare to.
     * @return True if the objects are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EscherComplexProperty)) {
            return false;
        }

        EscherComplexProperty escherComplexProperty = (EscherComplexProperty) o;

        return Arrays.equals(complexData, escherComplexProperty.complexData);
    }

    /**
     * Calculates the number of bytes required to serialize this property.
     *
     * @return Number of bytes
     */
    @Override
    public int getPropertySize() {
        return 6 + complexData.length;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{complexData, getId()});
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "base", super::getGenericProperties,
            "data", this::getComplexData
        );
    }
}
