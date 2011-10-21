package org.apache.poi.hpsf;

import org.apache.poi.util.LittleEndian;

class ClipboardData
{
    private int _format;
    private byte[] _value;

    ClipboardData( byte[] data, int offset )
    {
        int size = LittleEndian.getInt( data, offset );

        if ( size < 4 )
            throw new IllegalPropertySetDataException(
                    "ClipboardData size less than 4 bytes "
                            + "(doesn't even have format field!)" );
        _format = LittleEndian.getInt( data, offset + LittleEndian.INT_SIZE );
        _value = LittleEndian.getByteArray( data, offset
                + LittleEndian.INT_SIZE * 2, size - LittleEndian.INT_SIZE );
    }

    int getSize()
    {
        return LittleEndian.INT_SIZE * 2 + _value.length;
    }
}
