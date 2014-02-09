package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

/**
 * The META_RESTOREDC record restores the playback device context from a previously saved device
 * context.
 */
public class WmfRestoreDc implements WmfRecord {
    
    /**
     * nSavedDC (2 bytes):  A 16-bit signed integer that defines the saved state to be restored. If this 
     * member is positive, nSavedDC represents a specific instance of the state to be restored. If 
     * this member is negative, nSavedDC represents an instance relative to the current state.
     */
    int nSavedDC;
    
    public WmfRecordType getRecordType() {
        return WmfRecordType.restoreDc;
    }
    
    public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
        nSavedDC = leis.readShort();
        return LittleEndianConsts.SHORT_SIZE;
    }
}
