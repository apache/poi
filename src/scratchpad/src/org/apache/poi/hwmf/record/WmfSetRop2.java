package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

/**
 * The META_SETROP2 record defines the foreground raster operation mix mode in the playback device
 * context. The foreground mix mode is the mode for combining pens and interiors of filled objects with
 * foreground colors on the output surface.
 */
public class WmfSetRop2 implements WmfRecord {
    
    /**
     * A 16-bit unsigned integer that defines the foreground binary raster
     * operation mixing mode. This MUST be one of the values:
     * R2_BLACK = 0x0001,
     * R2_NOTMERGEPEN = 0x0002,
     * R2_MASKNOTPEN = 0x0003,
     * R2_NOTCOPYPEN = 0x0004,
     * R2_MASKPENNOT = 0x0005,
     * R2_NOT = 0x0006,
     * R2_XORPEN = 0x0007,
     * R2_NOTMASKPEN = 0x0008,
     * R2_MASKPEN = 0x0009,
     * R2_NOTXORPEN = 0x000A,
     * R2_NOP = 0x000B,
     * R2_MERGENOTPEN = 0x000C,
     * R2_COPYPEN = 0x000D,
     * R2_MERGEPENNOT = 0x000E,
     * R2_MERGEPEN = 0x000F,
     * R2_WHITE = 0x0010
     */
    int drawMode;
    
    public WmfRecordType getRecordType() {
        return WmfRecordType.setRop2;
    }
    
    public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
        drawMode = leis.readUShort();
        return LittleEndianConsts.SHORT_SIZE;
    }
}
