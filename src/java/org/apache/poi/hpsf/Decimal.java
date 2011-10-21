package org.apache.poi.hpsf;

import org.apache.poi.util.Internal;

import org.apache.poi.util.LittleEndian;

@Internal
class Decimal
{
    static final int SIZE = 16;

    private short field_1_wReserved;
    private byte field_2_scale;
    private byte field_3_sign;
    private int field_4_hi32;
    private long field_5_lo64;

    Decimal( final byte[] data, final int startOffset )
    {
        int offset = startOffset;

        field_1_wReserved = LittleEndian.getShort( data, offset );
        offset += LittleEndian.SHORT_SIZE;

        field_2_scale = data[offset];
        offset += LittleEndian.BYTE_SIZE;

        field_3_sign = data[offset];
        offset += LittleEndian.BYTE_SIZE;

        field_4_hi32 = LittleEndian.getInt( data, offset );
        offset += LittleEndian.INT_SIZE;

        field_5_lo64 = LittleEndian.getLong( data, offset );
        offset += LittleEndian.LONG_SIZE;
    }
}
