package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianInputStream;

public class WmfNoArg {
    protected static abstract class WmfNoArgParent implements WmfRecord {
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return 0;
        }
    }
    
    /**
     * The META_SAVEDC record saves the playback device context for later retrieval.
     */
    public static class WmfSaveDc extends WmfNoArgParent {
        public WmfRecordType getRecordType() { return WmfRecordType.saveDc; }
    }

    /**
     * The META_SETRELABS record is reserved and not supported.
     */
    public static class WmfSetRelabs extends WmfNoArgParent {
        public WmfRecordType getRecordType() { return WmfRecordType.setRelabs; }
    }
}
