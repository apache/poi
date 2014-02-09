package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

/**
 * The META_SETLAYOUT record defines the layout orientation in the playback device context.
 * The layout orientation determines the direction in which text and graphics are drawn
 */
public class WmfSetLayout implements WmfRecord {
    
    /**
     * A 16-bit unsigned integer that defines the layout of text and graphics.
     * LAYOUT_LTR = 0x0000
     * LAYOUT_RTL = 0x0001
     * LAYOUT_BITMAPORIENTATIONPRESERVED = 0x0008
     */
    int layout;
    
    public WmfRecordType getRecordType() {
        return WmfRecordType.setLayout;
    }
    
    @SuppressWarnings("unused")
    public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
        layout = leis.readUShort();
        // A 16-bit field that MUST be ignored.
        int reserved = leis.readShort();
        return 2*LittleEndianConsts.SHORT_SIZE;
    }
}
