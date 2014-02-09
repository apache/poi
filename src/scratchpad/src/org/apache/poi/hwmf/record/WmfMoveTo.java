package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

/**
 * The META_MOVETO record sets the output position in the playback device context to a specified
 * point.
 */
public class WmfMoveTo implements WmfRecord {
    
    /**
     * A 16-bit signed integer that defines the y-coordinate, in logical units.
     */
    int y;
    
    /**
     * A 16-bit signed integer that defines the x-coordinate, in logical units.
     */
    int x;
    
    public WmfRecordType getRecordType() {
        return WmfRecordType.moveTo;
    }
    
    public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
        y = leis.readShort();
        x = leis.readShort();
        return 2*LittleEndianConsts.SHORT_SIZE;
    }
}
