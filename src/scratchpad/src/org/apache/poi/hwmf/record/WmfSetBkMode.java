package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

/**
 * The META_SETBKMODE record defines the background raster operation mix mode in the playback
 * device context. The background mix mode is the mode for combining pens, text, hatched brushes,
 * and interiors of filled objects with background colors on the output surface.
 */
public class WmfSetBkMode implements WmfRecord {
    
    /**
     * A 16-bit unsigned integer that defines background mix mode.
     * This MUST be either TRANSPARENT = 0x0001 or OPAQUE = 0x0002
     */
    int bkMode;
    
    public WmfRecordType getRecordType() {
        return WmfRecordType.setBkMode;
    }
    
    public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
        bkMode = leis.readUShort();
        return LittleEndianConsts.SHORT_SIZE;
    }
}
