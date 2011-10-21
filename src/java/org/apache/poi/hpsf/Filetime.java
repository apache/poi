package org.apache.poi.hpsf;

import org.apache.poi.util.LittleEndian;

class Filetime
{
    static final int SIZE = LittleEndian.LONG_SIZE * 2;

    private long _dwLowDateTime;
    private long _dwHighDateTime;

    Filetime( byte[] data, int offset )
    {
        _dwLowDateTime = LittleEndian.getLong( data, offset + 0 );
        _dwHighDateTime = LittleEndian.getLong( data, offset + 4 );
    }
}
