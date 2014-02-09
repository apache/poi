package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianInputStream;

public interface WmfRecord {
    WmfRecordType getRecordType();

    /**
     * Init record from stream
     *
     * @param leis the little endian input stream
     * @return count of processed bytes
     * @throws IOException
     */
    int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException;
}
