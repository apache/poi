package org.apache.poi.hpsf;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.util.LittleEndian;

class Filetime
{
    static final int SIZE = LittleEndian.INT_SIZE * 2;

    private int _dwHighDateTime;
    private int _dwLowDateTime;

    Filetime( byte[] data, int offset )
    {
        _dwLowDateTime = LittleEndian.getInt( data, offset + 0
                * LittleEndian.INT_SIZE );
        _dwHighDateTime = LittleEndian.getInt( data, offset + 1
                * LittleEndian.INT_SIZE );
    }

    Filetime( int low, int high )
    {
        _dwLowDateTime = low;
        _dwHighDateTime = high;
    }

    long getHigh()
    {
        return _dwHighDateTime;
    }

    long getLow()
    {
        return _dwLowDateTime;
    }

    byte[] toByteArray()
    {
        byte[] result = new byte[SIZE];
        LittleEndian.putInt( result, 0 * LittleEndian.INT_SIZE, _dwLowDateTime );
        LittleEndian
                .putInt( result, 1 * LittleEndian.INT_SIZE, _dwHighDateTime );
        return result;
    }

    int write( OutputStream out ) throws IOException
    {
        LittleEndian.putInt( _dwLowDateTime, out );
        LittleEndian.putInt( _dwHighDateTime, out );
        return SIZE;
    }
}
