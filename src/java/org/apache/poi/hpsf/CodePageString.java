package org.apache.poi.hpsf;

import org.apache.poi.util.LittleEndian;

import org.apache.poi.util.Internal;

@Internal
class CodePageString
{

    byte[] _value;

    CodePageString( final byte[] data, final int startOffset )
    {
        int offset = startOffset;

        int size = LittleEndian.getInt( data, offset );
        offset += LittleEndian.INT_SIZE;

        _value = LittleEndian.getByteArray( data, offset, size );
        if ( _value[size - 1] != 0 )
            throw new IllegalPropertySetDataException(
                    "CodePageString started at offset #" + offset
                            + " is not NULL-terminated" );
    }

    int getSize()
    {
        return LittleEndian.INT_SIZE + _value.length;
    }
}
