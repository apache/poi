/* ====================================================================
   Copyright 2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.ddf;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hssf.record.RecordFormatException;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Generates a property given a reference into the byte array storing that property.
 *
 * @author Glen Stampoultzis
 */
public class EscherPropertyFactory
{
    /**
     * Create new properties from a byte array.
     *
     * @param data              The byte array containing the property
     * @param offset            The starting offset into the byte array
     * @return                  The new properties
     */
    public List createProperties( byte[] data, int offset, short numProperties )
    {
        List results = new ArrayList();

        int pos = offset;
        int complexBytes = 0;
//        while ( bytesRemaining >= 6 )
        for (int i = 0; i < numProperties; i++)
        {
            short propId;
            int propData;
            propId = LittleEndian.getShort( data, pos );
            propData = LittleEndian.getInt( data, pos + 2 );
            short propNumber = (short) ( propId & (short) 0x3FFF );
            boolean isComplex = ( propId & (short) 0x8000 ) != 0;
            boolean isBlipId = ( propId & (short) 0x4000 ) != 0;
            if ( isComplex )
                complexBytes = propData;
            else
                complexBytes = 0;
            byte propertyType = EscherProperties.getPropertyType( (short) propNumber );
            if ( propertyType == EscherPropertyMetaData.TYPE_BOOLEAN )
                results.add( new EscherBoolProperty( propNumber, propData ) );
            else if ( propertyType == EscherPropertyMetaData.TYPE_RGB )
                results.add( new EscherRGBProperty( propNumber, propData ) );
            else if ( propertyType == EscherPropertyMetaData.TYPE_SHAPEPATH )
                results.add( new EscherShapePathProperty( propNumber, propData ) );
            else
            {
                if ( !isComplex )
                    results.add( new EscherSimpleProperty( propNumber, propData ) );
                else
                {
                    if ( propertyType == EscherPropertyMetaData.TYPE_ARRAY)
                        results.add( new EscherArrayProperty( propId, new byte[propData]) );
                    else
                        results.add( new EscherComplexProperty( propId, new byte[propData]) );

                }
            }
            pos += 6;
//            bytesRemaining -= 6 + complexBytes;
        }

        // Get complex data
        for ( Iterator iterator = results.iterator(); iterator.hasNext(); )
        {
            EscherProperty p = (EscherProperty) iterator.next();
            if (p instanceof EscherComplexProperty)
            {
                if (p instanceof EscherArrayProperty)
                {
                    pos += ((EscherArrayProperty)p).setArrayData(data, pos);
                }
                else
                {
                    byte[] complexData = ((EscherComplexProperty)p).getComplexData();
                    System.arraycopy(data, pos, complexData, 0, complexData.length);
                    pos += complexData.length;
                }
            }
        }

        return results;
    }


}
