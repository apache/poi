package org.apache.poi.hpsf;

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

@Internal
class VariantBool
{
    private final static POILogger logger = POILogFactory.getLogger( VariantBool.class );
    
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

        logger.log( POILogger.WARN, "VARIANT_BOOL value '",
                Short.valueOf( value ), "' is incorrect" );
        _value = value != 0;
    }

    boolean getValue()
    {
        return _value;
    }

    void setValue( boolean value )
    {
        this._value = value;
    }
}
