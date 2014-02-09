package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianInputStream;

/**
 * The META_SETBKCOLOR record sets the background color in the playback device context to a
 * specified color, or to the nearest physical color if the device cannot represent the specified color.
 */
public class WmfSetBkColor implements WmfRecord {
    
    WmfColorRef colorRef;
    
    public WmfRecordType getRecordType() {
        return WmfRecordType.setBkColor;
    }
    
    public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
        WmfColorRef colorRef = new WmfColorRef();
        return colorRef.init(leis);
    }
}
