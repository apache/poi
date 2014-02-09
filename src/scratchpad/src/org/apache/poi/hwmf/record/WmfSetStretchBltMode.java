package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

/**
 * The META_SETSTRETCHBLTMODE record defines the bitmap stretching mode in the playback device
 * context.
 */
public class WmfSetStretchBltMode implements WmfRecord {
    
    /**
     * A 16-bit unsigned integer that defines bitmap stretching mode.
     * This MUST be one of the values:
     * BLACKONWHITE = 0x0001,
     * WHITEONBLACK = 0x0002,
     * COLORONCOLOR = 0x0003,
     * HALFTONE = 0x0004
     */
    int setStretchBltMode;
    
    public WmfRecordType getRecordType() {
        return WmfRecordType.setStretchBltMode;
    }
    
    public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
        setStretchBltMode = leis.readUShort();
        return LittleEndianConsts.SHORT_SIZE;
    }
}
