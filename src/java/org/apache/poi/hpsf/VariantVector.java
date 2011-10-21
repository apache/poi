package org.apache.poi.hpsf;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.LittleEndian;

/**
 * Holder for vector-type properties
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
public class VariantVector
{

    private final List<Property> values;

    public VariantVector( int codepage, long id, long type, byte[] data, int startOffset )
            throws UnsupportedEncodingException, ReadingNotSupportedException
    {
        if ( ( type & 0x1000 ) != 0x1000 )
            throw new IllegalArgumentException( "Specified type is not vector" );
        final long elementType = type ^ 0x1000;

        int offset = startOffset;

        final long longLength = LittleEndian.getUInt( data, offset );
        offset += LittleEndian.INT_SIZE;

        if ( longLength > Integer.MAX_VALUE )
            throw new UnsupportedOperationException( "Vector is too long -- "
                    + longLength );
        final int length = (int) longLength;

        this.values = new ArrayList<Property>();
        for ( int i = 0; i < length; i++ )
        {
            Property property = new Property( id, elementType, null );
            VariantSupport.read( data, offset, length, elementType, codepage );
        }
    }
}
