package org.apache.poi.hpsf;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

@Internal
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

    byte[] getValue()
    {
        return _value;
    }

    byte[] toByteArray()
    {
        byte[] result = new byte[getSize()];
        LittleEndian.putInt( result, 0 * LittleEndian.INT_SIZE,
                LittleEndian.INT_SIZE + _value.length );
        LittleEndian.putInt( result, 1 * LittleEndian.INT_SIZE, _format );
        LittleEndian.putInt( result, 2 * LittleEndian.INT_SIZE, _value.length );
        System.arraycopy( _value, 0, result, LittleEndian.INT_SIZE
                + LittleEndian.INT_SIZE, _value.length );
        return result;
    }

    int write( OutputStream out ) throws IOException
    {
        LittleEndian.putInt( LittleEndian.INT_SIZE + _value.length, out );
        LittleEndian.putInt( _format, out );
        out.write( _value );
        return 2 * LittleEndian.INT_SIZE + _value.length;
    }
}
