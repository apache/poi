package org.apache.poi.hpsf;

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * Holder for vector-type properties
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
@Internal
class Vector
{
    private final short _type;

    private TypedPropertyValue[] _values;

    Vector( short type )
    {
        this._type = type;
    }

    Vector( byte[] data, int startOffset, short type )
    {
        this._type = type;
        read( data, startOffset );
    }

    int read( byte[] data, int startOffset )
    {
        int offset = startOffset;

        final long longLength = LittleEndian.getUInt( data, offset );
        offset += LittleEndian.INT_SIZE;

        if ( longLength > Integer.MAX_VALUE )
            throw new UnsupportedOperationException( "Vector is too long -- "
                    + longLength );
        final int length = (int) longLength;

        _values = new TypedPropertyValue[length];

        if ( _type == Variant.VT_VARIANT )
        {
            for ( int i = 0; i < length; i++ )
            {
                TypedPropertyValue value = new TypedPropertyValue();
                offset += value.read( data, offset );
                _values[i] = value;
            }
        }
        else
        {
            for ( int i = 0; i < length; i++ )
            {
                TypedPropertyValue value = new TypedPropertyValue( _type, null );
                offset += value.readValuePadded( data, offset );
                _values[i] = value;
            }
        }
        return offset - startOffset;
    }

}
