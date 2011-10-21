package org.apache.poi.hpsf;

import org.apache.poi.util.LittleEndian;

import org.apache.poi.util.Internal;

@Internal
class Date
{
    static final int SIZE = 8;

    private byte[] _value;

    Date( byte[] data, int offset )
    {
        _value = LittleEndian.getByteArray( data, offset, SIZE );
    }
}
