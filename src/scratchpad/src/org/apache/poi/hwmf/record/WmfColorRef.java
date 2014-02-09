package org.apache.poi.hwmf.record;

import java.awt.Color;
import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class WmfColorRef {
    /**
     * A 32-bit ColorRef Object that defines the color value.
     * Red (1 byte):  An 8-bit unsigned integer that defines the relative intensity of red.
     * Green (1 byte):  An 8-bit unsigned integer that defines the relative intensity of green.
     * Blue (1 byte):  An 8-bit unsigned integer that defines the relative intensity of blue.
     * Reserved (1 byte):  An 8-bit unsigned integer that MUST be 0x00.
     */
    Color colorRef;
    
    public int init(LittleEndianInputStream leis) throws IOException {
        int red = leis.readUByte();
        int green = leis.readUByte();
        int blue = leis.readUByte();
        @SuppressWarnings("unused")
        int reserved = leis.readUByte();

        colorRef = new Color(red, green, blue);
        return 4*LittleEndianConsts.BYTE_SIZE;
    }

}
