package org.apache.poi.hwpf.model;

import org.apache.poi.util.ArrayUtil;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

public class NilPICFAndBinData
{

    private static final POILogger log = POILogFactory
            .getLogger( NilPICFAndBinData.class );

    private byte[] _binData;

    public NilPICFAndBinData( byte[] data, int offset )
    {
        fillFields( data, offset );
    }

    public void fillFields( byte[] data, int offset )
    {
        int lcb = LittleEndian.getInt( data, offset );
        int cbHeader = LittleEndian.getUShort( data, offset
                + LittleEndian.INT_SIZE );

        if ( cbHeader != 0x44 )
        {
            log.log( POILogger.WARN, "NilPICFAndBinData at offset ", offset,
                    " cbHeader 0x" + Integer.toHexString( cbHeader )
                            + " != 0x44" );
        }

        // skip the 62 ignored bytes
        int binaryLength = lcb - cbHeader;
        this._binData = ArrayUtil.copyOfRange( data, offset + cbHeader,
                offset + cbHeader + binaryLength );
    }

    public byte[] getBinData()
    {
        return _binData;
    }

    public byte[] serialize()
    {
        byte[] bs = new byte[_binData.length + 0x44];
        LittleEndian.putInt( bs, 0, _binData.length + 0x44 );
        System.arraycopy( _binData, 0, bs, 0x44, _binData.length );
        return bs;
    }

    public int serialize( byte[] data, int offset )
    {
        LittleEndian.putInt( data, offset, _binData.length + 0x44 );
        System.arraycopy( _binData, 0, data, offset + 0x44, _binData.length );
        return 0x44 + _binData.length;
    }
}
