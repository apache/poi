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
import java.util.function.BiFunction;

import org.apache.poi.util.LittleEndian;

/**
 * Generates a property given a reference into the byte array storing that property.
 */
public final class EscherPropertyFactory {

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
            final short propId = LittleEndian.getShort( data, pos );
            final int propData = LittleEndian.getInt( data, pos + 2 );
            final boolean isComplex = ( propId & EscherProperty.IS_COMPLEX ) != 0;

            EscherPropertyTypes propertyType = EscherPropertyTypes.forPropertyID(propId);

            final BiFunction<Short,Integer,EscherProperty> con;
            switch (propertyType.holder) {
                case BOOLEAN:
                    con = EscherBoolProperty::new;
                    break;
                case RGB:
                    con = EscherRGBProperty::new;
                    break;
                case SHAPE_PATH:
                    con = EscherShapePathProperty::new;
                    break;
                default:
                    if ( isComplex ) {
                        con = (propertyType.holder == EscherPropertyTypesHolder.ARRAY)
                            ? EscherArrayProperty::new
                            : EscherComplexProperty::new;
                    } else {
                        con = EscherSimpleProperty::new;
                    }
                    break;
            }

            results.add( con.apply(propId,propData) );
            pos += 6;
        }

        // Get complex data
        for (EscherProperty p : results) {
            if (p instanceof EscherArrayProperty) {
                EscherArrayProperty eap = (EscherArrayProperty)p;
                pos += eap.setArrayData(data, pos);
            } else if (p instanceof EscherComplexProperty) {
                EscherComplexProperty ecp = (EscherComplexProperty)p;
                int cdLen = ecp.getComplexData().length;
                int leftover = data.length - pos;
                if (leftover < cdLen) {
                    throw new IllegalStateException("Could not read complex escher property, length was " +
                        cdLen + ", but had only " + leftover + " bytes left");
                }
                pos += ecp.setComplexData(data, pos);
            }
        }
        return results;
    }
}
