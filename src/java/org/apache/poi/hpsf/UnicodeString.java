package org.apache.poi.hpsf;

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

@Internal
class UnicodeString
{
    private byte[] _value;

    UnicodeString( byte[] data, int offset )
    {
        int length = LittleEndian.getInt( data, offset );

        if ( length == 0 )
        {
            _value = new byte[0];
            return;
        }

        _value = new byte[length * 2];
        LittleEndian.getByteArray( data, offset + LittleEndian.INT_SIZE,
                length * 2 );

        if ( _value[length * 2 - 1] != 0 || _value[length * 2 - 2] != 0 )
            throw new IllegalPropertySetDataException(
                    "UnicodeString started at offset #" + offset
                            + " is not NULL-terminated" );
    }

    int getSize()
    {
        return LittleEndian.INT_SIZE + _value.length;
    }
}
