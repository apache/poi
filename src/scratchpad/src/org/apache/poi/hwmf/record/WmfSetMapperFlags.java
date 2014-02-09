package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

/**
 * The META_SETMAPPERFLAGS record defines the algorithm that the font mapper uses when it maps
 * logical fonts to physical fonts.
 */
public class WmfSetMapperFlags implements WmfRecord {
    
    /**
     * A 32-bit unsigned integer that defines whether the font mapper should attempt to
     * match a font's aspect ratio to the current device's aspect ratio. If bit 0 is
     * set, the mapper selects only matching fonts.
     */
    long mapperValues;
    
    public WmfRecordType getRecordType() {
        return WmfRecordType.setMapperFlags;
    }
    
    public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
        mapperValues = leis.readUInt();
        return LittleEndianConsts.INT_SIZE;
    }
}
