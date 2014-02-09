package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class WmfEscape implements WmfRecord {
    
    /**
     * A 16-bit unsigned integer that defines the escape function. The 
     * value MUST be from the MetafileEscapes enumeration.
     */
    int escapeFunction;
    /**
     * A 16-bit unsigned integer that specifies the size, in bytes, of the 
     * EscapeData field.
     */
    int byteCount;
    /**
     * An array of bytes of size ByteCount.
     */
    byte escapeData[];
    
    public WmfRecordType getRecordType() {
        return WmfRecordType.escape;
    }
    
    public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
        escapeFunction = leis.readUShort();
        byteCount = leis.readUShort();
        escapeData = new byte[byteCount];
        leis.read(escapeData);
        return 2*LittleEndianConsts.SHORT_SIZE+byteCount;
    }
}
