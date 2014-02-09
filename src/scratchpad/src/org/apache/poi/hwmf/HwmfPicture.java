package org.apache.poi.hwmf;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hwmf.record.WmfHeader;
import org.apache.poi.hwmf.record.WmfPlaceableHeader;
import org.apache.poi.hwmf.record.WmfRecord;
import org.apache.poi.hwmf.record.WmfRecordType;
import org.apache.poi.util.LittleEndianInputStream;

public class HwmfPicture {
    // http://www.symantec.com/avcenter/reference/inside.the.windows.meta.file.format.pdf
    
    public HwmfPicture(InputStream inputStream) throws IOException {
        LittleEndianInputStream leis = new LittleEndianInputStream(inputStream);
        WmfPlaceableHeader placeableHeader = WmfPlaceableHeader.readHeader(leis);
        WmfHeader header = new WmfHeader(leis);
        
        for (;;) {
            long recordSize = leis.readUInt();
            int recordFunction = leis.readShort();
            WmfRecordType wrt = WmfRecordType.getById(recordFunction);
            if (wrt == null) {
                throw new IOException("unexpected record type: "+recordFunction);
            }
            if (wrt == WmfRecordType.eof) break;
            if (wrt.clazz == null) {
                throw new IOException("unsupported record type: "+recordFunction);
            }
            
            WmfRecord wr;
            try {
                wr = wrt.clazz.newInstance();
            } catch (Exception e) {
                throw (IOException)new IOException("can't create wmf record").initCause(e);
            }
            
            int consumedSize = wr.init(leis, recordSize, recordFunction);
            if (consumedSize < recordSize) {
                leis.skip(recordSize - consumedSize);
            }
        }
    }


}
