package org.apache.poi.hpsf;

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

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

        _value = LittleEndian.getByteArray( data, offset
                + LittleEndian.INT_SIZE, length * 2 );

        if ( _value[length * 2 - 1] != 0 || _value[length * 2 - 2] != 0 )
            throw new IllegalPropertySetDataException(
                    "UnicodeString started at offset #" + offset
                            + " is not NULL-terminated" );
    }

    String toJavaString()
    {
        if ( _value.length == 0 )
            return null;

        return StringUtil.getFromUnicodeLE( _value, 0,
                ( _value.length - 2 ) >> 1 );
    }

    int getSize()
    {
        return LittleEndian.INT_SIZE + _value.length;
    }

    byte[] getValue()
    {
        return _value;
    }
}
