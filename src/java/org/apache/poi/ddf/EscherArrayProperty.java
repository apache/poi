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
import org.apache.poi.util.HexDump;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Escher array properties are the most wierd construction ever invented
 * with all sorts of special cases.  I'm hopeful I've got them all.
 *
 * @author Glen Stampoultzis (glens at superlinksoftware.com)
 */
public class EscherArrayProperty
        extends EscherComplexProperty
{
    private static final int FIXED_SIZE = 3 * 2;

    public EscherArrayProperty( short id, byte[] complexData )
    {
        super( id, checkComplexData(complexData) );
    }

    public EscherArrayProperty( short propertyNumber, boolean isBlipId, byte[] complexData )
    {
        super( propertyNumber, isBlipId, checkComplexData(complexData) );
    }

    private static byte[] checkComplexData( byte[] complexData )
    {
        if (complexData == null || complexData.length == 0)
            complexData = new byte[6];

        return complexData;
    }

    public int getNumberOfElementsInArray()
    {
        return LittleEndian.getUShort( complexData, 0 );
    }

    public void setNumberOfElementsInArray( int numberOfElements )
    {
        int expectedArraySize = numberOfElements * getActualSizeOfElements(getSizeOfElements()) + FIXED_SIZE;
        if ( expectedArraySize != complexData.length )
        {
            byte[] newArray = new byte[expectedArraySize];
            System.arraycopy( complexData, 0, newArray, 0, complexData.length );
            complexData = newArray;
        }
        LittleEndian.putShort( complexData, 0, (short) numberOfElements );
    }

    public int getNumberOfElementsInMemory()
    {
        return LittleEndian.getUShort( complexData, 2 );
    }

    public void setNumberOfElementsInMemory( int numberOfElements )
    {
        int expectedArraySize = numberOfElements * getActualSizeOfElements(getSizeOfElements()) + FIXED_SIZE;
        if ( expectedArraySize != complexData.length )
        {
            byte[] newArray = new byte[expectedArraySize];
            System.arraycopy( complexData, 0, newArray, 0, expectedArraySize );
            complexData = newArray;
        }
        LittleEndian.putShort( complexData, 2, (short) numberOfElements );
    }

    public short getSizeOfElements()
    {
        return LittleEndian.getShort( complexData, 4 );
    }

    public void setSizeOfElements( int sizeOfElements )
    {
        LittleEndian.putShort( complexData, 4, (short) sizeOfElements );

        int expectedArraySize = getNumberOfElementsInArray() * getActualSizeOfElements(getSizeOfElements()) + FIXED_SIZE;
        if ( expectedArraySize != complexData.length )
        {
            // Keep just the first 6 bytes.  The rest is no good to us anyway.
            byte[] newArray = new byte[expectedArraySize];
            System.arraycopy( complexData, 0, newArray, 0, 6 );
            complexData = newArray;
        }
    }

    public byte[] getElement( int index )
    {
        int actualSize = getActualSizeOfElements(getSizeOfElements());
        byte[] result = new byte[actualSize];
        System.arraycopy(complexData, FIXED_SIZE + index * actualSize, result, 0, result.length );
        return result;
    }

    public void setElement( int index, byte[] element )
    {
        int actualSize = getActualSizeOfElements(getSizeOfElements());
        System.arraycopy( element, 0, complexData, FIXED_SIZE + index * actualSize, actualSize);
    }

    public String toString()
    {
        String nl = System.getProperty("line.separator");

        StringBuffer results = new StringBuffer();
        results.append("    {EscherArrayProperty:" + nl);
        results.append("     Num Elements: " + getNumberOfElementsInArray() + nl);
        results.append("     Num Elements In Memory: " + getNumberOfElementsInMemory() + nl);
        results.append("     Size of elements: " + getSizeOfElements() + nl);
        for (int i = 0; i < getNumberOfElementsInArray(); i++)
        {
            results.append("     Element " + i + ": " + HexDump.toHex(getElement(i)) + nl);
        }
        results.append("}" + nl);

        return "propNum: " + getPropertyNumber()
                + ", propName: " + EscherProperties.getPropertyName( getPropertyNumber() )
                + ", complex: " + isComplex()
                + ", blipId: " + isBlipId()
                + ", data: " + nl + results.toString();
    }

    /**
     * We have this method because the way in which arrays in escher works
     * is screwed for seemly arbitary reasons.  While most properties are
     * fairly consistent and have a predictable array size, escher arrays
     * have special cases.
     *
     * @param data      The data array containing the escher array information
     * @param offset    The offset into the array to start reading from.
     * @return  the number of bytes used by this complex property.
     */
    public int setArrayData( byte[] data, int offset )
    {
        short numElements = LittleEndian.getShort(data, offset);
        short numReserved = LittleEndian.getShort(data, offset + 2);
        short sizeOfElements = LittleEndian.getShort(data, offset + 4);

        int arraySize = getActualSizeOfElements(sizeOfElements) * numElements;
        if (arraySize == complexData.length)
            complexData = new byte[arraySize + 6];  // Calculation missing the header for some reason
        System.arraycopy(data, offset, complexData, 0, complexData.length );
        return complexData.length;
    }

    /**
     * Sometimes the element size is stored as a negative number.  We
     * negate it and shift it to get the real value.
     */
    public static int getActualSizeOfElements(short sizeOfElements)
    {
        if (sizeOfElements < 0)
            return (short) ( ( -sizeOfElements ) >> 2 );
        else
            return sizeOfElements;
    }

}
