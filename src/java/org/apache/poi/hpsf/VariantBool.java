package org.apache.poi.hpsf;

import org.apache.poi.util.LittleEndian;

import org.apache.poi.util.Internal;

@Internal
class VariantBool
{
    static final int SIZE = 2;

    private boolean _value;

    VariantBool( byte[] data, int offset )
    {
        short value = LittleEndian.getShort( data, offset );
        if ( value == 0x0000 )
        {
            _value = false;
            return;
        }

        if ( value == 0xffff )
        {
            _value = true;
            return;
        }

        throw new IllegalPropertySetDataException( "VARIANT_BOOL value '"
                + value + "' is incorrect" );
    }
}
