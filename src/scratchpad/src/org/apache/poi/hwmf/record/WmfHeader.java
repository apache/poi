package org.apache.poi.hwmf.record;

import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class WmfHeader {
    private int type;
    private int recordSize;
    private int version;
    private int filesize;
    private int numberOfObjects;
    private long maxRecord;
    private int numberOfMembers;
    
    public WmfHeader(LittleEndianInputStream leis) throws IOException {
        // Type (2 bytes):  A 16-bit unsigned integer that defines the type of metafile
        // MEMORYMETAFILE = 0x0001, DISKMETAFILE = 0x0002 
        type = leis.readUShort();

        // HeaderSize (2 bytes):  A 16-bit unsigned integer that defines the number
        // of 16-bit words in the header.
        recordSize = leis.readUShort();
        int bytesLeft = recordSize*LittleEndianConsts.SHORT_SIZE;
        
        // Version (2 bytes):  A 16-bit unsigned integer that defines the metafile version.
        // METAVERSION100 = 0x0100, METAVERSION300 = 0x0300
        version = leis.readUShort();
        bytesLeft -= LittleEndianConsts.SHORT_SIZE;
        
        // SizeLow (2 bytes):  A 16-bit unsigned integer that defines the low-order word
        // of the number of 16-bit words in the entire metafile.
        // SizeHigh (2 bytes):  A 16-bit unsigned integer that defines the high-order word
        // of the number of 16-bit words in the entire metafile.
        filesize = leis.readInt();
        bytesLeft -= LittleEndianConsts.INT_SIZE;
        
        // NumberOfObjects (2 bytes):  A 16-bit unsigned integer that specifies the number
        // of graphics objects that are defined in the entire metafile. These objects include
        // brushes, pens, and the other objects
        numberOfObjects = leis.readUShort();
        bytesLeft -= LittleEndianConsts.SHORT_SIZE;
        
        // MaxRecord (4 bytes):  A 32-bit unsigned integer that specifies the size of the
        // largest record used in the metafile (in 16-bit elements).
        maxRecord = leis.readUInt();
        bytesLeft -= LittleEndianConsts.INT_SIZE;
        
        // NumberOfMembers (2 bytes):  A 16-bit unsigned integer that is not used.
        // It SHOULD be 0x0000.
        numberOfMembers = leis.readUShort();
        bytesLeft -= LittleEndianConsts.SHORT_SIZE;
        
        leis.skip(bytesLeft);
    }
}
