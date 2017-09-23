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

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * Generates a property given a reference into the byte array storing that property.
 */
public final class EscherPropertyFactory {

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000_000;

    /**
     * Create new properties from a byte array.
     *
     * @param data              The byte array containing the property
     * @param offset            The starting offset into the byte array
     * @param numProperties     The number of properties to be read
     * @return                  The new properties
     */
    public List<EscherProperty> createProperties(byte[] data, int offset, short numProperties) {
        List<EscherProperty> results = new ArrayList<>();

        int pos = offset;

        for (int i = 0; i < numProperties; i++) {
            short propId;
            int propData;
            propId = LittleEndian.getShort( data, pos );
            propData = LittleEndian.getInt( data, pos + 2 );
            short propNumber = (short) ( propId & (short) 0x3FFF );
            boolean isComplex = ( propId & (short) 0x8000 ) != 0;
            // boolean isBlipId = ( propId & (short) 0x4000 ) != 0;

            byte propertyType = EscherProperties.getPropertyType(propNumber);
            EscherProperty ep;
            switch (propertyType) {
                case EscherPropertyMetaData.TYPE_BOOLEAN:
                    ep = new EscherBoolProperty( propId, propData );
                    break;
                case EscherPropertyMetaData.TYPE_RGB:
                    ep = new EscherRGBProperty( propId, propData );
                    break;
                case EscherPropertyMetaData.TYPE_SHAPEPATH:
                    ep = new EscherShapePathProperty( propId, propData );
                    break;
                default:
                    if ( !isComplex ) {
                        ep = new EscherSimpleProperty( propId, propData );
                    } else if ( propertyType == EscherPropertyMetaData.TYPE_ARRAY) {
                        ep = new EscherArrayProperty( propId, IOUtils.safelyAllocate(propData, MAX_RECORD_LENGTH));
                    } else {
                        ep = new EscherComplexProperty( propId, IOUtils.safelyAllocate(propData, MAX_RECORD_LENGTH));
                    }
                    break;
            }
            results.add( ep );
            pos += 6;
        }

        // Get complex data
        for (EscherProperty p : results) {
            if (p instanceof EscherComplexProperty) {
                if (p instanceof EscherArrayProperty) {
                    pos += ((EscherArrayProperty)p).setArrayData(data, pos);
                } else {
                    byte[] complexData = ((EscherComplexProperty)p).getComplexData();

                    int leftover = data.length - pos;
                    if (leftover < complexData.length) {
                        throw new IllegalStateException("Could not read complex escher property, length was " + complexData.length + ", but had only " +
                                leftover + " bytes left");
                    }

                    System.arraycopy(data, pos, complexData, 0, complexData.length);
                    pos += complexData.length;
                }
            }
        }
        return results;
    }
}
